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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import tools.dynamia.domain.Auditable;

import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
public abstract class BaseEntityUuid extends SimpleEntityUuid implements Serializable, Auditable {

    /**
     *
     */
    private static final long serialVersionUID = 1047814464634611550L;

    /**
     * The creation date.
     */
    @Temporal(TemporalType.DATE)
    private Date creationDate = new Date();

    /**
     * The creation time.
     */
    @Temporal(TemporalType.TIME)
    private Date creationTime = new Date();

    /**
     * The creator.
     */
    private String creator;

    /**
     * The last update.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate = new Date();

    /**
     * The last updater.
     */
    private String lastUpdater;

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public void setCreationDate(Date creationDate) {
        notifyChange("creationDate", this.creationDate, creationDate);
        this.creationDate = creationDate;
    }

    @Override
    public Date getCreationTime() {
        return creationTime;
    }

    @Override
    public void setCreationTime(Date creationTime) {
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
    public Date getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public void setLastUpdate(Date lastUpdate) {
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
