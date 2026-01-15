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

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.OpenPersistenceInViewProvider;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Provider;


/**
 * This is a helper class for implement the open entitymanager in view pattern
 * using JPA and Spring. You can extends it or use it direclty. The only thing
 * you need to do is call the methods beforeView() and afterView() to open or
 * reobtain a EntityManager before the view is rendered and close it after the
 * view is rendered. This class is indepent to any view technologie.
 *
 * @author Ing. Mario Serrano
 * @since 2.0
 */
@Provider
public class OpenEntityManagerInViewPattern implements OpenPersistenceInViewProvider {

    /**
     * The logger.
     */
    protected final LoggingService logger = new SLF4JLoggingService(OpenEntityManagerInViewPattern.class);
    private EntityManagerFactory emf;

    /**
     * Before view.
     *
     * @return true, if successful
     */
    @Override
    public boolean beforeView() {
        boolean participate = false;
        if (TransactionSynchronizationManager.hasResource(getEntityManagerFactory())) {
            // do not modify the EntityManager: just mark the request
            // accordingly
            participate = true;

        } else {
            logger.debug("Opening JPA EntityManager in " + getClass().getName());
            try {
                EntityManager em = createEntityManager();
                TransactionSynchronizationManager.bindResource(getEntityManagerFactory(), new EntityManagerHolder(em));
            } catch (PersistenceException ex) {
                throw new DataAccessResourceFailureException("Could not create JPA EntityManager", ex);
            }
        }
        return participate;

    }

    /**
     * After view.
     *
     * @param participate the participate
     */
    @Override
    public void afterView(boolean participate) {

        if (!participate) {
            EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager.unbindResource(getEntityManagerFactory());
            logger.debug("Closing JPA EntityManager in " + getClass().getName());
            EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager());

        }
    }

    /**
     * Gets the entity manager factory.
     *
     * @return the entity manager factory
     */
    private EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            emf = Containers.get().findObject(EntityManagerFactory.class);
        }
        return emf;
    }

    /**
     * Creates the entity manager.
     *
     * @return the entity manager
     */
    private EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    @Override
    public boolean isDisabled() {
        return false;
    }
}
