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
package tools.dynamia.integration;

import java.util.Map;

/**
 * The Interface RequestSender. Provides functionality to send requests to remote services.
 * This functional interface defines the contract for sending HTTP requests or other types
 * of network communications to remote endpoints. Request senders are commonly used in
 * integration scenarios, microservices communication, webhook notifications, API calls,
 * and distributed system interactions. The interface abstracts the underlying communication
 * protocol and provides a simple, consistent API for request transmission.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * // Lambda expression for HTTP POST
 * RequestSender httpSender = (hostname, uri, params) -> {
 *     HttpClient client = HttpClient.newHttpClient();
 *     String url = "https://" + hostname + uri;
 *     // Build and send request with parameters
 * };
 * 
 * // Method reference
 * RequestSender apiSender = this::sendApiRequest;
 * 
 * // Usage
 * Map&lt;String, String&gt; params = Map.of("action", "notify", "data", "payload");
 * sender.send("api.example.com", "/webhook", params);
 * </code>
 *
 * @author Mario A. Serrano Leones
 */
@FunctionalInterface
public interface RequestSender {

    /**
     * Sends a request to the specified hostname and URI with parameters.
     *
     * @param hostname the target hostname
     * @param uri the target URI
     * @param parameters the request parameters
     */
    void send(String hostname, String uri, Map<String, String> parameters);

}
