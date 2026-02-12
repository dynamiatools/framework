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

package tools.dynamia.web.util;


import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link RestClient} wrapper with BASIC and Bearer authentication support
 */
public class HttpRestClient {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final String baseURL;
    private String username;
    private String password;
    private String bearerToken;
    private RestClient restClient;
    private final Map<String, String> headers = new HashMap<>();

    private MediaType contentType = MediaType.APPLICATION_JSON;

    public HttpRestClient(RestClient restClient) {
        this.restClient = restClient;
        this.baseURL = "";
    }

    public HttpRestClient(String baseURL) {
        this(baseURL, null, null, null);
    }

    public HttpRestClient(String baseURL, String username, String password) {
        this(baseURL, username, password, null);
    }

    public HttpRestClient(String baseURL, String username, String password, RestTemplate restTemplate) {
        this.baseURL = baseURL;
        this.username = username;
        this.password = password;

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseURL)
                .defaultHeaders(this::setHeaders);

        if (restTemplate != null) {
            builder.requestFactory(restTemplate.getRequestFactory());
        }

        this.restClient = builder.build();
    }


    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    private void setHeaders(HttpHeaders headers) {
        if (username != null && password != null) {
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
            headers.set(AUTHORIZATION_HEADER, "Basic " + new String(encodedAuth));
        } else if (bearerToken != null) {
            headers.set(AUTHORIZATION_HEADER, "Bearer " + bearerToken);
        }
        headers.setContentType(contentType);
        this.headers.forEach(headers::set);
    }

    public Map get(String uri) {
        return get(uri, Map.class);
    }

    public <T> T get(String uri, Class<T> responseClass) {
        return restClient.get()
                .uri(uri)
                .headers(this::setHeaders)
                .retrieve()
                .body(responseClass);
    }

    public List getList(String uri) {
        return getList(uri, Object.class);
    }

    /**
     * Perf
     *
     * @param uri
     * @param type
     * @param <T>
     * @return
     */
    public <T> List<T> getList(String uri, Class<T> type) {
        return restClient.get()
                .uri(uri)
                .headers(this::setHeaders)
                .retrieve()
                .body(new ParameterizedTypeReference<List<T>>() {
                });
    }

    public Map post(String uri, Map<String, Object> body) {
        return post(uri, body, Map.class);
    }

    public <T> T post(String uri, Object body, Class<T> responseClass) {
        return restClient.post()
                .uri(uri)
                .headers(this::setHeaders)
                .body(body)
                .retrieve()
                .body(responseClass);
    }

    public Map put(String uri, Map<String, Object> body) {
        return put(uri, body, Map.class);
    }

    public <T> T put(String uri, Object body, Class<T> responseClass) {
        return restClient.put()
                .uri(uri)
                .headers(this::setHeaders)
                .body(body)
                .retrieve()
                .body(responseClass);
    }

    public Map delete(String uri) {
        return delete(uri, Map.class);
    }

    public <T> T delete(String uri, Class<T> responseClass) {
        return restClient.delete()
                .uri(uri)
                .headers(this::setHeaders)
                .retrieve()
                .body(responseClass);
    }

    public <T> T exchange(HttpMethod method, String uri, Object body, Class<T> responseClass) {
        return restClient.method(method)
                .uri(uri)
                .headers(this::setHeaders)
                .body(body)
                .retrieve()
                .body(responseClass);
    }

    public <T> T exchange(HttpMethod method, String uri, Object body, ParameterizedTypeReference<T> type) {
        return restClient.method(method)
                .uri(uri)
                .headers(this::setHeaders)
                .body(body)
                .retrieve()
                .body(type);
    }

    public RestClient getRestClient() {
        return restClient;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public MediaType getContentType() {
        return contentType;
    }

    public void setContentType(MediaType contentType) {
        this.contentType = contentType;
    }
}
