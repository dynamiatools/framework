/*
 * Copyright (C) 2025 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

/**
 * DynamiaTools WebSocket Client
 *
 * Provides WebSocket connection management for ZK Desktop global commands with automatic
 * reconnection support using exponential backoff strategy.
 *
 * FEATURES:
 * - Native WebSocket (no external dependencies)
 * - Automatic reconnection with exponential backoff
 * - Configurable retry attempts and delays
 * - Automatic protocol detection (ws:// or wss://)
 * - Connection state monitoring
 * - Clean connection lifecycle management
 * - Keep-alive heartbeat to prevent idle timeout
 *
 * USAGE:
 *
 * 1. Initialize connection (automatic on ZK mount):
 *    DynamiaToolsWS.init('/ws-commands');
 *
 * 2. Get connection status:
 *    var status = DynamiaToolsWS.getStatus();
 *    console.log(status.connected);        // true/false
 *    console.log(status.reconnectAttempts); // number of attempts
 *    console.log(status.desktopId);        // ZK desktop ID
 *    console.log(status.readyState);       // WebSocket ready state (0-3)
 *
 * 3. Manually close connection:
 *    DynamiaToolsWS.close();
 *
 * 4. Reset reconnection counter:
 *    DynamiaToolsWS.reset();
 *
 * 5. Configure reconnection behavior:
 *    DynamiaToolsWS.config.maxAttempts = 15;        // default: 10
 *    DynamiaToolsWS.config.initialDelay = 2000;     // default: 1000ms
 *    DynamiaToolsWS.config.maxDelay = 60000;        // default: 30000ms
 *    DynamiaToolsWS.config.backoffMultiplier = 2.0; // default: 1.5
 *
 * 6. Configure keep-alive behavior:
 *    DynamiaToolsWS.keepAlive.enabled = true;       // default: true
 *    DynamiaToolsWS.keepAlive.interval = 30000;     // default: 30000ms (30 seconds)
 *
 * 7. Monitor connection health:
 *    setInterval(function() {
 *        var status = DynamiaToolsWS.getStatus();
 *        if (!status.connected) {
 *            console.warn('WebSocket disconnected, attempts:', status.reconnectAttempts);
 *        }
 *    }, 30000);
 *
 * READY STATES:
 * - 0: CONNECTING - Connection is being established
 * - 1: OPEN - Connection is open and ready to communicate
 * - 2: CLOSING - Connection is closing
 * - 3: CLOSED - Connection is closed
 *
 * AUTOMATIC BEHAVIOR:
 * - Initializes automatically when ZK desktop is ready
 * - Reconnects automatically on unexpected disconnections
 * - Cleans up on page unload
 * - Sends desktop ID to server on connection
 * - Processes global commands from server
 * - Sends periodic PING messages to prevent idle timeout
 *
 * EXAMPLES:
 *
 * Example 1 - Basic monitoring:
 *   if (DynamiaToolsWS.getStatus().connected) {
 *       console.log('WebSocket is connected');
 *   }
 *
 * Example 2 - Custom configuration:
 *   DynamiaToolsWS.config.maxAttempts = 20;
 *   DynamiaToolsWS.config.initialDelay = 500;
 *   DynamiaToolsWS.init('/ws-commands');
 *
 * Example 3 - Manual reconnection:
 *   DynamiaToolsWS.close();
 *   DynamiaToolsWS.reset();
 *   DynamiaToolsWS.init('/ws-commands');
 *
 * Example 4 - Disable keep-alive:
 *   DynamiaToolsWS.keepAlive.enabled = false;
 *
 * @public
 * @global DynamiaToolsWS
 */

(function () {
    'use strict';

    // Reconnection configuration
    var reconnectConfig = {
        maxAttempts: 10,
        initialDelay: 1000,
        maxDelay: 30000,
        backoffMultiplier: 1.5
    };

    // Keep-alive configuration
    var keepAliveConfig = {
        enabled: true,
        interval: 30000 // Send ping every 30 seconds
    };

    var wsManager = {
        socket: null,
        reconnectAttempts: 0,
        reconnectTimeout: null,
        keepAliveInterval: null,
        isIntentionallyClosed: false,
        uri: null,
        desktopId: null
    };

    /**
     * Calculates the delay for the next reconnection attempt using exponential backoff
     */
    function getReconnectDelay() {
        var delay = reconnectConfig.initialDelay * Math.pow(
            reconnectConfig.backoffMultiplier,
            wsManager.reconnectAttempts
        );
        return Math.min(delay, reconnectConfig.maxDelay);
    }

    /**
     * Clears the reconnection timeout if it exists
     */
    function clearReconnectTimeout() {
        if (wsManager.reconnectTimeout) {
            clearTimeout(wsManager.reconnectTimeout);
            wsManager.reconnectTimeout = null;
        }
    }

    /**
     * Clears the keep-alive interval if it exists
     */
    function clearKeepAliveInterval() {
        if (wsManager.keepAliveInterval) {
            clearInterval(wsManager.keepAliveInterval);
            wsManager.keepAliveInterval = null;
        }
    }

    /**
     * Starts the keep-alive mechanism to prevent idle disconnections
     */
    function startKeepAlive() {
        if (!keepAliveConfig.enabled) {
            return;
        }

        clearKeepAliveInterval();

        wsManager.keepAliveInterval = setInterval(function () {
            if (wsManager.socket && wsManager.socket.readyState === WebSocket.OPEN) {
                try {
                    wsManager.socket.send('PING');
                    console.debug('DynamiaTools WebSocket: ping sent');
                } catch (error) {
                    console.warn('DynamiaTools WebSocket: error sending ping', error);
                }
            }
        }, keepAliveConfig.interval);
    }

    /**
     * Attempts to reconnect the WebSocket
     */
    function attemptReconnect() {
        if (wsManager.isIntentionallyClosed) {
            console.log('DynamiaTools WebSocket: reconnection cancelled (intentional close)');
            return;
        }

        if (wsManager.reconnectAttempts >= reconnectConfig.maxAttempts) {
            console.error('DynamiaTools WebSocket: maximum reconnection attempts reached');
            return;
        }

        var delay = getReconnectDelay();
        wsManager.reconnectAttempts++;

        console.log(
            'DynamiaTools WebSocket: reconnection attempt ' +
            wsManager.reconnectAttempts + '/' + reconnectConfig.maxAttempts +
            ' in ' + delay + 'ms'
        );

        clearReconnectTimeout();
        wsManager.reconnectTimeout = setTimeout(function () {
            initWebSocket(wsManager.uri);
        }, delay);
    }

    /**
     * Closes the WebSocket connection cleanly
     */
    function closeWebSocket() {
        wsManager.isIntentionallyClosed = true;
        clearReconnectTimeout();
        clearKeepAliveInterval();

        if (wsManager.socket) {
            try {
                wsManager.socket.close();
            } catch (e) {
                console.warn('DynamiaTools WebSocket: error closing socket', e);
            }
            wsManager.socket = null;
        }
    }

    /**
     * Converts an HTTP URI to a WebSocket URI
     */
    function getWebSocketUri(uri) {
        var protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        var host = window.location.host;

        // If URI is already absolute, use it directly
        if (uri.startsWith('ws://') || uri.startsWith('wss://')) {
            return uri;
        }

        // Build complete URI
        var path = uri.startsWith('/') ? uri : '/' + uri;
        return protocol + '//' + host + path;
    }

    /**
     * Initializes the WebSocket connection
     */
    function initWebSocket(uri) {
        var desktop = zk.Desktop.$();

        if (!desktop) {
            console.warn('DynamiaTools WebSocket: ZK desktop is not ready');
            return;
        }

        wsManager.uri = uri;
        wsManager.desktopId = desktop.id;

        // Close previous connection if it exists
        if (wsManager.socket) {
            try {
                wsManager.socket.close();
            } catch (e) {
                // Ignore close errors
            }
        }

        try {
            var wsUri = getWebSocketUri(uri);
            console.log('DynamiaTools WebSocket: connecting to ' + wsUri);

            wsManager.socket = new WebSocket(wsUri);

            wsManager.socket.onopen = function () {
                console.log('DynamiaTools WebSocket connected - Desktop: ' + wsManager.desktopId);
                wsManager.reconnectAttempts = 0;
                clearReconnectTimeout();
                wsManager.isIntentionallyClosed = false;

                // Send desktop ID to server
                wsManager.socket.send(wsManager.desktopId);

                // Start keep-alive heartbeat
                startKeepAlive();
            };

            wsManager.socket.onmessage = function (e) {
                if (!e.data) {
                    console.warn('DynamiaTools WebSocket: empty message received');
                    return;
                }

                // Ignore server heartbeats
                if (e.data === 'PONG') {
                    return;
                }

                console.debug('DynamiaTools command received:', e.data);

                try {
                    var currentDesktop = zk.Desktop.$();
                    if (currentDesktop) {
                        var parsed = tryParseJSON(e.data);
                        if (parsed && typeof parsed.command === 'string' && parsed.command) {
                            // Send parsed object directly so additional fields are available server-side
                            zAu.send(new zk.Event(
                                currentDesktop,
                                'onGlobalCommand',
                                parsed,
                                {}
                            ));
                        } else {
                            // Fallback: send raw string command as before
                            zAu.send(new zk.Event(
                                currentDesktop,
                                'onGlobalCommand',
                                {command: e.data},
                                {}
                            ));
                        }
                    } else {
                        console.warn('DynamiaTools WebSocket: desktop not available to process command');
                    }
                } catch (error) {
                    console.error('DynamiaTools WebSocket: error processing command', error);
                }
            };

            wsManager.socket.onclose = function (event) {
                console.log('DynamiaTools WebSocket connection closed - Code:', event.code, 'Reason:', event.reason);
                clearKeepAliveInterval();
                wsManager.socket = null;

                if (!wsManager.isIntentionallyClosed) {
                    attemptReconnect();
                }
            };

            wsManager.socket.onerror = function (error) {
                console.error('DynamiaTools WebSocket error:', error);
            };

        } catch (error) {
            console.error('DynamiaTools WebSocket: error creating connection', error);
            if (!wsManager.isIntentionallyClosed) {
                attemptReconnect();
            }
        }
    }

    /**
     * Resets the reconnection attempt counter
     */
    function resetReconnectAttempts() {
        wsManager.reconnectAttempts = 0;
        clearReconnectTimeout();
    }

    /**
     * Safely tries to parse a JSON string and returns the object, or null on failure.
     * Only parses when the input is a non-empty string. Arrays are considered unsupported here.
     * @param {string} data
     * @returns {object|null}
     */
    function tryParseJSON(data) {
        if (typeof data !== 'string') return null;
        var text = data.trim();
        if (!text) return null;
        try {
            var parsed = JSON.parse(text);
            if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
                return parsed;
            }
        } catch (e) {
            // not JSON, ignore
        }
        return null;
    }

    // Expose public API
    window.DynamiaToolsWS = {
        init: initWebSocket,
        close: closeWebSocket,
        reset: resetReconnectAttempts,
        getStatus: function () {
            return {
                connected: wsManager.socket && wsManager.socket.readyState === WebSocket.OPEN,
                reconnectAttempts: wsManager.reconnectAttempts,
                desktopId: wsManager.desktopId,
                readyState: wsManager.socket ? wsManager.socket.readyState : null
            };
        },
        config: reconnectConfig,
        keepAlive: keepAliveConfig
    };

    // Initialize when ZK is ready
    zk.afterMount(function () {
        initWebSocket('/ws-commands');
    });

    // Cleanup on page unload
    window.addEventListener('beforeunload', function () {
        closeWebSocket();
    });

})();
