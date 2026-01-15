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

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import tools.dynamia.commons.DateTimeUtils;
import tools.dynamia.commons.Messages;
import tools.dynamia.domain.Auditable;
import tools.dynamia.domain.Auditable2;

import java.io.Serializable;
import java.time.*;
import java.util.Date;


/**
 * The Class BaseEntity.
 *
 * @author Ing. Mario Serrano Leones
 */
@MappedSuperclass
public abstract class BaseEntity2 extends SimpleEntity implements Serializable, Auditable2 {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The creation date.
     */
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate creationDate = LocalDate.now();

    /**
     * The creation time.
     */
    @JsonFormat(pattern = "hh:mm:ss")
    private LocalTime creationTime = LocalTime.now();


    private LocalDateTime creationTimestamp = LocalDateTime.now();
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

    /*
     * (non-Javadoc)
     *
     * @see Auditable#getCreationDate()
     */
    @Override
    public LocalDate getCreationDate() {
        return creationDate;
    }

    /*
     * (non-Javadoc)
     *
     * @see Auditable#getCreationTime()
     */
    @Override
    public LocalTime getCreationTime() {
        return creationTime;
    }

    /*
     * (non-Javadoc)
     *
     * @see Auditable#setCreationDate(java.util.Date)
     */
    @Override
    public void setCreationDate(LocalDate creationDate) {
        LocalDate oldCreationDate = this.creationDate;
        this.creationDate = creationDate;
        notifyChange("creationDate", oldCreationDate, creationDate);
    }

    /*
     * (non-Javadoc)
     *
     * @see Auditable#setCreationTime(java.util.Date)
     */
    @Override
    public void setCreationTime(LocalTime creationTime) {
        LocalTime oldCreationTime = this.creationTime;
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
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    /*
     * (non-Javadoc)
     *
     * @see Auditable#setLastUpdate(java.util.Date)
     */
    @Override
    public void setLastUpdate(LocalDateTime lastUpdate) {
        LocalDateTime oldLastUpdate = this.lastUpdate;
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

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(LocalDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    @Transient
    public Instant getCreationInstant() {
        if (creationTimestamp != null) {
            return creationTimestamp.atZone(ZoneId.systemDefault()).toInstant();
        }
        return null;
    }

    @Transient
    public Instant getLastUpdateInstant() {
        if (lastUpdate != null) {
            return lastUpdate.atZone(ZoneId.systemDefault()).toInstant();
        }
        return null;
    }

    @Transient
    public ZonedDateTime getCreationDateZoned(ZoneId zone) {
        Instant instant = getCreationInstant();
        return instant != null ? instant.atZone(zone) : null;
    }

    @Transient
    public ZonedDateTime getCreationDateZoned() {
        return getCreationDateZoned(Messages.getDefaultTimeZone());
    }

    @Transient
    public ZonedDateTime getLastUpdateZoned(ZoneId zone) {
        Instant instant = getLastUpdateInstant();
        return instant != null ? instant.atZone(zone) : null;
    }

    @Transient
    public ZonedDateTime getLastUpdateZoned() {
        return getLastUpdateZoned(Messages.getDefaultTimeZone());
    }
}
