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

/**
 * Marker interface for domain entities that require comprehensive audit tracking capabilities.
 *
 * <p>This interface extends {@link AuditableWithJavaTimes} to provide audit functionality using
 * modern Java 8+ date and time types. Implementing this interface enables automatic tracking of
 * creation and modification metadata for entities, which is essential for maintaining data
 * integrity and compliance requirements.</p>
 *
 * <p><b>Key audit information tracked:</b></p>
 * <ul>
 *   <li><b>Creation date and time:</b> When the entity was first persisted</li>
 *   <li><b>Creator:</b> Username or identifier of the user who created the entity</li>
 *   <li><b>Last update timestamp:</b> When the entity was last modified</li>
 *   <li><b>Last updater:</b> Username or identifier of the user who last modified the entity</li>
 * </ul>
 *
 * <p><b>Benefits:</b></p>
 * <ul>
 *   <li>Automatic audit trail for compliance and regulatory requirements</li>
 *   <li>Simplified debugging by tracking when and who made changes</li>
 *   <li>Support for data versioning and historical analysis</li>
 *   <li>Integration with Dynamia Tools CRUD operations</li>
 *   <li>Uses modern Java time API for better date/time handling</li>
 * </ul>
 *
 * <p><b>Basic usage example:</b></p>
 * <pre>{@code
 * @Entity
 * @Table(name = "customers")
 * public class Customer implements Auditable {
 *
 *     @Id
 *     @GeneratedValue
 *     private Long id;
 *
 *     private String name;
 *     private String email;
 *
 *     // Audit fields
 *     private LocalDate creationDate;
 *     private LocalTime creationTime;
 *     private String creator;
 *     private LocalDateTime lastUpdate;
 *     private String lastUpdater;
 *
 *     // Getters and setters for all fields including audit fields
 *
 *     @PrePersist
 *     protected void onCreate() {
 *         creationDate = LocalDate.now();
 *         creationTime = LocalTime.now();
 *         creator = SecurityUtils.getCurrentUsername();
 *     }
 *
 *     @PreUpdate
 *     protected void onUpdate() {
 *         lastUpdate = LocalDateTime.now();
 *         lastUpdater = SecurityUtils.getCurrentUsername();
 *     }
 * }
 * }</pre>
 *
 * <p><b>Usage with JPA listeners:</b></p>
 * <pre>{@code
 * @Component
 * public class AuditListener {
 *
 *     @PrePersist
 *     public void setCreationInfo(Object entity) {
 *         if (entity instanceof Auditable auditable) {
 *             auditable.setCreationDate(LocalDate.now());
 *             auditable.setCreationTime(LocalTime.now());
 *             auditable.setCreator(getCurrentUser());
 *         }
 *     }
 *
 *     @PreUpdate
 *     public void setUpdateInfo(Object entity) {
 *         if (entity instanceof Auditable auditable) {
 *             auditable.setLastUpdate(LocalDateTime.now());
 *             auditable.setLastUpdater(getCurrentUser());
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p><b>Note:</b> This interface is a convenience marker that extends {@link AuditableWithJavaTimes}.
 * All method implementations are inherited from the parent interface. Use this interface in your
 * domain entities to leverage the framework's automatic audit tracking features.</p>
 *
 * @author Mario Serrano Leones
 * @see AuditableWithJavaTimes
 * @see java.time.LocalDate
 * @see java.time.LocalTime
 * @see java.time.LocalDateTime
 */
public interface Auditable extends AuditableWithJavaTimes {

}
