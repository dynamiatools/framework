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
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import tools.dynamia.commons.logger.Loggable;


/**
 * Spring WebSocket configurer for ZK global commands.
 *
 * <p>This class implements {@link WebSocketConfigurer} and registers a WebSocket handler
 * for ZK commands at a configurable endpoint. It exposes two beans:
 * <ul>
 *     <li>{@link WebSocketGlobalCommandHandler}</li>
 *     <li>{@link DefaultHandshakeHandler}</li>
 * </ul>
 * <p>
 * Usage:
 * <ul>
 *     <li>Extend this class in your own {@code @Configuration} to customize endpoint,
 *         allowed origins or handshake behavior.</li>
 *     <li>Or import it from another {@code @Configuration} class using
 *         {@code @Import(ZKWebSocketConfigurer.class)} to reuse the default configuration.</li>
 * </ul>
 * <p>
 * Important: When using this class as a Spring configuration (either by extending it
 * or importing it), the hosting configuration must enable WebSocket support by being
 * annotated with {@code @EnableWebSocket}. Without {@code @EnableWebSocket} Spring
 * will not process WebSocket configuration. Examples:
 *
 * <pre>{@code
 * @Configuration
 * @EnableWebSocket
 * public class MyWsConfig extends ZKWebSocketConfigurer {
 *     public MyWsConfig() {
 *         setEndpoint("/my-ws");
 *         setAllowedOrigins(new String[]{"https://example.com"});
 *     }
 * }
 *
 * @Configuration
 * @EnableWebSocket
 * @Import(ZKWebSocketConfigurer.class)
 * public class AppConfig { }
 * }</pre>
 * <p>
 * This class is intentionally lightweight and provides sensible defaults. Override
 * or extend behaviour when a project needs different endpoint paths, CORS rules
 * or a custom handshake handler.
 */
public class ZKWebSocketConfigurer implements WebSocketConfigurer, Loggable {

    private String endpoint = "/ws-commands";
    private String[] allowedOrigins = new String[]{"*"};
    private String[] allowedOriginPatterns = null;


    /**
     * Creates a new ZKWebSocketConfigurer with default configuration values.
     * A startup message is logged when an instance is created.
     */
    public ZKWebSocketConfigurer() {
        log("Starting " + getClass());
    }

    /**
     * Register the WebSocket handlers into the provided {@link WebSocketHandlerRegistry}.
     * This method configures the handler endpoint, allowed origins / origin patterns
     * and the handshake handler.
     *
     * @param registry the WebSocketHandlerRegistry used to register handlers
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log("Registering WS Handler for ZK Commands");
        var reg = registry.addHandler(globalCommandHandler(), endpoint != null ? endpoint : "/ws-commands");
        if (allowedOrigins != null) {
            reg.setAllowedOrigins(allowedOrigins);
        }
        if (allowedOriginPatterns != null) {
            reg.setAllowedOriginPatterns(allowedOriginPatterns);
        }
        reg.setHandshakeHandler(handshakeHandler());
    }

    /**
     * Exposes the {@link WebSocketGlobalCommandHandler} as a Spring bean.
     * Override this method if you need to provide a custom handler implementation.
     *
     * @return a new instance of {@link WebSocketGlobalCommandHandler}
     */
    @Bean
    public WebSocketGlobalCommandHandler globalCommandHandler() {
        return new WebSocketGlobalCommandHandler();
    }

    /**
     * Exposes the {@link DefaultHandshakeHandler} as a Spring bean.
     * Override this method to provide a custom handshake handler when needed.
     *
     * @return a new instance of {@link DefaultHandshakeHandler}
     */
    @Bean
    public DefaultHandshakeHandler handshakeHandler() {
        return new DefaultHandshakeHandler();
    }

    /**
     * Returns the configured WebSocket endpoint path. Default is {@code "/ws-commands"}.
     *
     * @return the endpoint path where the WebSocket handler is registered
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the WebSocket endpoint path where the handler will be registered.
     *
     * @param endpoint the endpoint path to set (e.g. {@code "/ws-commands"})
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Returns the allowed origins used for CORS. By default this is {@code new String[]{"*"}}.
     *
     * @return the allowed origins array or {@code null} if not set
     */
    public String[] getAllowedOrigins() {
        return allowedOrigins;
    }

    /**
     * Sets the allowed origins used for CORS.
     *
     * @param allowedOrigins an array of allowed origin strings
     */
    public void setAllowedOrigins(String... allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * Returns the allowed origin patterns used for CORS. If set, these patterns
     * will be applied instead of {@link #allowedOrigins}.
     *
     * @return the allowed origin patterns array or {@code null} if not set
     */
    public String[] getAllowedOriginPatterns() {
        return allowedOriginPatterns;
    }

    /**
     * Sets the allowed origin patterns used for CORS.
     *
     * @param allowedOriginPatterns an array of allowed origin pattern strings
     */
    public void setAllowedOriginPatterns(String... allowedOriginPatterns) {
        this.allowedOriginPatterns = allowedOriginPatterns;
    }
}
