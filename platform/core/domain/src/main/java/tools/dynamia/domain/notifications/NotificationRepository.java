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

package tools.dynamia.domain.notifications;

import java.util.List;

/**
 * Repository interface for managing user notifications persistence and retrieval.
 * <p>
 * This interface defines the contract for storing, querying, and managing user notifications in the system.
 * Implementations are responsible for the persistence layer operations related to notifications, such as
 * saving new notifications, retrieving unread notifications, counting pending notifications, and checking
 * for new notification availability.
 * </p>
 *
 * <p>
 * <b>Key features:</b>
 * <ul>
 *   <li>Persist and retrieve user notifications</li>
 *   <li>Query unread notifications globally or by source</li>
 *   <li>Count pending notifications efficiently</li>
 *   <li>Check for new notifications availability</li>
 *   <li>Retrieve notifications by unique identifier (UUID)</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * @Component
 * public class JpaNotificationRepository implements NotificationRepository {
 *
 *     @Autowired
 *     private EntityManager em;
 *
 *     @Override
 *     public void save(INotification notification) {
 *         em.persist(notification);
 *     }
 *
 *     @Override
 *     public List<INotification> getNotifications() {
 *         return em.createQuery(
 *             "SELECT n FROM Notification n WHERE n.read = false ORDER BY n.createdAt DESC",
 *             INotification.class
 *         ).getResultList();
 *     }
 *
 *     @Override
 *     public long getNotificationCount() {
 *         return em.createQuery(
 *             "SELECT COUNT(n) FROM Notification n WHERE n.read = false",
 *             Long.class
 *         ).getSingleResult();
 *     }
 *
 *     // Other methods...
 * }
 * }</pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 * @see INotification
 * @see NotificationListener
 */
public interface NotificationRepository {

    /**
     * Saves a user notification to the underlying persistence store.
     * <p>
     * This method persists a new notification or updates an existing one. Implementations should
     * handle the persistence logic appropriate to their storage mechanism (database, cache, file, etc.).
     * </p>
     *
     * @param notification the notification to save
     */
    void save(INotification notification);

    /**
     * Retrieves a single user notification by its unique identifier (UUID).
     * <p>
     * The UUID serves as a unique identifier for notifications across the system, allowing for
     * direct notification retrieval without exposing internal database IDs.
     * </p>
     *
     * @param uuid the unique identifier of the notification
     * @return the notification with the specified UUID, or {@code null} if not found
     */
    INotification getByUuid(String uuid);

    /**
     * Returns all unread notifications for the current user or context.
     * <p>
     * This method retrieves all notifications that have not been marked as read. Implementations
     * should consider user context, permissions, and ordering (typically newest first).
     * </p>
     *
     * @return list of unread notifications, empty list if none exist
     */
    List<INotification> getNotifications();

    /**
     * Returns all unread notifications filtered by source.
     * <p>
     * Allows querying notifications from a specific source or module, useful for displaying
     * notifications grouped by origin (e.g., "orders", "messages", "system").
     * </p>
     *
     * @param source the source identifier to filter notifications
     * @return list of unread notifications from the specified source, empty list if none exist
     */
    List<INotification> getNotifications(String source);

    /**
     * Returns the count of unread notifications.
     * <p>
     * This method provides an efficient way to get the total number of pending notifications
     * without retrieving the full notification objects. Commonly used for displaying notification
     * badges or counters in the UI.
     * </p>
     *
     * @return the number of unread notifications
     */
    long getNotificationCount();

    /**
     * Checks whether there are any new notifications available.
     * <p>
     * This is a lightweight check that can be used for polling or determining whether to
     * refresh notification displays. More efficient than calling {@link #getNotificationCount()}
     * when only a boolean result is needed.
     * </p>
     *
     * @return {@code true} if there are new unread notifications, {@code false} otherwise
     */
    boolean hasNew();
}
