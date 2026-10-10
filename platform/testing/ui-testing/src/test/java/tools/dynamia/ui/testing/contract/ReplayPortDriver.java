package tools.dynamia.ui.testing.contract;

import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.actions.ActionFlowStepType;
import tools.dynamia.actions.replay.ReplayExecutor;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.ui.contract.Effects;
import tools.dynamia.ui.contract.Outcome;
import tools.dynamia.ui.contract.PortDriver;
import tools.dynamia.ui.contract.Reply;
import tools.dynamia.ui.files.FlowPrincipal;
import tools.dynamia.ui.files.InMemoryTransferStore;
import tools.dynamia.ui.files.TransferMeta;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Drives an action the way a remote client does: through the real {@link ReplayExecutor}, one pass per answer, with the
 * resume token, a {@link InMemoryTransferStore} for the files, and a script that plays the user.
 */
public class ReplayPortDriver implements PortDriver {

    private final List<Object> beans;

    /**
     * @param beans what the adapter needs in the container besides the transfer store (for example the contributor of
     *              the forms)
     */
    public ReplayPortDriver(Object... beans) {
        this.beans = List.of(beans);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Outcome run(Consumer<Effects> action, List<Reply> replies) {
        var store = new InMemoryTransferStore();
        var container = new SimpleObjectContainer("replay-contract");
        container.addObject(store);
        beans.forEach(container::addObject);
        Containers.get().installObjectContainer(container);
        try {
            var asked = new ArrayList<Outcome.Asked>();
            var notices = new ArrayList<String>();
            var downloads = new ArrayList<Outcome.Download>();
            var last = new AtomicReference<Effects>(new Effects());
            Outcome.Redirect redirect = null;
            Throwable failure = null;
            boolean abandoned = false;
            int next = 0;

            var request = new ActionExecutionRequest();
            while (true) {
                ActionExecutionResponse response;
                try {
                    response = ReplayExecutor.execute("contract", request, original -> {
                        var effects = new Effects();
                        last.set(effects);
                        action.accept(effects);
                        return null;
                    });
                } catch (AssertionError e) {
                    throw e;
                } catch (Throwable e) {
                    failure = e;
                    break;
                }
                ActionFlowStep step = response.getFlow();
                if (step.getType() == ActionFlowStepType.DONE || step.getType() == ActionFlowStepType.REDIRECT) {
                    collectFinal(response, store, notices, downloads);
                    if (step.getType() == ActionFlowStepType.REDIRECT) {
                        var target = (Map<String, Object>) step.getData();
                        redirect = new Outcome.Redirect(String.valueOf(target.get("url")), Boolean.TRUE.equals(target.get("newWindow")));
                    }
                    break;
                }
                boolean isError = step.getMessageType() == tools.dynamia.ui.MessageType.ERROR;
                boolean questionIsMessage = step.getType() == ActionFlowStepType.CONFIRM || step.getType() == ActionFlowStepType.INPUT;
                asked.add(new Outcome.Asked(step.getType().name(), questionIsMessage ? step.getMessage() : step.getTitle(),
                        isError && !questionIsMessage ? step.getMessage() : null));
                if (step.getType() == ActionFlowStepType.VIEW) {
                    request = resume(step, true);
                    continue;
                }
                if (next >= replies.size()) {
                    throw new AssertionError("Unanswered " + step.getType() + " '" + (questionIsMessage ? step.getMessage() : step.getTitle()) + "'");
                }
                Object answer = answerFor(step, replies.get(next++), store);
                if (answer == ABANDONED) {
                    abandoned = true;
                    break;
                }
                request = resume(step, answer);
            }
            if (failure == null && !abandoned && next < replies.size()) {
                throw new AssertionError((replies.size() - next) + " reply(ies) were never asked for");
            }
            return new Outcome(asked, last.get().entries(), notices, downloads, redirect, failure);
        } finally {
            Containers.get().removeContainer("replay-contract");
        }
    }

    private static final Object ABANDONED = new Object();

    private static ActionExecutionRequest resume(ActionFlowStep step, Object answer) {
        var request = new ActionExecutionRequest();
        request.setResumeToken(step.getResumeToken());
        request.setData(answer);
        return request;
    }

    private static Object answerFor(ActionFlowStep step, Reply reply, InMemoryTransferStore store) {
        switch (step.getType()) {
            case CONFIRM -> {
                if (reply instanceof Reply.Yes) return true;
                if (reply instanceof Reply.No || reply instanceof Reply.Cancel) return false;
            }
            case INPUT -> {
                if (reply instanceof Reply.Input input) return input.value();
                if (reply instanceof Reply.Cancel) return ABANDONED;
            }
            case DIALOG -> {
                if (reply instanceof Reply.Form form) return form.values();
                if (reply instanceof Reply.Cancel) return ABANDONED;
            }
            case CHOICE -> {
                if (reply instanceof Reply.Choose choose) return choose.keys();
                if (reply instanceof Reply.Cancel) return ABANDONED;
            }
            case UPLOAD -> {
                if (reply instanceof Reply.Upload upload) {
                    return upload.files().stream().map(file -> {
                        try (var in = file.openStream()) {
                            var ref = store.put(in, TransferMeta.of(file.name(), file.contentType(),
                                    TransferMeta.Direction.UPLOAD, FlowPrincipal.current()), Long.MAX_VALUE);
                            return Map.of("ref", ref.ref());
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }).toList();
                }
                if (reply instanceof Reply.Cancel) return ABANDONED;
            }
            default -> {
            }
        }
        throw new AssertionError(step.getType() + " cannot be answered with " + reply);
    }

    @SuppressWarnings("unchecked")
    private static void collectFinal(ActionExecutionResponse response, InMemoryTransferStore store, List<String> notices,
                                     List<Outcome.Download> downloads) {
        Map<String, Object> params = response.getParams();
        if (params == null) {
            return;
        }
        if (params.get("notifications") instanceof List<?> list) {
            for (Object item : list) {
                var map = (Map<String, Object>) item;
                notices.add(map.get("type") + " " + map.get("message"));
            }
        }
        if (params.get("downloads") instanceof List<?> list) {
            for (Object item : list) {
                var map = (Map<String, Object>) item;
                String url = String.valueOf(map.get("url"));
                var stored = store.get(url.substring(url.lastIndexOf('/') + 1), FlowPrincipal.current()).orElseThrow();
                try (var in = stored.openStream()) {
                    downloads.add(new Outcome.Download(String.valueOf(map.get("name")),
                            map.get("contentType") == null ? null : String.valueOf(map.get("contentType")), in.readAllBytes()));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }
}
