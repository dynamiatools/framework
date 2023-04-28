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

import com.googlecode.jsonrpc4j.spring.JsonProxyFactoryBean;
import org.apache.http.client.methods.HttpPost;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.remoting.httpinvoker.HttpComponentsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to create Spring Remoting service client. Its supports standar java serialization
 * {@link HttpInvokerProxyFactoryBean} and {@link JsonProxyFactoryBean for JSONRPC }. Also you can use this class as a
 * factory bean in Spring Config classes.
 *
 * @author Mario Serrano Leones
 */
public class HttpRemotingServiceClient<T> implements FactoryBean<T> {

    private final static LoggingService LOGGER = new SLF4JLoggingService(HttpRemotingServiceClient.class);
    private String serviceURL;
    private String username;
    private String password;
    private FactoryBean<Object> delegate;
    private Class<T> serviceInterface;
    private boolean jsonRPC;
    private int connectionTimeout = 10 * 60000; //10 minutes

    private HttpRemotingServiceClient(Class<T> serviceInterface) {
        super();
        this.serviceInterface = serviceInterface;
    }

    private void init() {
        if (serviceInterface == null) {
            try {
                serviceInterface = BeanUtils.getGenericTypeClass(this);
            } catch (Exception e) {
                throw new UnsupportedOperationException(
                        "Cannot init service class, JDK not support generic instrospection. Specified service class using constructor",
                        e);
            }
        }


        final String authorization = getAuthorization();
        LOGGER.info("Initializing Remoting Service for interfaces " + serviceInterface + " in url " + serviceURL);
        if (isJsonRPC()) {
            LOGGER.info("Creating JSON factory bean");
            JsonProxyFactoryBean json = new JsonProxyFactoryBean();
            json.setServiceInterface(serviceInterface);
            json.setServiceUrl(serviceURL);

            if (authorization != null) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic " + authorization);
                json.setExtraHttpHeaders(headers);
            }
            delegate = json;
        } else {
            LOGGER.info("Creating stanrdard java factory bean");
            HttpInvokerProxyFactoryBean standard = new HttpInvokerProxyFactoryBean();
            standard.setServiceInterface(serviceInterface);
            standard.setServiceUrl(serviceURL);

            HttpComponentsHttpInvokerRequestExecutor executor = new HttpComponentsHttpInvokerRequestExecutor() {
                @Override
                protected HttpPost createHttpPost(HttpInvokerClientConfiguration config) throws IOException {
                    HttpPost post = super.createHttpPost(config);

                    if (authorization != null) {
                        post.addHeader("Authorization", "Basic " + authorization);
                    }
                    return post;
                }
            };


            executor.setReadTimeout(connectionTimeout);
            standard.setHttpInvokerRequestExecutor(executor);

            standard.afterPropertiesSet();
            delegate = standard;
        }
    }

    private String getAuthorization() {
        return username != null && password != null ? new String(Base64.getEncoder().encode((username + ":" + password).getBytes())) : null;
    }

    public T getProxy() {
        if (delegate == null) {
            init();
        }

        try {
            return (T) delegate.getObject();
        } catch (Exception e) {
            throw new HttpServiceException("Error creating proxy object for " + serviceInterface, e);
        }
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public HttpRemotingServiceClient<T> setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public HttpRemotingServiceClient<T> setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public HttpRemotingServiceClient<T> setPassword(String password) {
        this.password = password;
        return this;
    }

    public static <T> HttpRemotingServiceClient<T> build(Class<T> serviceInterface) {
        return new HttpRemotingServiceClient<>(serviceInterface);
    }

    @Override
    public T getObject() throws Exception {
        return getProxy();
    }

    @Override
    public Class<?> getObjectType() {
        return serviceInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public boolean isJsonRPC() {
        return jsonRPC;
    }

    public HttpRemotingServiceClient setJsonRPC(boolean jsonRPC) {
        this.jsonRPC = jsonRPC;
        return this;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }
}
