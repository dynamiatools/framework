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

import java.util.List;

public interface NotificationRepository {

    /**
     * Save user notification somewhere
     *
     * @param notification
     */
    void save(INotification notification);

    /**
     * Retrive a single user notification by uuid
     *
     * @param uuid
     * @return
     */
    INotification getByUuid(String uuid);

    /**
     * Return all non-readed notifications
     *
     * @return
     */
    List<INotification> getNotifications();

    /**
     * Return all non-readed notification by source
     *
     * @param source
     * @return
     */
    List<INotification> getNotifications(String source);

    /**
     * Return non-readed notification count
     *
     * @return
     */
    long getNotificationCount();
}
