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
package tools.dynamia.domain.neo4j;

import tools.dynamia.domain.Auditable;

import java.io.Serializable;
import java.util.Date;


/**
 * The Class NodeBaseEntity.
 *
 * @author Ing. Mario Serrano Leones
 */

public abstract class NodeBaseEntity extends NodeSimpleEntity implements Serializable, Auditable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;


    private Date creationDate = new Date();


    private Date creationTime = new Date();

    private String creator;

    private Date lastUpdate = new Date();

    private String lastUpdater;

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public Date getCreationTime() {
        return creationTime;
    }


    @Override
    public void setCreationDate(Date creationDate) {
        Date oldCreationDate = this.creationDate;
        this.creationDate = creationDate;
        notifyChange("creationDate", oldCreationDate, creationDate);
    }


    @Override
    public void setCreationTime(Date creationTime) {
        Date oldCreationTime = this.creationTime;
        this.creationTime = creationTime;
        notifyChange("creationTime", oldCreationTime, creationTime);
    }

    @Override
    public String getCreator() {
        return creator;
    }


    @Override
    public void setCreator(String creator) {
        String oldCreator = this.creator;
        this.creator = creator;
        notifyChange("creator", oldCreator, creator);
    }

    /*
     * (non-Javadoc)
     *
     * @see Auditable#getLastUpdate()
     */
    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }

    /*
     * (non-Javadoc)
     *
     * @see Auditable#setLastUpdate(java.util.Date)
     */
    @Override
    public void setLastUpdate(Date lastUpdate) {
        Date oldLastUpdate = this.lastUpdate;
        this.lastUpdate = lastUpdate;
        notifyChange("lastUpdate", oldLastUpdate, lastUpdate);
    }

    /*
     * (non-Javadoc)
     *
     * @see Auditable#getLastUpdater()
     */
    @Override
    public String getLastUpdater() {
        return lastUpdater;
    }

    /*
     * (non-Javadoc)
     *
     * @see Auditable#setLastUpdater(java.lang.String)
     */
    @Override
    public void setLastUpdater(String lastUpdater) {
        String oldLastUpdater = this.lastUpdater;
        this.lastUpdater = lastUpdater;
        notifyChange("lastUpdater", oldLastUpdater, lastUpdater);
    }
}
