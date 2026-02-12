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

package tools.dynamia.zk.ui;

import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zk.ui.sys.RequestInfo;

/**
 * Interface for custom ZK page definition loading strategies.
 * <p>
 * This interface allows implementations to customize how ZK page definitions (ZUL files) are loaded
 * and resolved. It's particularly useful for scenarios where page definitions need to be loaded from
 * non-standard locations, dynamically generated, loaded from databases, or resolved through custom
 * logic based on request context, user permissions, or application state.
 * </p>
 *
 * <p>
 * <b>Key use cases:</b>
 * <ul>
 *   <li>Loading ZUL files from database or external storage</li>
 *   <li>Dynamic ZUL generation based on user roles or permissions</li>
 *   <li>Multi-tenant applications with tenant-specific page definitions</li>
 *   <li>Custom caching strategies for page definitions</li>
 *   <li>A/B testing with different page variations</li>
 *   <li>Theme-specific page definition resolution</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * @Component
 * public class DatabasePageDefinitionLoader implements PageDefinitionLoader {
 *
 *     @Autowired
 *     private PageTemplateRepository repository;
 *
 *     @Override
 *     public PageDefinition getPageDefinition(RequestInfo ri, String path) {
 *         // Load ZUL content from database
 *         String zulContent = repository.findByPath(path);
 *
 *         if (zulContent != null) {
 *             // Parse and return page definition
 *             return Executions.getCurrent()
 *                 .getDesktop()
 *                 .getWebApp()
 *                 .getConfiguration()
 *                 .getPageDefinition(ri, zulContent);
 *         }
 *
 *         return null; // Fall back to default loader
 *     }
 * }
 *
 * // Multi-tenant example
 * @Component
 * public class TenantPageDefinitionLoader implements PageDefinitionLoader {
 *
 *     @Override
 *     public PageDefinition getPageDefinition(RequestInfo ri, String path) {
 *         String tenantId = getCurrentTenantId(ri);
 *         String tenantPath = "/tenants/" + tenantId + path;
 *
 *         // Try tenant-specific page first, fall back to default
 *         return loadPageOrDefault(ri, tenantPath, path);
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 * @see PageDefinition
 * @see RequestInfo
 */
public interface PageDefinitionLoader {

	/**
	 * Loads and returns the ZK page definition for the specified path.
	 * <p>
	 * This method is called by the ZK framework when a page needs to be rendered. Implementations
	 * should resolve the path to a {@link PageDefinition}, which can be loaded from files, databases,
	 * or generated dynamically. Return {@code null} to indicate that this loader cannot handle the
	 * path, allowing other loaders or the default ZK mechanism to handle it.
	 * </p>
	 *
	 * @param ri the request information containing context about the current HTTP request
	 * @param path the path to the page to be loaded (e.g., "/views/home.zul")
	 * @return the page definition for the specified path, or {@code null} if this loader cannot handle it
	 */
	PageDefinition getPageDefinition(RequestInfo ri, String path);

}
