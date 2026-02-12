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

package tools.dynamia.reports;

import tools.dynamia.integration.Containers;

/**
 * Interface for providing report templates for specific objects or entity types.
 * <p>
 * Implementations of this interface are responsible for resolving and returning the appropriate
 * report template for a given object. This allows for dynamic template selection based on object type,
 * properties, or business rules. The framework automatically discovers all implementations and queries
 * them in sequence until a suitable template is found.
 * </p>
 *
 * <p>
 * <b>Key features:</b>
 * <ul>
 *   <li>Dynamic template resolution based on object type or state</li>
 *   <li>Automatic provider discovery via {@link Containers}</li>
 *   <li>Support for multiple template formats (JasperReports, HTML, PDF, etc.)</li>
 *   <li>Flexible template location (database, filesystem, classpath, remote URLs)</li>
 *   <li>Chain of responsibility pattern for template resolution</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * @Component
 * public class InvoiceTemplateProvider implements ReportTemplateProvider<Invoice> {
 *
 *     @Override
 *     public Object getTemplate(Invoice invoice) {
 *         if (invoice.getType() == InvoiceType.SIMPLE) {
 *             return loadTemplate("reports/invoice-simple.jrxml");
 *         } else if (invoice.getType() == InvoiceType.DETAILED) {
 *             return loadTemplate("reports/invoice-detailed.jrxml");
 *         }
 *         return null;
 *     }
 *
 *     private Object loadTemplate(String path) {
 *         // Load template from classpath or database
 *         return getClass().getResourceAsStream(path);
 *     }
 * }
 *
 * // Usage
 * Invoice invoice = new Invoice();
 * Object template = ReportTemplateProvider.findTemplate(invoice);
 * }</pre>
 * </p>
 *
 * @param <T> the type of object for which templates are provided
 * @author Mario A. Serrano Leones
 * @see tools.dynamia.integration.Containers
 */
public interface ReportTemplateProvider<T> {

    /**
     * Returns the report template for the given object.
     * <p>
     * Implementations should return a template object (e.g., JasperReport, InputStream, File, URL)
     * that can be used to generate a report for the specified object. Return {@code null} if this
     * provider cannot supply a template for the given object.
     * </p>
     *
     * @param object the object for which a report template is requested
     * @return the template object, or {@code null} if no template is available
     */
    Object getTemplate(T object);

    /**
     * Finds and returns a suitable report template for the given object by querying all registered
     * {@link ReportTemplateProvider} implementations.
     * <p>
     * This static utility method iterates through all available providers until one returns a non-null
     * template. Providers are discovered automatically via the {@link Containers} mechanism.
     * </p>
     *
     * @param object the object for which a template is needed
     * @return the first matching template found, or {@code null} if no provider can supply a template
     */
    static Object findTemplate(Object object) {

        Object template = null;
        for (ReportTemplateProvider tp : Containers.get().findObjects(ReportTemplateProvider.class)) {

            try {
                //noinspection unchecked
                template = tp.getTemplate(object);
                if (template != null) {
                    break;
                }
            } catch (ClassCastException e) {
                //ignore
            }
        }

        return template;
    }
}
