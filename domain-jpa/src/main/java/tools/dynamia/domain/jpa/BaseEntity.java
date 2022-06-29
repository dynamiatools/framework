/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import com.fasterxml.jackson.annotation.JsonFormat;
import tools.dynamia.domain.Auditable;

import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;


/**
 * The Class BaseEntity.
 *
 * @author Ing. Mario Serrano Leones
 */
@MappedSuperclass
public abstract class BaseEntity extends SimpleEntity implements Serializable, Auditable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The creation date.
     */
    @Temporal(javax.persistence.TemporalType.DATE)
    @JsonFormat(pattern = "dd-MM-yyyy")
    private Date creationDate = new Date();

    /**
     * The creation time.
     */
    @Temporal(javax.persistence.TemporalType.TIME)
    @JsonFormat(pattern = "hh:mm:ss")
    private Date creationTime = new Date();


    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTimestamp = new Date();
    /**
     * The creator.
     */
    private String creator;

    /**
     * The last update.
     */
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastUpdate = new Date();

    /**
     * The last updater.
     */
    private String lastUpdater;

    /*
     * (non-Javadoc)
     *
     * @see Auditable#getCreationDate()
     */
    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    /*
     * (non-Javadoc)
     *
     * @see Auditable#getCreationTime()
     */
    @Override
    public Date getCreationTime() {
        return creationTime;
    }

    /*
     * (non-Javadoc)
     *
     * @see Auditable#setCreationDate(java.util.Date)
     */
    @Override
    public void setCreationDate(Date creationDate) {
        Date oldCreationDate = this.creationDate;
        this.creationDate = creationDate;
        this.creationTimestamp = creationDate;
        notifyChange("creationDate", oldCreationDate, creationDate);
    }

    /*
     * (non-Javadoc)
     *
     * @see Auditable#setCreationTime(java.util.Date)
     */
    @Override
    public void setCreationTime(Date creationTime) {
        Date oldCreationTime = this.creationTime;
        this.creationTime = creationTime;
        notifyChange("creationTime", oldCreationTime, creationTime);
    }

    /*
     * (non-Javadoc)
     *
     * @see Auditable#getCreator()
     */
    @Override
    public String getCreator() {
        return creator;
    }

    /*
     * (non-Javadoc)
     *
     * @see Auditable#setCreator(java.lang.String)
     */
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

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }
}
