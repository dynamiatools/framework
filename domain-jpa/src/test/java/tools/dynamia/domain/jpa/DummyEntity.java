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

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tools.dynamia.domain.query.QueryParameters;

import java.util.List;

import static tools.dynamia.domain.EntityHandler.handle;

@Entity
@Table(name = "test_DummyEntity")
public class DummyEntity extends BaseEntity {

    private String name;


    public DummyEntity() {

    }

    public DummyEntity(String name) {
        super();
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<DummyEntity> findByName(String name) {
        return handle(DummyEntity.class).find(QueryParameters.with("name", name));
    }

}
