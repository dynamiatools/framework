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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Load actions and control access using {@link ActionRestriction}
 *
 * @param <T>
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

    public void setActionAttributes(Map<String, Object> actionAttributes) {
        this.actionAttributes = actionAttributes;
    }

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

    public List<Action> getActionsReferences(ObjectMatcher<T> matcher) {
        Collection<T> allActions = Containers.get().findObjects(targetActionClass, matcher);
        List<Action> actions = new ArrayList<>();
        for (Action action : allActions) {
            actions.add(action);
            configureAttributes(action);
        }
        return actions;

    }

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

    public List<T> load() {
        return load(null);
    }

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


    public boolean isIgnoreRestrictions() {
        return ignoreRestrictions;
    }

    public void setIgnoreRestrictions(boolean ignoreRestrictions) {
        this.ignoreRestrictions = ignoreRestrictions;
    }

    public static List<Action> loadActionCommands(Object object) {
        List<Action> actionsCommands = new ArrayList<>();

        if (object != null) {
            Method[] methods = BeanUtils.getMethodsWithAnnotation(object.getClass(), ActionCommand.class);
            final var localizer = findDefaultLocalizedMessagesProvider();
            for (Method method : methods) {
                ActionCommand actionCommand = method.getAnnotation(ActionCommand.class);
                FastAction action = new FastAction(actionCommand.name(), actionCommand.image(),
                        actionCommand.description(), null, evt -> invokeActionCommand(object, method, evt));

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

    public static <T extends Action> T findActionById(Class<T> actionType, String actionId) {
        return Containers.get().findObjects(actionType)
                .stream().filter(a -> a.getId().equals(actionId))
                .findFirst()
                .orElse(null);
    }

    public boolean isAutolocalize() {
        return autolocalize;
    }

    public void setAutolocalize(boolean autolocalize) {
        this.autolocalize = autolocalize;
    }

    private static LocalizedMessagesProvider findDefaultLocalizedMessagesProvider() {
        return Containers.get().findObjects(LocalizedMessagesProvider.class)
                .stream().min(Comparator.comparingInt(LocalizedMessagesProvider::getPriority))
                .orElse(null);
    }
}
