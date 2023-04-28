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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple {@link INotification} implementation
 */
public class Notification implements INotification {

    private String uuid = UUID.randomUUID().toString();
    private String userId;
    private String source;
    private boolean readed;
    private NotificationType type = NotificationType.NORMAL;
    private String message;
    private String icon;
    private String tenantId;
    private Map<String, Object> content = new HashMap<>();

    public static Notification build() {
        return new Notification();
    }

    public Notification() {
    }

    public Notification(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public Notification source(String source) {
        this.source = source;
        return this;
    }

    public Notification userId(String userId) {
        this.userId = userId;
        return this;
    }


    public Notification message(String message) {
        this.message = message;
        return this;
    }

    public Notification tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public Notification content(Map<String, Object> content) {
        this.content = content;
        return this;
    }

    public Notification addContent(String key, Object value) {
        if (content == null) {
            content = new HashMap<>();
        }
        content.put(key, value);
        return this;
    }

    public Notification icon(String icon) {
        this.icon = icon;
        return this;
    }

    public Notification type(NotificationType type) {
        this.type = type;
        return this;
    }

    public Notification readed(boolean readed) {
        this.readed = readed;
        return this;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean isReaded() {
        return readed;
    }

    public void setReaded(boolean readed) {
        this.readed = readed;
    }

    @Override
    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public Map<String, Object> getContent() {
        return content;
    }

    public void setContent(Map<String, Object> content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "BasicUserNotification{" +
                "uuid='" + uuid + '\'' +
                ", userId='" + userId + '\'' +
                ", source='" + source + '\'' +
                ", type=" + type +
                ", message='" + message + '\'' +
                '}';
    }


}
