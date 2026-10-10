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
package tools.dynamia.ui;

import tools.dynamia.integration.context.ContextCapturer;
import tools.dynamia.integration.sterotypes.Component;

/**
 * Makes sure an async task started with {@code SchedulerUtil} (or any {@code ObjectsContext.Snapshot}) runs with no UI:
 * inside the task the active environment is {@link NoUIEnvironment}, whatever environment started it. A task has no user
 * in front, so a UI facade used there fails with {@link UIUnavailableException} instead of reaching for the widgets of
 * the thread that launched it. The task reports back through the callbacks of {@code UIProgress}, which run in the
 * environment that started the work.
 */
@Component
public class UIEnvironmentContextCapturer implements ContextCapturer {

    @Override
    public Binding capture() {
        return new Binding() {
            @Override
            public <T, X extends Throwable> T call(ScopedValue.CallableOp<? extends T, X> op) throws X {
                return ScopedValue.where(UIFacades.environmentBinding(), (UIEnvironment) NoUIEnvironment.INSTANCE).call(op);
            }
        };
    }
}
