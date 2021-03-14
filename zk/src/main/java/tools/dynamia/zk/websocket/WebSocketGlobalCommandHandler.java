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

package tools.dynamia.zk.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.zkoss.zk.ui.Desktop;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketGlobalCommandHandler extends TextWebSocketHandler {

    private Map<String, String> desktops = new ConcurrentHashMap<>();
    private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String desktopId = message.getPayload();

        String oldSessionId = desktops.get(desktopId);
        if (oldSessionId != null) {
            WebSocketSession oldSession = sessions.get(oldSessionId);
            if (oldSession != null) {
                oldSession.close(CloseStatus.NORMAL);
            }
        }
        desktops.put(desktopId, session.getId());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        sessions.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {


        String desktopId = desktops.entrySet().stream().filter(e -> e.getValue().equals(session.getId())).map(Map.Entry::getKey).findFirst().orElse(null);
        if (desktopId != null) {
            desktops.remove(desktopId);
        }
        sessions.remove(session.getId());
    }

    WebSocketSession findSession(Desktop desktop) {
        WebSocketSession session = null;
        if (desktop != null && desktop.getId() != null) {
            String sessionID = desktops.get(desktop.getId());
            if (sessionID != null) {
                session = sessions.get(sessionID);
            }
        }
        return session;
    }

    Collection<WebSocketSession> getAllSessions() {
        return sessions.values();
    }
}
