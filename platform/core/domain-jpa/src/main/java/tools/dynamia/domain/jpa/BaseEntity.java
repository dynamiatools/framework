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

import jakarta.persistence.MappedSuperclass;
import tools.dynamia.domain.Auditable;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * The Class BaseEntity.
 *
 * @author Ing. Mario Serrano Leones
 */
@MappedSuperclass
public abstract class BaseEntity extends BaseEntityWithJavaTimes implements Serializable, Auditable {


    public void resetCreationDate() {
        resetCreationDate(LocalDateTime.now());
    }

    /**
     * Reset creation date.
     *
     * @param date the date
     */
    public void resetCreationDate(LocalDateTime date) {
        if (date == null) date = LocalDateTime.now();
        setCreationDate(date.toLocalDate());
        setCreationTime(date.toLocalTime());
        setCreationTimestamp(date);
    }

}
