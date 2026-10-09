package tools.dynamia.actions.replay;

import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.actions.ActionFlows;
import tools.dynamia.actions.flow.FlowTokenException;
import tools.dynamia.integration.Containers;
import tools.dynamia.ui.ChoicesProvider;
import tools.dynamia.ui.FileTransfer;
import tools.dynamia.ui.NavigationProvider;
import tools.dynamia.ui.ProgressRunner;
import tools.dynamia.ui.UIFacades;
import tools.dynamia.ui.UIMessages;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Runs an action written once, against the UI ports, for a client that talks to the server in separate requests.
 * <p>
 * The problem: {@code UIMessages.showQuestion("Delete?", () -> delete())} keeps "what comes after the answer" as a
 * callback in the Java stack, which a desktop UI (ZK) holds between events and a stateless REST server cannot.
 * <p>
 * The solution is to <em>replay</em> instead of suspending. The action runs from the start; its questions are
 * answered, in order, with the answers the user already gave ({@link ReplayInteractions}); the first question
 * without an answer stops the pass, is sent to the client as an {@code ActionFlowStep} and the request plus the
 * answers go in a signed, expiring resume token. When the answer comes back the action runs again from the start with
 * one more answer. No server state, no sticky sessions, nothing to serialize but plain data, and the wire protocol is
 * the one of {@link tools.dynamia.actions.FlowRemoteAction}.
 * <p>
 * Every pass runs in a transaction (see {@link ReplayTransactions}) that is committed only by the pass that ran to
 * the end, so the passes that stopped at a question leave nothing behind.
 */
public final class ReplayExecutor {

    private static final String REQUEST_KEY = "request";
    private static final String ANSWERS_KEY = "answers";
    /** Messages the action showed in the final pass, in {@link ActionExecutionResponse#getParams()}. */
    public static final String NOTIFICATIONS_PARAM = "notifications";
    /** Response param with the files the action gave to the user: a list of {@code {name, contentType, content}}, Base64. */
    public static final String DOWNLOADS_PARAM = "downloads";

    private ReplayExecutor() {
    }

    /**
     * Runs one request of a replayed action.
     *
     * @param actionId id of the action, bound to the resume token
     * @param request  the HTTP request, with the resume token and the answer when it continues a flow
     * @param body     the action itself: it receives the <em>original</em> request (the same one in every pass) and
     *                 returns the data of the final response
     * @return the question to ask, or the final response
     */
    public static ActionExecutionResponse execute(String actionId, ActionExecutionRequest request,
                                                  Function<ActionExecutionRequest, Object> body) {
        boolean resuming = request.getResumeToken() != null && !request.getResumeToken().isBlank();
        String flowId;
        ActionExecutionRequest original;
        List<Object> answers = new ArrayList<>();

        if (resuming) {
            try {
                var payload = ActionFlows.verifyToken(request.getResumeToken(), actionId);
                flowId = payload.flowId();
                original = requestFromMap(asMap(payload.data().get(REQUEST_KEY)));
                if (payload.data().get(ANSWERS_KEY) instanceof List<?> previous) {
                    answers.addAll(previous);
                }
                answers.add(request.getData());
            } catch (FlowTokenException e) {
                return new ActionExecutionResponse(e.getMessage(), "ERROR", 401);
            }
        } else {
            flowId = request.getFlowId() != null ? request.getFlowId() : UUID.randomUUID().toString();
            original = request;
        }

        var session = new ReplaySession(answers);
        var interactions = new ReplayInteractions(session);
        Object result = transactions().run(
                () -> ReplaySession.run(session, () -> UIMessages.withDisplayer(interactions,
                        () -> UIFacades.with(FileTransfer.class, new ReplayFileTransfer(session),
                                () -> UIFacades.with(ProgressRunner.class, new ReplayProgressRunner(session),
                                        () -> UIFacades.with(ChoicesProvider.class, new ReplayChoicesProvider(session),
                                                () -> UIFacades.with(NavigationProvider.class, new ReplayNavigationProvider(session),
                                                        () -> bindAll(session, binders(), 0, () -> body.apply(original)))))))),
                () -> !interactions.isPending());

        if (interactions.isPending()) {
            Map<String, Object> carried = new HashMap<>();
            carried.put(REQUEST_KEY, requestToMap(original));
            carried.put(ANSWERS_KEY, answers);
            return ActionFlows.toResponse(flowId, actionId, carried, interactions.pending());
        }

        var notifications = interactions.notifications();
        var last = notifications.isEmpty() ? null : notifications.get(notifications.size() - 1);
        var done = last == null ? ActionFlowStep.done(result) : ActionFlowStep.done(result, last.message(), last.type());
        var finalStep = session.redirectUrl() == null ? done
                : ActionFlowStep.redirect(session.redirectUrl(), false, session.redirectInNewWindow());
        var response = ActionFlows.toResponse(flowId, actionId, Map.of(), finalStep);
        var downloads = session.downloads();
        if (!notifications.isEmpty() || !downloads.isEmpty()) {
            var params = new HashMap<String, Object>();
            if (!notifications.isEmpty()) {
                params.put(NOTIFICATIONS_PARAM, notifications.stream()
                        .map(n -> Map.of("message", String.valueOf(n.message()), "type", String.valueOf(n.type())))
                        .toList());
            }
            if (!downloads.isEmpty()) {
                params.put(DOWNLOADS_PARAM, downloads.stream().map(d -> {
                    var file = new LinkedHashMap<String, Object>();
                    file.put("name", d.name());
                    file.put("contentType", d.contentType());
                    file.put("content", Base64.getEncoder().encodeToString(d.content()));
                    return file;
                }).toList());
            }
            response.setParams(params);
        }
        return response;
    }

    private static List<ReplayBinder> binders() {
        var found = Containers.get().findObjects(ReplayBinder.class);
        return found == null ? List.of() : new ArrayList<>(found);
    }

    static <T> T bindAll(ReplaySession session, List<ReplayBinder> binders, int index, java.util.function.Supplier<T> work) {
        if (index >= binders.size()) {
            return work.get();
        }
        return binders.get(index).bind(session, () -> bindAll(session, binders, index + 1, work));
    }

    private static ReplayTransactions transactions() {
        var found = Containers.get().findObject(ReplayTransactions.class);
        return found != null ? found : ReplayTransactions.NONE;
    }

    // -- the original request travels in the token: only plain data ---------------------------------------

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : new HashMap<>();
    }

    private static Map<String, Object> requestToMap(ActionExecutionRequest request) {
        var map = new LinkedHashMap<String, Object>();
        map.put("data", request.getData());
        map.put("params", request.getParams());
        map.put("source", request.getSource());
        map.put("dataType", request.getDataType());
        map.put("dataId", request.getDataId());
        map.put("dataName", request.getDataName());
        return map;
    }

    @SuppressWarnings("unchecked")
    private static ActionExecutionRequest requestFromMap(Map<String, Object> map) {
        var request = new ActionExecutionRequest();
        request.setData(map.get("data"));
        request.setParams((Map<String, Object>) map.get("params"));
        request.setSource((String) map.get("source"));
        request.setDataType((String) map.get("dataType"));
        request.setDataId((String) map.get("dataId"));
        request.setDataName((String) map.get("dataName"));
        return request;
    }
}
