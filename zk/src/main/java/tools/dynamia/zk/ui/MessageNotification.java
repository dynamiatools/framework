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
package tools.dynamia.zk.ui;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import tools.dynamia.ui.MessageType;

import java.util.Map;

/**
 * @author Mario A. Serrano Leones Serrano
 */
public class MessageNotification extends MessageDialog {

    private String position = "bottom_right";
    private int timeout = 3000;

    public MessageNotification() {
    }

    public MessageNotification(Map<String, Object> config) {

        Object pos = config.get("position");
        if (pos != null) {
            position = String.valueOf(pos);
        }
        Object time = config.get("timeout");
        if (time != null) {
            try {
                timeout = Integer.parseInt(String.valueOf(time));
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void showMessage(String message) {
        showMessage(message, "Mensaje", MessageType.INFO);
    }

    @Override
    public void showMessage(String message, MessageType type) {
        showMessage(message, "Message", type);
    }

    @Override
    public void showMessage(String message, String title, MessageType type) {
        showMessage(message, title, type, null);
    }

    @Override
    public void showMessage(String message, String title, MessageType type, Object source) {
        if (type == MessageType.NORMAL || type == MessageType.SPECIAL) {
            type = MessageType.INFO;
        }
        String pos = position;
        Component ref = null;
        if (source instanceof Component) {
            ref = (Component) source;
            pos = "after_center";
        }
        Clients.showNotification(message, type.toString().toLowerCase(), ref, pos, timeout, true);

    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
