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

package tools.dynamia.domain.notifications;

import tools.dynamia.integration.Containers;

import java.util.Map;

/**
 * User Notifications class handler
 */
public class Notifications {

    private static final NotificationRepository DEFAULT = new InMemoryNotificationRepository();

    /**
     * Post a new Notification
     *
     * @param notification
     */
    public static void post(INotification notification) {
        var repository = findDefaultRepository();
        repository.save(notification);
        Containers.get().findObjects(NotificationListener.class).forEach(l -> l.notificationPosted(notification));
    }

    /**
     * Post a new notification with user id and message. It use a instance of {@link Notification}
     *
     * @param userId
     * @param message
     */
    public static void post(String userId, String message) {
        post(Notification.build().userId(userId)
                .message(message));
    }

    /**
     * Post a new notification with user id, message and tenant id. It use a instance of {@link Notification}
     *
     * @param userId
     * @param message
     * @param tenantId
     */
    public static void post(String userId, String message, String tenantId) {
        post(Notification.build().userId(userId)
                .message(message)
                .tenantId(tenantId));
    }

    /**
     * Post a new notification with user id, message, tenant id and content. It use a instance of {@link Notification}
     *
     * @param userId
     * @param message
     * @param tenantId
     * @param content
     */
    public static void post(String userId, String message, String tenantId, Map<String, Object> content) {
        post(Notification.build().userId(userId)
                .message(message)
                .tenantId(tenantId)
                .content(content));
    }

    /**
     * Find and return the current {@link NotificationRepository}. If nothing found an static instance of {@link InMemoryNotificationRepository}
     * is returned
     *
     * @return UserNotificationRepository
     */
    public static NotificationRepository findDefaultRepository() {
        var repo = Containers.get().findObject(NotificationRepository.class);
        if (repo == null) {
            repo = DEFAULT;
        }
        return repo;
    }
}
