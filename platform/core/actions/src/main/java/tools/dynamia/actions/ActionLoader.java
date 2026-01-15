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
package tools.dynamia.actions;

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.LocalizedMessagesProvider;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.ObjectMatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Utility class for loading, configuring, and controlling access to {@link Action} instances.
 * <p>
 * The {@code ActionLoader} provides methods to discover actions, apply restrictions, configure attributes,
 * and support localization. It integrates with the framework's dependency injection and event system.
 * </p>
 * <p>
 * <b>Example usage:</b>
 * <pre>
 *     // Load all allowed actions of a specific type
 *     ActionLoader<MyAction> loader = new ActionLoader<>(MyAction.class);
 *     List<MyAction> actions = loader.load();
 *
 *     // Load actions with a custom matcher
 *     List<MyAction> filtered = loader.load(myMatcher);
 *
 *     // Load action commands from an object
 *     List<Action> commands = ActionLoader.loadActionCommands(myObject);
 * </pre>
 * </p>
 *
 * @param <T> the type of Action to load
 * @author Mario A. Serrano Leones
 */
public class ActionLoader<T extends Action> {

    private final Class<T> targetActionClass;
    private Map<String, Object> actionAttributes = null;
    private boolean ignoreRestrictions;

    private boolean autolocalize = true;


    public ActionLoader(Class<T> targetClass) {
        super();
        this.targetActionClass = targetClass;
    }

    /**
     * Sets custom attributes for actions loaded by this loader.
     * <p>
     * The attributes map should use the action ID as key and a map of properties as value.
     * These properties will be applied to each action after loading.
     * </p>
     *
     * @param actionAttributes a map of action IDs to property maps
     */
    public void setActionAttributes(Map<String, Object> actionAttributes) {
        this.actionAttributes = actionAttributes;
    }

    /**
     * Loads all actions matching the given matcher, applies restrictions, configures attributes, and localizes them if needed.
     * <p>
     * Only actions allowed by restrictions are included. Attributes and localization are applied after loading.
     * Lifecycle hooks are called for each action.
     * </p>
     *
     * @param matcher an ObjectMatcher to filter actions (can be null for all)
     * @return a list of allowed and configured actions
     */
    public List<T> load(ObjectMatcher<T> matcher) {
        final var localizer = findDefaultLocalizedMessagesProvider();
        Collection<T> allActions = Containers.get().findObjects(targetActionClass, matcher);
        List<T> actions = new ArrayList<>();
        for (T action : allActions) {
            if (isActionAllowed(action)) {
                actions.add(action);
                configureAttributes(action);
                if (isAutolocalize() && action instanceof AbstractAction) {
                    ((AbstractAction) action).setLocalizedMessagesProvider(localizer);
                }

                if (action instanceof ActionLifecycleAware ala) {
                    ala.onCreate();
                }
            }
        }
        actions.sort(new ActionComparator());
        return actions;
    }

    /**
     * Loads all actions matching the given matcher and returns them as Action references.
     * <p>
     * Attributes are configured for each action, but restrictions are not checked.
     * </p>
     *
     * @param matcher an ObjectMatcher to filter actions
     * @return a list of Action references
     */
    public List<Action> getActionsReferences(ObjectMatcher<T> matcher) {
        Collection<T> allActions = Containers.get().findObjects(targetActionClass, matcher);
        List<Action> actions = new ArrayList<>();
        for (Action action : allActions) {
            actions.add(action);
            configureAttributes(action);
        }
        return actions;
    }

    /**
     * Configures custom attributes for the given action using the attributes map.
     * <p>
     * Properties are set using BeanUtils and additional attributes are merged into the action's attribute map.
     * </p>
     *
     * @param action the action to configure
     */
    @SuppressWarnings("unchecked")
    private void configureAttributes(Action action) {
        if (actionAttributes != null) {
            @SuppressWarnings("unchecked") Map<String, Object> params = (Map<String, Object>) actionAttributes.get(action.getId());
            if (params != null) {
                BeanUtils.setupBean(action, params);
                if (params.get("attributes") != null && params.get("attributes") instanceof Map) {
                    //noinspection unchecked
                    action.getAttributes().putAll((Map) params.get("attributes"));
                }
            }
        }

    }

    /**
     * Loads all actions allowed by restrictions and configuration.
     * <p>
     * Equivalent to {@code load(null)}.
     * </p>
     *
     * @return a list of allowed and configured actions
     */
    public List<T> load() {
        return load(null);
    }

    /**
     * Checks if the given action is allowed according to restrictions.
     * <p>
     * If restrictions are ignored, always returns true. Otherwise, checks all registered restrictions.
     * </p>
     *
     * @param action the action to check
     * @return true if allowed, false otherwise
     */
    public boolean isActionAllowed(Action action) {
        if (isIgnoreRestrictions()) {
            return true;
        }

        Boolean allowed = ActionRestrictions.allowAccess(action);
        if (allowed == null) {
            allowed = true;
        }
        return allowed;
    }

    /**
     * Returns whether restrictions are ignored when loading actions.
     *
     * @return true if restrictions are ignored, false otherwise
     */
    public boolean isIgnoreRestrictions() {
        return ignoreRestrictions;
    }

    /**
     * Sets whether restrictions should be ignored when loading actions.
     *
     * @param ignoreRestrictions true to ignore restrictions, false otherwise
     */
    public void setIgnoreRestrictions(boolean ignoreRestrictions) {
        this.ignoreRestrictions = ignoreRestrictions;
    }

    /**
     * Loads all {@link ActionCommand}-annotated methods from the given object as FastAction instances.
     * <p>
     * Each method is wrapped as a FastAction, with localization and renderer applied as needed.
     * </p>
     *
     * @param object the object containing ActionCommand methods
     * @return a list of FastAction instances
     */
    public static List<Action> loadActionCommands(Object object) {
        List<Action> actionsCommands = new ArrayList<>();

        if (object != null) {
            Method[] methods = BeanUtils.getMethodsWithAnnotation(object.getClass(), ActionCommand.class);
            final var localizer = findDefaultLocalizedMessagesProvider();
            for (Method method : methods) {
                ActionCommand actionCommand = method.getAnnotation(ActionCommand.class);
                FastAction action = new FastAction(actionCommand.name())
                        .image(actionCommand.image())
                        .type(actionCommand.type())
                        .description(actionCommand.description())
                        .onActionPerfomed(evt -> invokeActionCommand(object, method, evt));


                if (actionCommand.name().isEmpty()) {
                    action.setName(method.getName());
                }

                if (actionCommand.autolocalize()) {
                    action.setLocalizedMessagesProvider(localizer);
                }

                action.setActionRendererClass(actionCommand.actionRenderer());
                actionsCommands.add(action);
            }
        }

        return actionsCommands;
    }

    /**
     * Invokes an ActionCommand method on the given object with the provided ActionEvent.
     * <p>
     * Supports methods with zero or one parameter (ActionEvent). Throws exception for invalid signatures.
     * </p>
     *
     * @param object the target object
     * @param method the method to invoke
     * @param evt    the ActionEvent to pass (if required)
     */
    private static void invokeActionCommand(Object object, Method method, ActionEvent evt) {
        try {
            method.setAccessible(true);
            switch (method.getParameterCount()) {
                case 0 -> method.invoke(object);
                case 1 -> method.invoke(object, evt);
                default -> throw new ActionLoaderException(
                        "Invalid ActionCommand " + method.getName() + " from " + object.getClass());
            }

        } catch (IllegalAccessException e) {
            throw new ActionLoaderException("ActionCommand method cannot be access, make sure is public method", e);
        } catch (IllegalArgumentException e) {
            throw new ActionLoaderException(
                    "ActionCommand has more than zero arguments and first is not ActionEvent type", e);
        } catch (InvocationTargetException e) {
            throw new ActionLoaderException(e.getCause().getMessage(), e);
        }

    }

    /**
     * Finds an action by its ID among all registered actions of the given type.
     * <p>
     * Returns the first matching action or null if not found.
     * </p>
     *
     * @param actionType the class of the action type
     * @param actionId   the ID of the action
     * @param <T>        the type of Action
     * @return the found action or null
     */
    public static <T extends Action> T findActionById(Class<T> actionType, String actionId) {
        return Containers.get().findObjects(actionType)
                .stream().filter(a -> a.getId().equals(actionId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns whether actions should be automatically localized after loading.
     *
     * @return true if autolocalize is enabled, false otherwise
     */
    public boolean isAutolocalize() {
        return autolocalize;
    }

    /**
     * Sets whether actions should be automatically localized after loading.
     *
     * @param autolocalize true to enable autolocalization, false otherwise
     */
    public void setAutolocalize(boolean autolocalize) {
        this.autolocalize = autolocalize;
    }

    /**
     * Finds the default {@link LocalizedMessagesProvider} with the highest priority.
     * <p>
     * Used for automatic localization of actions.
     * </p>
     *
     * @return the default LocalizedMessagesProvider or null if none found
     */
    private static LocalizedMessagesProvider findDefaultLocalizedMessagesProvider() {
        return Containers.get().findObjects(LocalizedMessagesProvider.class)
                .stream().min(Comparator.comparingInt(LocalizedMessagesProvider::getPriority))
                .orElse(null);
    }

    /**
     * Loads an action by its ID.
     *
     * @param actionId the ID of the action to load
     * @return an Optional containing the found action, or empty if not found
     */
    public Optional<T> loadById(String actionId) {
        return load(a -> a.getId().equals(actionId)).stream().findFirst();
    }

    /**
     * Loads an action by its reference.
     *
     * @param actionReference the ActionRef containing the ID of the action to load
     * @return an Optional containing the found action, or empty if not found
     */
    public Optional<T> loadByReference(ActionReference actionReference) {
        var action = loadById(actionReference.getId());
        action.ifPresent(a -> {
            a.config(actionReference);
        });

        return action;
    }
}
