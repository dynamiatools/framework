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
package tools.dynamia.domain.neo4j;


import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.neo4j.transaction.SessionFactoryUtils;
import org.springframework.data.neo4j.transaction.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.OpenPersistenceInViewProvider;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.sterotypes.Provider;

/**
 *
 * @author Mario A. Serrano Leones
 */
@Provider
public class OpenNeo4jSessionInViewProvider implements OpenPersistenceInViewProvider {

	private static final String NEO4J_PARTICIPATE = "NEO4J.PARTICIPATE";
	/**
	 * The logger.
	 */
	protected LoggingService logger = new SLF4JLoggingService(OpenNeo4jSessionInViewProvider.class);



	/**
	 * Before view.
	 *
	 * @return true, if successful
	 */
	@Override
	public  boolean beforeView() {
		boolean participate = false;
		SessionFactory sessionFactory = getSessionFactory();
		if (sessionFactory != null) {
			if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
				participate = true;
			} else {
				logger.debug("Opening Neo4j Session in " + getClass().getName());
				try {
					Session session = createSession();
					if (session != null) {
						TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
					}
				} catch (Exception ex) {
					throw new DataAccessResourceFailureException("Could not create Neo4j Session", ex);
				}
			}
		}
		return participate;
	}

	/**
	 * After view.
	 *
	 * @param participate
	 *            the participate
	 */
	@Override
	public void afterView(boolean participate) {

		if (!participate) {
			SessionFactory sessionFactory = getSessionFactory();
			if (sessionFactory != null) {
				SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager
						.unbindResource(sessionFactory);
				logger.debug("Closing Neo4 Session in " + getClass().getName());
				SessionFactoryUtils.closeSession(sessionHolder.getSession());
			}

		}
	}

	private SessionFactory getSessionFactory() {
		return Containers.get().findObject(SessionFactory.class);
	}

	private Session createSession() {
		Session session = null;
		if (getSessionFactory() != null) {
			session = getSessionFactory().openSession();
		}
		return session;
	}
}
