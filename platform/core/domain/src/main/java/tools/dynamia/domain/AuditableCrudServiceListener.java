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
package tools.dynamia.domain;

import tools.dynamia.domain.util.CrudServiceListenerAdapter;
import tools.dynamia.integration.sterotypes.Listener;

import java.time.LocalDateTime;
import java.util.Date;

@Listener
public class AuditableCrudServiceListener extends CrudServiceListenerAdapter<Auditable> {

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * CrudServiceListenerAdapter#beforeUpdate(java
	 * .lang.Object)
     */
    @Override
    public void beforeUpdate(Auditable entity) {
        entity.setLastUpdate(LocalDateTime.now());
    }

    @Override
    public void beforeCreate(Auditable entity) {
        if (entity.getCreationDate() == null || entity.getCreationTime() == null) {
            var now = LocalDateTime.now();
            entity.setCreationDate(now.toLocalDate());
            entity.setCreationTime(now.toLocalTime());
        }
    }
}
