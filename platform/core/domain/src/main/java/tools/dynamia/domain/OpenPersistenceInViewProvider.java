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

package tools.dynamia.domain;

/**
 * Interface for implementing the Open Session in View (OSIV) pattern provider.
 * <p>
 * This interface defines the contract for managing persistence context lifecycle across view rendering,
 * commonly known as the "Open Session in View" or "Open EntityManager in View" pattern. It ensures that
 * lazy-loaded entities remain accessible during view rendering by keeping the persistence context open
 * beyond the service layer.
 * </p>
 *
 * <p>
 * <b>Key responsibilities:</b>
 * <ul>
 *   <li>Open persistence context before view rendering</li>
 *   <li>Close persistence context after view rendering completes</li>
 *   <li>Handle transaction boundaries for view layer operations</li>
 *   <li>Manage nested or concurrent view rendering scenarios</li>
 *   <li>Provide mechanism to disable OSIV when needed</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Pattern flow:</b>
 * <ol>
 *   <li>{@link #beforeView()} is called before rendering begins</li>
 *   <li>View rendering occurs with open persistence context</li>
 *   <li>{@link #afterView(boolean)} is called after rendering completes</li>
 *   <li>{@link #isDisabled()} can be checked to skip OSIV processing</li>
 * </ol>
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * @Component
 * public class JpaOpenPersistenceInViewProvider implements OpenPersistenceInViewProvider {
 *
 *     @Autowired
 *     private EntityManagerFactory emf;
 *
 *     private ThreadLocal<EntityManager> contextHolder = new ThreadLocal<>();
 *
 *     @Override
 *     public boolean beforeView() {
 *         if (contextHolder.get() != null) {
 *             return false; // Already participating
 *         }
 *         EntityManager em = emf.createEntityManager();
 *         contextHolder.set(em);
 *         return true;
 *     }
 *
 *     @Override
 *     public void afterView(boolean participate) {
 *         if (participate) {
 *             EntityManager em = contextHolder.get();
 *             if (em != null) {
 *                 em.close();
 *                 contextHolder.remove();
 *             }
 *         }
 *     }
 *
 *     @Override
 *     public boolean isDisabled() {
 *         return false; // Enable OSIV
 *     }
 * }
 * }</pre>
 * </p>
 *
 * <p>
 * <b>Note:</b> While OSIV is convenient for avoiding {@code LazyInitializationException}, it should be used
 * carefully as it can lead to performance issues and unexpected database queries in the view layer.
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public interface OpenPersistenceInViewProvider {

    /**
     * Called before view rendering begins to open the persistence context.
     * <p>
     * Implementations should open a new persistence session/context if one doesn't already exist
     * for the current thread or request. This method should return {@code true} if a new context
     * was opened, or {@code false} if already participating in an existing context.
     * </p>
     *
     * @return {@code true} if a new persistence context was opened, {@code false} if already participating
     */
    boolean beforeView();

    /**
     * Called after view rendering completes to close the persistence context.
     * <p>
     * Implementations should close the persistence context only if this provider opened it
     * (i.e., {@code participate} is {@code true}). This ensures proper cleanup without interfering
     * with nested or concurrent view rendering.
     * </p>
     *
     * @param participate {@code true} if this provider opened the context in {@link #beforeView()},
     *                    {@code false} if it was already open
     */
    void afterView(boolean participate);

    /**
     * Checks whether the Open Session in View pattern is disabled.
     * <p>
     * When this method returns {@code true}, the OSIV mechanism is skipped entirely, and
     * persistence context management follows standard service-layer boundaries.
     * </p>
     *
     * @return {@code true} if OSIV is disabled, {@code false} otherwise
     */
    boolean isDisabled();
}
