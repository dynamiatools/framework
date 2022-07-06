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

package tools.dynamia.web.util;


import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link RestTemplate} wrapper with BASIC authentication support
 */
public class HttpRestClient {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    private final String url;
    private final String username;
    private final String password;
    private RestTemplate rest;
    private final Map<String, String> headers = new HashMap<>();

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
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(
                    auth.getBytes(StandardCharsets.US_ASCII));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
            setContentType(MediaType.APPLICATION_JSON);
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

    protected <T> T exchange(HttpMethod method, String uri, Object body, Class<T> responseClass) {
        HttpEntity entity = new HttpEntity<>(body, createHeaders());

        ResponseEntity<T> response = rest.exchange(url + uri, method, entity, responseClass);
        return response.getBody();
    }


}
