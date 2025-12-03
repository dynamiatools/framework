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

package tools.dynamia.zk.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.zkoss.zk.ui.Desktop;
import tools.dynamia.commons.logger.Loggable;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for managing global ZK Desktop commands.
 *
 * <p>This handler maintains WebSocket connections for each ZK Desktop and enables
 * server-to-client push notifications through global commands. It supports:</p>
 *
 * <ul>
 *   <li>Desktop-to-session mapping for targeted message delivery</li>
 *   <li>Automatic cleanup of disconnected sessions</li>
 *   <li>Keep-alive PING/PONG mechanism to prevent idle timeouts</li>
 * </ul>
 *
 * <p><strong>Message Protocol:</strong></p>
 * <ul>
 *   <li>First message from client: Desktop ID (for registration)</li>
 *   <li>PING message: Client heartbeat to keep connection alive</li>
 *   <li>PONG response: Server acknowledgment of PING</li>
 *   <li>Command messages: Server-to-client global commands</li>
 * </ul>
 *
 * <p><strong>Example Usage:</strong></p>
 * <pre>{@code
 * // Find session for a desktop and send command
 * WebSocketSession session = handler.findSession(desktop);
 * if (session != null && session.isOpen()) {
 *     session.sendMessage(new TextMessage("refreshData"));
 * }
 * }</pre>
 *
 * @see ZKWebSocketConfigurer
 * @see WebSocketPushSender
 */
public class WebSocketGlobalCommandHandler extends TextWebSocketHandler implements Loggable {

    // Map of active WebSocket sessions by session ID
    private final Map<String, DeskstopWebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * Handles incoming text messages from WebSocket clients.
     *
     * <p>Processes two types of messages:</p>
     * <ul>
     *   <li><strong>PING:</strong> Keep-alive heartbeat from client. Responds with PONG to maintain connection.</li>
     *   <li><strong>Desktop ID:</strong> Associates the WebSocket session with a ZK Desktop for targeted messaging.</li>
     * </ul>
     *
     * <p>If a desktop is already associated with another session, the old session is closed
     * to prevent duplicate connections.</p>
     *
     * @param session the WebSocket session sending the message
     * @param message the text message received
     * @throws Exception if there's an error processing the message
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        // Handle PING messages to keep connection alive
        if ("PING".equals(payload)) {
            session.sendMessage(new TextMessage("PONG"));
            return;
        }

        if ("PONG".equals(payload)) {
            // Ignore PONG messages
            return;
        }

        // Handle desktop ID registration
        var oldSession = sessions.get(payload);
        if (oldSession != null) {
            oldSession.close(CloseStatus.NORMAL);
        }
        log("Associating desktop " + payload + " with ws session " + session.getId());
        sessions.put(session.getId(), new DeskstopWebSocketSession(payload, session));
    }

    /**
     * Called when a WebSocket connection is successfully established.
     *
     * <p>Registers the session in the active sessions map. The session will be associated
     * with a desktop ID when the client sends its first message.</p>
     *
     * @param session the newly established WebSocket session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log("New webSocket connection established: " + session.getId() + " waiting for desktop ID...");
    }

    /**
     * Called when a WebSocket connection is closed.
     *
     * <p>Performs cleanup by removing the session and its desktop association from
     * the internal maps. This ensures that closed connections don't accumulate in memory.</p>
     *
     * @param session the closed WebSocket session
     * @param status  the status code indicating why the connection was closed
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log("WebSocket connection closed: " + session.getId() + " with status " + status);
        sessions.remove(session.getId());
    }

    /**
     * Finds the WebSocket session associated with a specific ZK Desktop.
     *
     * <p>This method is used to retrieve the active WebSocket connection for a desktop,
     * enabling server-to-client push notifications.</p>
     *
     * @param desktop the ZK Desktop to find the session for
     * @return the associated WebSocket session, or {@code null} if not found or desktop is null
     */
    public DeskstopWebSocketSession findSession(Desktop desktop) {
        return sessions.values().stream()
                .filter(ds -> ds.matchesDesktop(desktop))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns all active WebSocket sessions.
     *
     * <p>This method can be used to broadcast messages to all connected clients
     * or for monitoring purposes.</p>
     *
     * @return a collection of all active WebSocket sessions
     */
    public Map<String, DeskstopWebSocketSession> getSessions() {
        return sessions;
    }

    /**
     * Closes the WebSocket session associated with a specific ZK Desktop.
     *
     * <p>This method safely closes the session and removes it from the internal
     * session map to prevent resource leaks.</p>
     *
     * @param desktop the ZK Desktop whose session should be closed
     */
    public void closeSession(Desktop desktop) {
        var session = findSession(desktop);
        if (session != null) {
            try {
                session.close(CloseStatus.NORMAL);
            } finally {
                sessions.remove(session.session.getId());
            }
        } else {
            log("No websocket session found for desktop " + desktop.getId());
        }
    }


    /**
     * Represents a WebSocket session associated with a specific ZK Desktop.
     *
     * <p>This record encapsulates the desktop ID and the WebSocket session,
     * providing utility methods for session management and message sending.</p>
     */
    public record DeskstopWebSocketSession(String desktopId, WebSocketSession session) {
        boolean isOpen() {
            return session.isOpen();
        }

        boolean matchesDesktop(Desktop desktop) {
            return desktop != null && desktopId.equals(desktop.getId());
        }

        boolean matchesSession(WebSocketSession otherSession) {
            return session.getId().equals(otherSession.getId());
        }

        void close(CloseStatus closeStatus) {
            try {
                session.close(closeStatus);
            } catch (Exception e) {
                //ignore
            }
        }

        void sendMessage(String message) throws IOException {
            session.sendMessage(new TextMessage(message));
        }
    }
}
