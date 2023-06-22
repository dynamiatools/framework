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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link RestTemplate} wrapper with BASIC and Bearer authentication support
 */
public class HttpRestClient {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final String url;
    private String username;
    private String password;
    private String bearerToken;
    private RestTemplate rest;
    private final Map<String, String> headers = new HashMap<>();

    private MediaType contentType = MediaType.APPLICATION_JSON;

    public HttpRestClient(String url) {
        this(url, null, null, null);
    }

    public HttpRestClient(String url, String username, String password) {
        this(url, username, password, null);
    }

    public HttpRestClient(String url, String username, String password, RestTemplate restTemplate) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.rest = restTemplate;

        if (this.rest == null) {
            this.rest = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        } else {
            this.rest = restTemplate;
        }
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    HttpHeaders createHeaders() {
        return new HttpHeaders() {{
            if (username != null && password != null) {
                String auth = username + ":" + password;
                byte[] encodedAuth = Base64.getEncoder().encode(
                        auth.getBytes(StandardCharsets.US_ASCII));
                String authHeader = "Basic " + new String(encodedAuth);
                set(AUTHORIZATION_HEADER, authHeader);
            } else if (bearerToken != null) {
                set(AUTHORIZATION_HEADER, "Bearer " + bearerToken);
            }

            setContentType(contentType);
            headers.forEach(this::set);
        }};
    }

    public Map get(String uri) {
        return get(uri, Map.class);

    }

    public <T> T get(String uri, Class<T> responseClass) {
        return exchange(HttpMethod.GET, uri, null, responseClass);
    }

    public List getList(String uri) {
        return exchange(HttpMethod.GET, uri, null, List.class);
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

        return exchange(HttpMethod.GET, uri, null, new ParameterizedTypeReference<>() {
        });
    }


    public Map post(String uri, Map<String, Object> body) {
        return post(uri, body, Map.class);

    }

    public <T> T post(String uri, Object body, Class<T> responseClass) {
        return exchange(HttpMethod.POST, uri, body, responseClass);
    }

    public Map put(String uri, Map<String, Object> body) {
        return put(uri, body, Map.class);

    }

    public <T> T put(String uri, Object body, Class<T> responseClass) {
        return exchange(HttpMethod.PUT, uri, body, responseClass);
    }

    public Map delete(String uri) {
        return delete(uri, Map.class);
    }

    public <T> T delete(String uri, Class<T> responseClass) {
        return exchange(HttpMethod.DELETE, uri, null, responseClass);
    }

    public <T> T exchange(HttpMethod method, String uri, Object body, Class<T> responseClass) {
        HttpEntity entity = new HttpEntity<>(body, createHeaders());

        ResponseEntity<T> response = rest.exchange(url + uri, method, entity, responseClass);
        return response.getBody();
    }

    public <T> T exchange(HttpMethod method, String uri, Object body, ParameterizedTypeReference<T> type) {
        HttpEntity entity = new HttpEntity<>(body, createHeaders());

        ResponseEntity<T> response = rest.exchange(url + uri, method, entity, type);
        return response.getBody();
    }

    public RestTemplate getRestTemplate() {
        return rest;
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
