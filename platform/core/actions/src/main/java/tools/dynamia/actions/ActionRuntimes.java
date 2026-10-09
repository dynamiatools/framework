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

/**
 * Finds the {@link ActionRuntime} of an action: what it declares with {@link RunsOn}, otherwise what its type says.
 */
public final class ActionRuntimes {

    private ActionRuntimes() {
    }

    /**
     * @param action the action
     * @return its runtime; {@link ActionRuntime#UNDECLARED} for a local action that is not headless-capable and did not
     * declare one
     */
    public static ActionRuntime of(Action action) {
        return of(action.getClass(), action);
    }

    /**
     * @param actionClass the class of an action
     * @param action      an instance, used to ask {@link HeadlessCapable#headlessSupported()}; may be {@code null}
     * @return its runtime
     */
    public static ActionRuntime of(Class<?> actionClass, Action action) {
        RunsOn declared = actionClass.getAnnotation(RunsOn.class);
        if (declared != null) {
            return declared.value();
        }
        if (FlowRemoteAction.class.isAssignableFrom(actionClass)) {
            return ActionRuntime.FLOW;
        }
        if (RemoteAction.class.isAssignableFrom(actionClass)) {
            return ActionRuntime.REMOTE;
        }
        if (HeadlessCapable.class.isAssignableFrom(actionClass)) {
            boolean supported = !(action instanceof HeadlessCapable capable) || capable.headlessSupported();
            return supported ? ActionRuntime.HEADLESS : ActionRuntime.UNDECLARED;
        }
        return ActionRuntime.UNDECLARED;
    }
}
