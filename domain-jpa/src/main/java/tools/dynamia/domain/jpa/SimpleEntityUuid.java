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
package tools.dynamia.domain.jpa;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.domain.IdGenerators;

import java.io.Serializable;

@MappedSuperclass
public abstract class SimpleEntityUuid extends AbstractEntity<String> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1047814464634611550L;

    @Id
    private String id = IdGenerators.createId(String.class);



    /**
     * The creation date.
     */
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        notifyChange("id", this.id, id);
        this.id = id;
    }

}
