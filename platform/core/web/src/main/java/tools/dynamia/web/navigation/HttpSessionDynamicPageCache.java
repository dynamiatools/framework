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
package tools.dynamia.web.navigation;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import tools.dynamia.navigation.DynamicPageCache;
import tools.dynamia.navigation.Page;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link DynamicPageCache} stored in the HTTP session of the current request, so pages of dynamic groups resolved for
 * one account are never seen by another one. Without a request (background jobs) it caches nothing.
 *
 * @author Mario A. Serrano Leones
 */
@Component
public class HttpSessionDynamicPageCache implements DynamicPageCache {

    static final String ATTRIBUTE = HttpSessionDynamicPageCache.class.getName();

    @Override
    public Page get(String key) {
        Holder holder = holder(false);
        return holder == null ? null : holder.pages.get(key);
    }

    @Override
    public void put(String key, Page page) {
        Holder holder = holder(true);
        if (holder != null && key != null && page != null) {
            holder.pages.put(key, page);
        }
    }

    private Holder holder(boolean create) {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        try {
            Holder holder = (Holder) attributes.getAttribute(ATTRIBUTE, RequestAttributes.SCOPE_SESSION);
            if (holder == null && create) {
                holder = new Holder();
                attributes.setAttribute(ATTRIBUTE, holder, RequestAttributes.SCOPE_SESSION);
            }
            return holder;
        } catch (IllegalStateException e) {
            // session already invalidated
            return null;
        }
    }

    /**
     * Session attribute. Pages are not serializable state worth persisting, so they are transient and rebuilt on demand.
     */
    private static class Holder implements Serializable {
        private transient Map<String, Page> pages = new ConcurrentHashMap<>();

        private Object readResolve() {
            pages = new ConcurrentHashMap<>();
            return this;
        }
    }
}
