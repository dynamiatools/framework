package tools.dynamia.actions;

import tools.dynamia.commons.MapBuilder;
import tools.dynamia.integration.Containers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Utility class for executing, rendering, and managing {@link Action} instances and their events.
 * <p>
 * Provides static helper methods to run actions, render UI components, execute actions with requests,
 * and manage action filters and lifecycle events. Integrates with the framework's dependency injection
 * and event system for flexible action handling.
 * </p>
 * <p>
 * <b>Example usage:</b>
 * <pre>
 *     // Run an action with custom parameters
 *     Actions.run(myAction, myEventBuilder, source, data, params);
 *
 *     // Render an action as a UI component
 *     Button button = Actions.render(myRenderer, myAction, myEventBuilder);
 *
 *     // Execute an action using an ActionExecutionRequest
 *     ActionExecutionResponse response = Actions.execute(myAction, myRequest);
 * </pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public class Actions {

    private static final String ACTION_PARAM = "action";
    private static final String ACTION_COMPONENT = "ACTION_COMPONENT";

    /**
     * Runs the specified {@link Action} using the provided {@link ActionEventBuilder}, source, data, and parameters.
     * <p>
     * Handles action filters, lifecycle events, and delegates execution to the configured {@link ActionRunner}.
     * </p>
     *
     * @param action the action to execute
     * @param eventBuilder the event builder for creating the action event
     * @param source the source object for the event
     * @param data the data to associate with the event
     * @param params additional parameters for the event
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

    /**
     * Runs the specified {@link Action} with the provided event builder, source, and parameters.
     *
     * @param action the action to execute
     * @param eventBuilder the event builder
     * @param source the source object
     * @param params additional parameters
     */
    public static void run(Action action, ActionEventBuilder eventBuilder, Object source, Map<String, Object> params) {
        run(action, eventBuilder, source, null, params);
    }

    /**
     * Runs the specified {@link Action} with the provided event builder, source, and data.
     *
     * @param action the action to execute
     * @param eventBuilder the event builder
     * @param source the source object
     * @param data the data to associate with the event
     */
    public static void run(Action action, ActionEventBuilder eventBuilder, Object source, Object data) {
        run(action, eventBuilder, source, data, null);
    }

    /**
     * Runs the specified {@link Action} with the provided event builder, without source or parameters.
     *
     * @param action the action to execute
     * @param eventBuilder the event builder
     */
    public static void run(Action action, ActionEventBuilder eventBuilder) {
        run(action, eventBuilder, null, null);
    }

    /**
     * Runs the specified {@link Action} with the provided event builder and source.
     *
     * @param action the action to execute
     * @param eventBuilder the event builder
     * @param source the source object
     */
    public static void run(Action action, ActionEventBuilder eventBuilder, Object source) {
        run(action, eventBuilder, source, null);
    }

    /**
     * Runs the specified {@link Action} with the given data, using a default event builder.
     *
     * @param action the action to execute
     * @param data the data to associate with the event
     */
    public static void run(Action action, Object data) {
        run(action, (source, params) -> new ActionEvent(data, source, params));
    }

    /**
     * Renders the specified {@link Action} as a UI component using the given {@link ActionRenderer} and {@link ActionEventBuilder}.
     * <p>
     * Calls lifecycle hooks before and after rendering, and stores the rendered component as an attribute of the action.
     * </p>
     *
     * @param renderer the action renderer
     * @param action the action to render
     * @param eventBuilder the event builder
     * @param <T> the type of UI component
     * @return the rendered UI component
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
     * Returns the last rendered UI component for the specified {@link Action}.
     *
     * @param action the action
     * @return the last rendered component, or null if not rendered
     */
    public static Object getActionComponent(Action action) {
        return action.getAttribute(ACTION_COMPONENT);
    }

    /**
     * Executes the specified {@link Action} using an {@link ActionExecutionRequest} and returns the response.
     * <p>
     * Handles action filters and lifecycle events before and after execution.
     * </p>
     *
     * @param action the action to execute
     * @param request the execution request
     * @return the execution response
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
     * Iterates over all registered {@link ActionFilter} instances and applies the given consumer.
     *
     * @param filterConsumer the consumer to apply to each filter
     */
    public static void forEachActionFilter(Consumer<ActionFilter> filterConsumer) {
        Containers.get().findObjects(ActionFilter.class).forEach(filterConsumer);
    }

    /**
     * Executes all registered {@link ActionFilter} instances before an action execution request.
     *
     * @param action the action
     * @param request the execution request
     */
    public static void fireBeforeActionFilter(Action action, ActionExecutionRequest request) {
        if (action != null && request != null) {
            forEachActionFilter(f -> f.beforeActionExecution(action, request));
        }
    }

    /**
     * Executes all registered {@link ActionFilter} instances before an action event is performed.
     *
     * @param action the action
     * @param evt the action event
     */
    public static void fireBeforeActionFilter(Action action, ActionEvent evt) {
        if (action != null && evt != null) {
            forEachActionFilter(f -> f.beforeActionPerformed(action, evt));
        }
    }

    /**
     * Executes all registered {@link ActionFilter} instances after an action execution request.
     *
     * @param action the action
     * @param request the execution request
     * @param response the execution response
     */
    public static void fireAfterActionFilter(Action action, ActionExecutionRequest request, ActionExecutionResponse response) {
        if (action != null && request != null && response != null) {
            forEachActionFilter(f -> f.afterActionExecution(action, request, response));
        }
    }

    /**
     * Executes all registered {@link ActionFilter} instances after an action event is performed.
     *
     * @param action the action
     * @param evt the action event
     */
    public static void fireAfterActionFilter(Action action, ActionEvent evt) {
        if (action != null && evt != null) {
            forEachActionFilter(f -> f.afterActionPerformed(action, evt));
        }
    }
}
