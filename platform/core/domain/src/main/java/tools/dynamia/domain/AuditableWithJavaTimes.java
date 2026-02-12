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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Interface for entities that require audit tracking using Java 8+ date/time types.
 * <p>
 * This interface extends the auditing capabilities by separating date and time components using
 * {@link LocalDate} and {@link LocalTime} instead of legacy {@link java.util.Date}. It tracks
 * when entities are created and last modified, along with optional user information.
 * </p>
 *
 * <p>
 * <b>Key features:</b>
 * <ul>
 *   <li>Tracks creation and last update timestamps using modern Java time API</li>
 *   <li>Separates date and time components for flexible querying and display</li>
 *   <li>Optional tracking of user who created or last modified the entity</li>
 *   <li>Provides convenient method to get combined creation date-time</li>
 *   <li>Improves upon legacy {@link Auditable} interface with modern types</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * @Entity
 * public class Document implements AuditableWithJavaTimes {
 *
 *     @Column
 *     private LocalDate creationDate;
 *
 *     @Column
 *     private LocalTime creationTime;
 *
 *     @Column
 *     private LocalDate lastUpdate;
 *
 *     @Column
 *     private String creator;
 *
 *     // Implement getters and setters
 *
 *     @PrePersist
 *     protected void onCreate() {
 *         setCreationDate(LocalDate.now());
 *         setCreationTime(LocalTime.now());
 *     }
 *
 *     @PreUpdate
 *     protected void onUpdate() {
 *         setLastUpdate(LocalDate.now());
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 * @see Auditable
 * @see LocalDate
 * @see LocalTime
 * @see LocalDateTime
 */
public interface AuditableWithJavaTimes {

    /**
     * Gets the creation date.
     *
     * @return the creation date
     */
    LocalDate getCreationDate();

    /**
     * Gets the creation time.
     *
     * @return the creation time
     */
    LocalTime getCreationTime();

    /**
     * Sets the creation date.
     *
     * @param creationDate the new creation date
     */
    void setCreationDate(LocalDate creationDate);

    /**
     * Sets the creation time.
     *
     * @param creationTime the new creation time
     */
    void setCreationTime(LocalTime creationTime);

    /**
     * Gets the creator.
     *
     * @return the creator
     */
    String getCreator();

    /**
     * Sets the creator.
     *
     * @param creator the new creator
     */
    void setCreator(String creator);

    /**
     * Gets the last update.
     *
     * @return the last update
     */
    LocalDateTime getLastUpdate();

    /**
     * Sets the last update.
     *
     * @param lastUpdate the new last update
     */
    void setLastUpdate(LocalDateTime lastUpdate);

    /**
     * Gets the last updater.
     *
     * @return the last updater
     */
    String getLastUpdater();

    /**
     * Sets the last updater.
     *
     * @param lastUpdater the new last updater
     */
    void setLastUpdater(String lastUpdater);

}
