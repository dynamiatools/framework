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
import tools.dynamia.domain.Auditable2;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@MappedSuperclass
public abstract class BaseEntityUuid2 extends SimpleEntityUuid implements Serializable, Auditable2 {

    /**
     *
     */
    private static final long serialVersionUID = 1047814464634611550L;

    /**
     * The creation date.
     */

    private LocalDate creationDate = LocalDate.now();

    /**
     * The creation time.
     */
    private LocalTime creationTime = LocalTime.now();

    /**
     * The creator.
     */
    private String creator;

    /**
     * The last update.
     */
    private LocalDateTime lastUpdate = LocalDateTime.now();

    /**
     * The last updater.
     */
    private String lastUpdater;

    @Override
    public LocalDate getCreationDate() {
        return creationDate;
    }

    @Override
    public void setCreationDate(LocalDate creationDate) {
        notifyChange("creationDate", this.creationDate, creationDate);
        this.creationDate = creationDate;
    }

    @Override
    public LocalTime getCreationTime() {
        return creationTime;
    }

    @Override
    public void setCreationTime(LocalTime creationTime) {
        notifyChange("creationTime", this.creationTime, creationTime);
        this.creationTime = creationTime;
    }

    @Override
    public String getCreator() {
        return creator;
    }

    @Override
    public void setCreator(String creator) {
        notifyChange("creator", this.creator, creator);
        this.creator = creator;
    }

    @Override
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public void setLastUpdate(LocalDateTime lastUpdate) {
        notifyChange("lastUpdate", this.lastUpdate, lastUpdate);
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String getLastUpdater() {
        return lastUpdater;
    }

    @Override
    public void setLastUpdater(String lastUpdater) {
        notifyChange("lastUpdater", this.lastUpdater, lastUpdater);
        this.lastUpdater = lastUpdater;
    }

}
