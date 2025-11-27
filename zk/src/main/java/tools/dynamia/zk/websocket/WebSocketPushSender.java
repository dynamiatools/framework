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

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Clients;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;
import tools.dynamia.zk.util.ZKUtil;

import java.io.IOException;

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
        WebSocketGlobalCommandHandler handler = Containers.get().findObject(WebSocketGlobalCommandHandler.class);
        if (handler != null) {
            try {
                WebSocketSession session = handler.findSession(desktop);
                if (session != null) {
                    session.sendMessage(new TextMessage(command));
                    return true;
                } else {
                    LOGGER.warn("No websocket session found for desktop " + desktop);
                }
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
        WebSocketGlobalCommandHandler handler = Containers.get().findObject(WebSocketGlobalCommandHandler.class);
        if (handler != null) {
            handler.getAllSessions().forEach(s -> {
                try {
                    s.sendMessage(new TextMessage(command));
                } catch (IOException e) {
                    LOGGER.error("Error sending command " + command + " to WS Session: " + s);
                }
            });
        }
    }

    /**
     * Init WS connection with client
     */
    public static void initWS() {
        if (ZKUtil.isInEventListener()) {
            try {
                LOGGER.info("Requesting to initialize Websocket connection");
                Clients.evalJavaScript("initWebSocket('/ws-commands');");
            } catch (Exception e) {
                //
            }
        }
    }
}
