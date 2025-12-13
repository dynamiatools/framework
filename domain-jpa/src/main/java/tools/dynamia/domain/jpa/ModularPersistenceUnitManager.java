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
package tools.dynamia.domain.jpa;

import jakarta.persistence.spi.PersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;

import java.net.URL;


/**
 * The Class ModularPersistenceUnitManager.
 *
 * @author Mario A. Serrano Leones
 */
public class ModularPersistenceUnitManager extends DefaultPersistenceUnitManager {

    /**
     * The Constant log.
     */
    private static final LoggingService log = new SLF4JLoggingService(ModularPersistenceUnitManager.class);

    /**
     * Instantiates a new modular persistence unit manager.
     */
    public ModularPersistenceUnitManager() {
        log.info("Iniatilizing " + ModularPersistenceUnitManager.class.getName() + "...");
    }

    /* (non-Javadoc)
     * @see org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager#postProcessPersistenceUnitInfo(org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo)
     */
    @Override
    protected void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {

        // Invoke normal post processing
        super.postProcessPersistenceUnitInfo(pui);

        MutablePersistenceUnitInfo oldPui = getPersistenceUnitInfo(pui.getPersistenceUnitName());

        if (oldPui != null) {
            postProcessPersistenceUnitInfo(pui, oldPui);
        }
    }

    /**
     * Post process persistence unit info.
     *
     * @param pui the pui
     * @param oldPui the old pui
     */
    void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui, MutablePersistenceUnitInfo oldPui) {

        for (URL url : oldPui.getJarFileUrls()) {

            // Add jar file url to PUI
            if (!pui.getJarFileUrls().contains(url)) {
                pui.addJarFileUrl(url);
            }
        }

        pui.addJarFileUrl(oldPui.getPersistenceUnitRootUrl());
    }
}
