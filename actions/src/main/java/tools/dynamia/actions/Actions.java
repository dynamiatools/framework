package tools.dynamia.actions;

import tools.dynamia.commons.MapBuilder;
import tools.dynamia.integration.Containers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Actions {

    private static final String ACTION_PARAM = "action";
    private static final String ACTION_COMPONENT = "ACTION_COMPONENT";

    /**
     * Helper method to run actions
     */
    public static void run(Action action, ActionEventBuilder eventBuilder, Object source, Object data, Map<String, Object> params) {
        if (action != null) {
            if (params == null) {
                params = MapBuilder.put(ACTION_PARAM, action);
            } else if (!params.containsKey(ACTION_PARAM)) {
                params = new HashMap<>(params);
                params.put(ACTION_PARAM, action);
            }

            var event = eventBuilder.buildActionEvent(source, params);
            if (data != null) {
                event.setData(data);
            }

            if (action instanceof ActionSelfFilter filter) {
                filter.beforeActionPerformed(event);
            }

            fireBeforeActionFilter(action, event);

            if (event.isPropagatable()) {
                var actionRunner = Containers.get().findObject(ActionRunner.class);
                if (actionRunner == null) {
                    actionRunner = new DefaultActionRunner();
                }
                actionRunner.run(action, event);
            }

            if (event.isPropagatable()) {
                if (action instanceof ActionSelfFilter filter) {
                    filter.afterActionPerformed(event);
                }

                fireAfterActionFilter(action, event);
            }

        }
    }

    public static void run(Action action, ActionEventBuilder eventBuilder, Object source, Map<String, Object> params) {
        run(action, eventBuilder, source, null, params);
    }

    public static void run(Action action, ActionEventBuilder eventBuilder, Object source, Object data) {
        run(action, eventBuilder, source, data, null);
    }

    /**
     * Run action without source and params
     */
    public static void run(Action action, ActionEventBuilder eventBuilder) {
        run(action, eventBuilder, null, null);
    }

    public static void run(Action action, ActionEventBuilder eventBuilder, Object source) {
        run(action, eventBuilder, source, null);
    }

    /**
     * Run action without event builder
     */
    public static void run(Action action, Object data) {
        run(action, (source, params) -> new ActionEvent(data, source, params));
    }

    /**
     * Render action using {@link ActionRenderer}.render() method and call {@link ActionLifecycleAware} before and after render
     *
     * @param renderer
     * @param action
     * @param eventBuilder
     * @param <T>
     * @return action component
     */
    public static <T> T render(ActionRenderer<T> renderer, Action action, ActionEventBuilder eventBuilder) {

        if (action instanceof ActionLifecycleAware ala) {
            ala.beforeRenderer(renderer);
        }

        T component = renderer.render(action, eventBuilder);

        if (action instanceof ActionLifecycleAware ala) {
            ala.afterRenderer(renderer, component);
        }

        action.setAttribute(ACTION_COMPONENT, component);

        return component;
    }

    /**
     * Call this method after action is rendered to get the last rendered action component
     *
     * @param action
     * @return
     */
    public static Object getActionComponent(Action action) {
        return action.getAttribute(ACTION_COMPONENT);
    }

    /**
     * Execute an action using {@link ActionExecutionRequest} instead of {@link ActionEvent}
     *
     * @param action
     * @param request
     * @return
     */
    public static ActionExecutionResponse execute(Action action, ActionExecutionRequest request) {

        if (action instanceof ActionSelfFilter filter) {
            filter.beforeActionExecution(request);
        }

        fireBeforeActionFilter(action, request);

        var response = action.execute(request);

        if (action instanceof ActionSelfFilter filter) {
            filter.afterActionExecution(request, response);
        }

        fireAfterActionFilter(action, request, response);

        return response;
    }

    /**
     * Execute filters
     *
     * @param filterConsumer
     */
    public static void forEachActionFilter(Consumer<ActionFilter> filterConsumer) {
        Containers.get().findObjects(ActionFilter.class).forEach(filterConsumer);
    }

    /**
     * Execute filters
     *
     * @param action
     * @param request
     */
    public static void fireBeforeActionFilter(Action action, ActionExecutionRequest request) {
        if (action != null && request != null) {
            forEachActionFilter(f -> f.beforeActionExecution(action, request));
        }
    }

    /**
     * Execute filters
     *
     * @param action
     * @param evt
     */
    public static void fireBeforeActionFilter(Action action, ActionEvent evt) {
        if (action != null && evt != null) {
            forEachActionFilter(f -> f.beforeActionPerformed(action, evt));
        }
    }

    /**
     * Execute filters
     *
     * @param action
     * @param request
     * @param response
     */
    public static void fireAfterActionFilter(Action action, ActionExecutionRequest request, ActionExecutionResponse response) {
        if (action != null && request != null && response != null) {
            forEachActionFilter(f -> f.afterActionExecution(action, request, response));
        }
    }

    /**
     * Execute filters
     *
     * @param action
     * @param evt
     */
    public static void fireAfterActionFilter(Action action, ActionEvent evt) {
        if (action != null && evt != null) {
            forEachActionFilter(f -> f.afterActionPerformed(action, evt));
        }
    }
}
