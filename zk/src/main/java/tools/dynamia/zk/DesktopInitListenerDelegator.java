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

package tools.dynamia.zk;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.util.DesktopInit;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;

/**
 * This class is registrered in zk.xml as a <listener>..</listener> to delegate all @{@link DesktopInit} calls to {@link DesktopListener}.
 *
 */
public class DesktopInitListenerDelegator implements DesktopInit {

    private static final LoggingService LOGGER = new SLF4JLoggingService(DesktopInitListenerDelegator.class);

    public DesktopInitListenerDelegator() {
        LOGGER.info("Starting " + getClass());
    }

    @Override
    public void init(Desktop desktop, Object request) throws Exception {
        Containers.get().findObjects(DesktopListener.class).forEach(l -> l.onDesktopInit(desktop, request));
    }
}
