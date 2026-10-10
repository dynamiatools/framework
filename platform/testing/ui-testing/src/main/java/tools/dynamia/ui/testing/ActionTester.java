/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.ui.testing;

import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.actions.ActionFlowStepType;
import tools.dynamia.actions.LocalAction;
import tools.dynamia.actions.replay.ReplayExecutor;
import tools.dynamia.crud.headless.HeadlessViewsContributor;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIFacades;
import tools.dynamia.ui.testing.UIInteraction.Type;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.ViewDescriptorBuilder;
import tools.dynamia.viewers.ViewDescriptorFactory;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Runs an action against a scripted user, without ZK, a browser or an HTTP server.
 * <pre>{@code
 * ActionResult r = ActionTester.of(new DeleteAction())
 *         .crud(Sale.class, crudService)
 *         .on(sale)
 *         .user(u -> u.confirm(true))
 *         .runEverywhere();
 *
 * assertThat(r.types()).containsExactly(CONFIRM, NOTIFY);
 * }</pre>
 * <ul>
 *     <li>{@link #run()}: direct execution, callbacks run immediately as in ZK. Shows the logic of the action.</li>
 *     <li>{@link #runRemote()}: through the real {@link ReplayExecutor}, one pass per answer, signed tokens. Shows the
 *     action works for a remote client.</li>
 *     <li>{@link #runEverywhere()}: both, failing if they differ. It is what an action declared {@code HEADLESS} must
 *     pass.</li>
 * </ul>
 * The script is built again for every run from the function given to {@link #user(Consumer)}.
 */
public final class ActionTester {

    private static final String CONTAINER_NAME = "ui-testing";

    private final LocalAction action;
    private TestCrud<?> crud;
    private Object data;
    private final Map<String, Object> params = new HashMap<>();
    private final List<Object> beans = new ArrayList<>();
    private Consumer<UIScript> user = script -> {
    };
    private boolean lenient;

    private ActionTester(LocalAction action) {
        this.action = action;
    }

    /**
     * @param action the action under test
     * @return the tester
     */
    public static ActionTester of(LocalAction action) {
        return new ActionTester(action);
    }

    /**
     * Gives the action a CRUD (headless controller and view) over {@code crudService}. The service is also made available
     * in the container, where actions look it up.
     *
     * @param entityClass the entity of the CRUD
     * @param crudService the service, real or mock
     * @param <E>         entity type
     * @return this tester
     */
    public <E> ActionTester crud(Class<E> entityClass, CrudService crudService) {
        this.crud = TestCrud.of(entityClass, crudService);
        beans.add(crudService);
        return this;
    }

    /**
     * @param crud a CRUD built by hand, for a custom state
     * @return this tester
     */
    public ActionTester crud(TestCrud<?> crud) {
        this.crud = crud;
        return this;
    }

    /**
     * @param data the data of the action event, usually the selected entity
     * @return this tester
     */
    public ActionTester on(Object data) {
        this.data = data;
        return this;
    }

    /**
     * @param name  parameter name
     * @param value parameter value
     * @return this tester
     */
    public ActionTester param(String name, Object value) {
        params.put(name, value);
        return this;
    }

    /**
     * Makes {@code bean} available in the container for the duration of the run, for actions that look services up.
     *
     * @param bean the service
     * @return this tester
     */
    public ActionTester bean(Object bean) {
        beans.add(bean);
        return this;
    }

    /**
     * @param script writes what the user answers, in order
     * @return this tester
     */
    public ActionTester user(Consumer<UIScript> script) {
        this.user = script;
        return this;
    }

    /**
     * @return this tester, with scripts that do not fail on unanswered interactions or leftover answers
     */
    public ActionTester lenient() {
        this.lenient = true;
        return this;
    }

    private UIScript newScript() {
        var script = new UIScript();
        if (lenient) {
            script.lenient();
        }
        user.accept(script);
        return script;
    }

    private ActionEvent newEvent() {
        var eventParams = new HashMap<>(params);
        return crud != null ? crud.event(data, action, eventParams) : eventWithParams(eventParams);
    }

    private ActionEvent eventWithParams(Map<String, Object> eventParams) {
        var event = new ActionEvent(data, action);
        event.getParams().putAll(eventParams);
        return event;
    }

    private <R> R withContainer(boolean remote, java.util.function.Supplier<R> work) {
        var container = new SimpleObjectContainer(CONTAINER_NAME);
        beans.forEach(container::addObject);
        if (remote) {
            container.addObject(new HeadlessViewsContributor());
        }
        if (beans.stream().noneMatch(ViewDescriptorFactory.class::isInstance)) {
            container.addObject(autoFieldsFactory());
        }
        Containers.get().installObjectContainer(container);
        try {
            return work.get();
        } finally {
            Containers.get().removeContainer(CONTAINER_NAME);
        }
    }

    /**
     * Stands for the application's descriptor factory (forms filled with values need one): every class gets a form with one field per
     * property, which is what forms of entities without a descriptor file look like. Register your own with
     * {@link #bean(Object)} to test against real descriptors.
     */
    private static ViewDescriptorFactory autoFieldsFactory() {
        return (ViewDescriptorFactory) Proxy.newProxyInstance(ViewDescriptorFactory.class.getClassLoader(),
                new Class<?>[]{ViewDescriptorFactory.class}, (proxy, method, args) -> {
                    if (ViewDescriptor.class.isAssignableFrom(method.getReturnType())) {
                        for (Object arg : args) {
                            if (arg instanceof Class<?> type) {
                                return ViewDescriptorBuilder.viewDescriptor("form", type).autofields(true).build();
                            }
                        }
                        return null;
                    }
                    if (Set.class.isAssignableFrom(method.getReturnType())) {
                        return Set.of();
                    }
                    return null;
                });
    }

    // -- direct --------------------------------------------------------------------------------------------

    /**
     * Runs the action directly: callbacks run immediately, as in ZK.
     *
     * @return what happened
     * @throws AssertionError when the script does not match what the action asks
     */
    public ActionResult run() {
        var script = newScript();
        var environment = new TestUIEnvironment(script);
        Throwable failure = withContainer(false, () -> UIFacades.with(environment, () -> {
            try {
                action.actionPerformed(newEvent());
                return null;
            } catch (AssertionError e) {
                throw e;
            } catch (Throwable e) {
                return e;
            }
        }));
        if (failure == null) {
            script.verifyConsumed();
        }
        return new ActionResult("direct", environment.interactions(), environment.downloads(), environment.redirectUrl(),
                failure, crud, List.of(), 0);
    }

    // -- remote --------------------------------------------------------------------------------------------

    /**
     * Runs the action as a remote client would: through {@link ReplayExecutor}, one pass for every answer, with signed
     * resume tokens.
     *
     * @return what happened
     * @throws AssertionError when the script does not match what the action asks
     */
    public ActionResult runRemote() {
        var script = newScript();
        return withContainer(true, () -> remote(script));
    }

    @SuppressWarnings("unchecked")
    private ActionResult remote(UIScript script) {
        var interactions = new ArrayList<UIInteraction>();
        var steps = new ArrayList<ActionFlowStep>();
        var downloads = new ArrayList<CapturedDownload>();
        String redirect = null;
        Throwable failure = null;
        int maxToken = 0;
        boolean abandoned = false;

        var request = new ActionExecutionRequest();
        while (true) {
            ActionExecutionResponse response;
            try {
                response = ReplayExecutor.execute(action.getId(), request, original -> {
                    action.actionPerformed(newEvent());
                    return null;
                });
            } catch (AssertionError e) {
                throw e;
            } catch (Throwable e) {
                failure = e;
                break;
            }
            var step = response.getFlow();
            if (step == null) {
                failure = new AssertionError("The replay executor answered without a step: " + response.getStatus()
                        + " " + response.getData());
                break;
            }
            if (step.getType() == ActionFlowStepType.DONE || step.getType() == ActionFlowStepType.REDIRECT) {
                addFinal(response, interactions, downloads);
                if (step.getType() == ActionFlowStepType.REDIRECT) {
                    var target = (Map<String, Object>) step.getData();
                    redirect = String.valueOf(target.get("url"));
                    interactions.add(new UIInteraction(Type.REDIRECT, null, redirect, null, Map.copyOf(target)));
                }
                break;
            }
            steps.add(step);
            maxToken = Math.max(maxToken, step.getResumeToken() == null ? 0 : step.getResumeToken().length());
            var interaction = toInteraction(step);
            interactions.add(interaction);
            var answer = answerFor(script, interaction, step, interactions.size());
            if (answer == ABANDONED) {
                abandoned = true;
                break;
            }
            request = new ActionExecutionRequest();
            request.setResumeToken(step.getResumeToken());
            request.setData(answer);
        }
        if (failure == null && !abandoned) {
            script.verifyConsumed();
        }
        return new ActionResult("remote", interactions, downloads, redirect, failure, crud, steps, maxToken);
    }

    private static final Object ABANDONED = new Object();

    private static UIInteraction toInteraction(ActionFlowStep step) {
        var type = Type.valueOf(step.getType().name());
        var payload = new LinkedHashMap<String, Object>();
        if (step.getViewDescriptor() != null) {
            payload.put("viewName", step.getViewDescriptor());
        }
        if (step.getViewClass() != null) {
            payload.put("beanClass", step.getViewClass());
        }
        if (step.getData() != null) {
            payload.put("data", step.getData());
        }
        return new UIInteraction(type, step.getTitle(), step.getMessage(), step.getMessageType(), payload);
    }

    @SuppressWarnings("unchecked")
    private Object answerFor(UIScript script, UIInteraction interaction, ActionFlowStep step, int number) {
        if (interaction.type() == Type.VIEW) {
            return true; // the user only has to see it
        }
        var answer = script.answer(interaction, number);
        if (answer == null) {
            return ABANDONED;
        }
        switch (interaction.type()) {
            case CONFIRM -> {
                if (answer instanceof UIScript.Cancel) {
                    return false;
                }
                if (answer instanceof UIScript.Confirm confirm) {
                    return confirm.yes();
                }
            }
            case INPUT -> {
                if (answer instanceof UIScript.Cancel) {
                    return ABANDONED;
                }
                if (answer instanceof UIScript.Input input) {
                    return input.value();
                }
            }
            case DIALOG -> {
                if (answer instanceof UIScript.Cancel) {
                    return ABANDONED;
                }
                if (answer instanceof UIScript.Form form) {
                    if (form.values() == null) {
                        throw new AssertionError("submitForm(...) cannot run remotely: use fillForm(Map) in " + interaction);
                    }
                    return form.values();
                }
            }
            case CHOICE -> {
                if (answer instanceof UIScript.Cancel) {
                    return ABANDONED;
                }
                if (answer instanceof UIScript.Choose choose) {
                    var labels = (List<String>) ((Map<String, Object>) step.getData()).get("options");
                    var positions = new ArrayList<Integer>();
                    for (Object selected : choose.selection()) {
                        positions.add(selected instanceof Integer i ? i : labels.indexOf(String.valueOf(selected)));
                    }
                    boolean multiple = Boolean.TRUE.equals(((Map<String, Object>) step.getData()).get("multiple"));
                    return multiple ? positions : positions.get(0);
                }
            }
            case UPLOAD -> {
                if (answer instanceof UIScript.Cancel) {
                    return ABANDONED;
                }
                if (answer instanceof UIScript.Upload upload) {
                    var files = upload.files().stream().map(f -> {
                        var map = new LinkedHashMap<String, Object>();
                        map.put("name", f.name());
                        map.put("contentType", f.contentType());
                        map.put("content", Base64.getEncoder().encodeToString(f.content()));
                        return map;
                    }).toList();
                    boolean multiple = Boolean.TRUE.equals(((Map<String, Object>) step.getData()).get("multiple"));
                    return multiple ? files : files.get(0);
                }
            }
            default -> {
            }
        }
        throw UIScript.mismatch(interaction, number, answer);
    }

    @SuppressWarnings("unchecked")
    private static void addFinal(ActionExecutionResponse response, List<UIInteraction> interactions,
                                 List<CapturedDownload> downloads) {
        Map<String, Object> params = response.getParams();
        if (params == null) {
            return;
        }
        if (params.get("notifications") instanceof List<?> notifications) {
            for (Object item : notifications) {
                var map = (Map<String, Object>) item;
                interactions.add(new UIInteraction(Type.NOTIFY, null, String.valueOf(map.get("message")),
                        MessageType.valueOf(String.valueOf(map.get("type"))), Map.of()));
            }
        }
        if (params.get("downloads") instanceof List<?> files) {
            for (Object item : files) {
                var map = (Map<String, Object>) item;
                downloads.add(new CapturedDownload(String.valueOf(map.get("name")),
                        map.get("contentType") == null ? null : String.valueOf(map.get("contentType")),
                        Base64.getDecoder().decode(String.valueOf(map.get("content")))));
            }
        }
    }

    // -- everywhere ----------------------------------------------------------------------------------------

    /**
     * Runs the action directly and remotely and fails if they differ in what the user is asked, the messages shown, the
     * files given, the redirect or the exception. This is the acceptance condition of an action declared
     * {@code HEADLESS}.
     *
     * @return the direct result, with the remote one in {@link ActionResult#remote()}
     * @throws AssertionError when the two runs behave differently
     */
    public ActionResult runEverywhere() {
        var direct = run();
        var remote = runRemote();
        direct.attachRemote(remote);

        compare("exception", describe(direct.exception()), describe(remote.exception()));
        compare("questions", asked(direct), asked(remote));
        compare("notifications", notified(direct), notified(remote));
        compare("downloads", downloaded(direct), downloaded(remote));
        compare("redirect", direct.redirectUrl(), remote.redirectUrl());
        return direct;
    }

    private static List<String> asked(ActionResult result) {
        return result.interactions().stream()
                .filter(i -> i.type().asksTheUser() || i.type() == Type.VIEW)
                .map(i -> i.type() + " " + i.label()).toList();
    }

    private static List<String> notified(ActionResult result) {
        return result.notifications().stream().map(i -> i.messageType() + " " + i.message()).toList();
    }

    private static List<String> downloaded(ActionResult result) {
        return result.downloads().stream().map(d -> d.name() + " " + d.size()).toList();
    }

    private static String describe(Throwable exception) {
        return exception == null ? null : exception.getClass().getName() + ": " + exception.getMessage();
    }

    private static void compare(String what, Object direct, Object remote) {
        if (!Objects.equals(direct, remote)) {
            throw new AssertionError("The action does not behave the same in direct and remote execution: " + what
                    + "\n  direct: " + direct + "\n  remote: " + remote);
        }
    }
}
