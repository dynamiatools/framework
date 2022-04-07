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

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very simple session tracker. Do not use it in cluster environment
 *
 * @author Mario A. Serrano Leones
 */
public class SessionTracker implements HttpSessionListener {


    private static final Map<String, HttpSession> activeSessions = new ConcurrentHashMap<>();

    @Override
    public void sessionCreated(HttpSessionEvent evt) {
        activeSessions.put(evt.getSession().getId(), evt.getSession());

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent evt) {
        activeSessions.remove(evt.getSession().getId());
    }

    public static Collection<HttpSession> getActiveSessions() {
        return activeSessions.values();
    }

    public static HttpSession getSessionById(String sessionId) {
        return activeSessions.get(sessionId);
    }

    public static int getActionSessionsCount() {
        return getActiveSessions().size();
    }
}
