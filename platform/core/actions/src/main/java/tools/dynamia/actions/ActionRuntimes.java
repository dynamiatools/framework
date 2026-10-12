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

import java.util.Arrays;

/**
 * Finds the {@link ActionRuntime} of an action: what its concrete class declares with {@link RunsOn} (never inherited),
 * otherwise what its type implies.
 * <p>
 * Only types whose contract already says how they run are derived: {@link FlowRemoteAction} is {@code FLOW},
 * {@link RemoteAction} is {@code REMOTE}. The deprecated {@link HeadlessCapable} counts as {@code HEADLESS} only when the
 * concrete class lists it in its own {@code implements}; a subclass of a headless action is {@code UNDECLARED} until
 * someone reviews it and declares it.
 */
public final class ActionRuntimes {

    private ActionRuntimes() {
    }

    /**
     * @param action the action
     * @return its runtime
     */
    public static ActionRuntime of(Action action) {
        return of(action.getClass(), action);
    }

    /**
     * @param actionClass the class of an action
     * @param action      an instance, used to ask {@link HeadlessCapable#headlessSupported()}; may be {@code null}
     * @return its runtime
     */
    @SuppressWarnings("deprecation")
    public static ActionRuntime of(Class<?> actionClass, Action action) {
        RunsOn declared = actionClass.getDeclaredAnnotation(RunsOn.class);
        if (declared != null) {
            return declared.value();
        }
        if (FlowRemoteAction.class.isAssignableFrom(actionClass)) {
            return ActionRuntime.FLOW;
        }
        if (RemoteAction.class.isAssignableFrom(actionClass)) {
            return ActionRuntime.REMOTE;
        }
        if (Arrays.asList(actionClass.getInterfaces()).contains(HeadlessCapable.class)) {
            boolean supported = !(action instanceof HeadlessCapable capable) || capable.headlessSupported();
            return supported ? ActionRuntime.HEADLESS : ActionRuntime.UNDECLARED;
        }
        return ActionRuntime.UNDECLARED;
    }

    /**
     * @param actionClass a class of an action
     * @return the id a remote client uses for a local action served headless: the class name without the
     * {@code Action} suffix, in lower case ({@code DeleteAction} becomes {@code delete})
     */
    public static String headlessId(Class<?> actionClass) {
        var name = actionClass.getSimpleName();
        if (name.endsWith("Action") && name.length() > "Action".length()) {
            name = name.substring(0, name.length() - "Action".length());
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
