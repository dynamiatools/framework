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
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Clients;
import tools.dynamia.commons.StringPojoParser;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;
import tools.dynamia.zk.util.ZKUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helper class to send push command to desktop client using web socket
 */
public abstract class WebSocketPushSender {


    private static final LoggingService LOGGER = new SLF4JLoggingService(WebSocketPushSender.class);

    /**
     * Send a push command to client desktop. The command its returned to the server as a ZK Global Command. You should
     * have a ViemModel that receive this global command
     *
     * @return true if command is sended successfull. Check log for false response
     */

    public static boolean sendPushCommand(Desktop desktop, String command) {
        return sendPushCommand(desktop, command, null);
    }

    /**
     * Send a push command to client desktop. The command its returned to the server as a ZK Global Command. You should
     * have a ViemModel that receive this global command
     *
     * @return true if command is sended successfull. Check log for false response
     */
    public static boolean sendPushCommand(Desktop desktop, String command, Map<String, Object> payload) {
        WebSocketGlobalCommandHandler handler = getHandler();
        if (handler != null) {
            try {
                Map<String, Object> data = new HashMap<>();
                if (payload != null) {
                    data.putAll(payload);
                }
                data.put("command", command);
                var session = handler.findSession(desktop);
                if (session != null) {
                    String textData = StringPojoParser.convertMapToJson(data);
                    session.sendMessage(textData);
                    return true;
                } else {
                    LOGGER.warn("No websocket session found for desktop " + desktop);
                }
            } catch (IOException e) {
                LOGGER.error("IO Error sending push command '" + command + "' to Dekstop: " + desktop, e);
                handler.closeSession(desktop);
            } catch (Exception e) {
                LOGGER.error("Error sending push command '" + command + "' to Dekstop: " + desktop, e);
            }
        } else {
            LOGGER.warn("No websocket handler found");
        }
        return false;
    }

    /**
     * Send a push command to current client desktop. The command its returned to the server as a ZK Global Command. You should
     * have a ViemModel that receive this global command
     *
     * @return true if command is sended successfull. Check log for false response
     */
    public static boolean sendPushCommand(String command) {
        return sendPushCommand(Executions.getCurrent().getDesktop(), command);
    }

    /**
     * Send a push command to all connected sessions
     *
     */
    public static void broadcastCommand(String command) {
        WebSocketGlobalCommandHandler handler = getHandler();
        if (handler != null) {
            AtomicInteger count = new AtomicInteger();
            handler.getSessions().values().forEach(s -> {
                try {
                    s.sendMessage(command);
                    count.incrementAndGet();
                } catch (IOException e) {
                    LOGGER.error("IO Error sending command " + command + " to WS Session: " + s, e);
                    s.close(CloseStatus.NORMAL);
                } catch (Exception e) {
                    LOGGER.error("Error sending command " + command + " to WS Session: " + s, e);
                }
            });
            LOGGER.info("Broadcasted command '" + command + "' to " + count.get() + " WS sessions.");
        }
    }

    /**
     * Broadcast a heartbeat PING command to all connected sessions
     */
    public static void broadcastHeartbeat() {
        broadcastCommand("PING");
    }


    public static WebSocketGlobalCommandHandler getHandler() {
        return Containers.get().findObject(WebSocketGlobalCommandHandler.class);
    }

    /**
     * Initialize WebSocket connection on client side by requesting it via Clients.evalJavaScript
     */
    public static void initWS() {
        if (ZKUtil.isInEventListener()) {
            try {
                LOGGER.info("Requesting to initialize Websocket connection");
                var config = Containers.get().findObject(ZKWebSocketConfigurer.class);
                var endpoint = config != null ? config.getEndpoint() : "/ws-commands";
                Clients.evalJavaScript("DynamiaToolsWS.init('" + endpoint + "');");
            } catch (Exception e) {
                //
            }
        }
    }

    /**
     * Check if desktop has an open WS session, if not try to init WS connection
     */
    public static void checkDesktop(Desktop desktop) {
        var handler = getHandler();
        if (handler != null) {
            var session = handler.findSession(desktop);
            if (session == null || !session.isOpen()) {
                initWS();
            }
        }
    }
}
