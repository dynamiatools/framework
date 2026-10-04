
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

package tools.dynamia.modules.saas;

import org.springframework.stereotype.Component;
import tools.dynamia.integration.context.ContextCapturer;

/**
 * Carries the tenant bound with {@link AccountTenants} into async tasks started with {@code SchedulerUtil} (or any
 * {@code ObjectsContext.Snapshot}). Without it a task started inside {@code AccountTenants.runAs(5L, ...)} would not
 * see tenant {@code 5}: a scoped value is not inherited by other threads, and the task would resolve the root tenant.
 *
 * @author Mario Serrano Leones
 */
@Component
public class AccountTenantContextCapturer implements ContextCapturer {

    @Override
    public Binding capture() {
        Long tenantId = AccountTenants.forcedTenantId();
        if (tenantId == null) {
            return null;
        }
        return new Binding() {
            @Override
            public <T, X extends Throwable> T call(ScopedValue.CallableOp<? extends T, X> op) throws X {
                return AccountTenants.callBoundTo(tenantId, op);
            }
        };
    }
}
