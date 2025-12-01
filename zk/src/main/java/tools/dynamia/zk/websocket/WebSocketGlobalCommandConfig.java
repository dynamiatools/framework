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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

@Configuration
@EnableWebSocket
public class WebSocketGlobalCommandConfig implements WebSocketConfigurer {

    private final LoggingService logger = new SLF4JLoggingService(WebSocketGlobalCommandConfig.class);

    public WebSocketGlobalCommandConfig() {
        logger.info("Starting " + getClass());
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        logger.info("Registering WS Handler for ZK Commands");
        registry.addHandler(globalCommandHandler(), "/ws-commands")
                .setAllowedOrigins("*"); // Permitir todas las origenes, ajustar según necesidades de seguridad
    }

    @Bean
    public WebSocketHandler globalCommandHandler() {
        return new WebSocketGlobalCommandHandler();
    }
}
