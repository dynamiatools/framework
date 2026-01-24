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

import java.util.HashMap;
import java.util.Map;

/**
 * Provider interface for supplying parameters to reports based on a given object.
 * <p>
 * Implementations of this interface can contribute parameters to report generation
 * by extracting or computing values from the provided object. Multiple providers
 * can be registered, and their parameters will be merged together.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Component
 * public class CustomerReportParametersProvider implements ReportParametersProvider<Customer> {
 *
 *     @Override
 *     public Map<String, Object> getParams(Customer customer) {
 *         Map<String, Object> params = new HashMap<>();
 *         params.put("customerName", customer.getName());
 *         params.put("customerCode", customer.getCode());
 *         params.put("totalOrders", customer.getOrders().size());
 *         return params;
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of object from which parameters are extracted
 * @since 1.0
 */
public interface ReportParametersProvider<T> {

    /**
     * Extracts report parameters from the given object.
     * <p>
     * Implementations should return a map containing all parameters that should be
     * available to the report based on the provided object's data.
     * </p>
     *
     * @param object the source object from which to extract parameters, may be null
     * @return a map containing parameter names and their corresponding values, never null
     */
    Map<String, Object> getParams(T object);

    /**
     * Loads all report parameters from registered providers and interceptors.
     * <p>
     * This method collects parameters from all registered {@link ReportParametersProvider}
     * instances, merges them into a single map, and then applies all registered
     * {@link ReportParametersInterceptor} instances to allow post-processing.
     * </p>
     *
     * @param object the source object for parameter extraction
     * @return a merged and intercepted map of all report parameters
     *
     * @see ReportParametersProvider
     * @see ReportParametersInterceptor
     */
    static Map<String, Object> loadParameters(Object object) {
        Map<String, Object> params = new HashMap<>();

        // Collect parameters from all providers
        Containers.get().findObjects(ReportParametersProvider.class).forEach(provider -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> providerParams = provider.getParams(object);
                if (providerParams != null) {
                    params.putAll(providerParams);
                }
            } catch (ClassCastException e) {
                // Ignore providers that don't match the object type
            }
        });

        // Apply all interceptors
        Map<String, Object> finalParams = params;
        for (ReportParametersInterceptor interceptor : Containers.get().findObjects(ReportParametersInterceptor.class)) {
            finalParams = interceptor.intercept(finalParams);
        }

        return finalParams;
    }

}
