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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class InMemoryNotificationRepository implements NotificationRepository {

    private final List<INotification> data = new ArrayList<>();
    private boolean hasNew;

    @Override
    public void save(INotification notification) {
        data.add(notification);
        hasNew = true;

    }

    @Override
    public INotification getByUuid(String uuid) {
        return data.stream().filter(n -> n.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    @Override
    public List<INotification> getNotifications() {
        hasNew = false;
        return data.stream().filter(not(INotification::isReaded)).collect(Collectors.toList());
    }

    @Override
    public List<INotification> getNotifications(String source) {
        if (source == null || source.isBlank()) {
            return Collections.emptyList();
        }
        hasNew = false;
        return data.stream().filter(not(INotification::isReaded))
                .filter(n -> source.equals(n.getSource())).collect(Collectors.toList());

    }

    @Override
    public long getNotificationCount() {
        return data.stream().filter(not(INotification::isReaded)).count();
    }

    @Override
    public boolean hasNew() {
        return hasNew;
    }
}
