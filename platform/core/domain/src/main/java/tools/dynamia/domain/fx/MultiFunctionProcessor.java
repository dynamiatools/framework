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

package tools.dynamia.domain.fx;

import java.util.List;
import java.util.Map;

/**
 * Interface for processing multiple functions in batch against a dataset.
 * <p>
 * This interface defines the contract for executing multiple {@link FunctionProvider} instances
 * against a single data source with shared arguments, returning the computed results for each function.
 * This is useful for bulk calculations, aggregations, or applying multiple transformations efficiently
 * without repeated data access.
 * </p>
 *
 * <p>
 * <b>Key benefits:</b>
 * <ul>
 *   <li>Batch processing of multiple functions reduces overhead</li>
 *   <li>Shared data context and arguments across all functions</li>
 *   <li>Results mapped to their corresponding function providers</li>
 *   <li>Efficient for database queries, calculations, or transformations</li>
 *   <li>Supports parallel or optimized execution strategies</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * @Component
 * public class SqlFunctionProcessor implements MultiFunctionProcessor<DataSource, Object> {
 *
 *     @Override
 *     public Map<FunctionProvider, Object> compute(DataSource data,
 *                                                   Map<String, Object> args,
 *                                                   List<FunctionProvider> functions) {
 *         Map<FunctionProvider, Object> results = new HashMap<>();
 *
 *         try (Connection conn = data.getConnection()) {
 *             for (FunctionProvider function : functions) {
 *                 String sql = buildQuery(function, args);
 *                 Object result = executeQuery(conn, sql);
 *                 results.put(function, result);
 *             }
 *         }
 *
 *         return results;
 *     }
 * }
 *
 * // Usage
 * List<FunctionProvider> functions = Arrays.asList(totalFunction, avgFunction, maxFunction);
 * Map<String, Object> args = Map.of("year", 2024, "status", "active");
 * Map<FunctionProvider, Object> results = processor.compute(dataSource, args, functions);
 * }</pre>
 * </p>
 *
 * @param <D> the type of data source or dataset to process
 * @param <R> the type of result produced by each function
 * @author Mario A. Serrano Leones
 * @see FunctionProvider
 * @see Function
 */
public interface MultiFunctionProcessor<D, R> {

    /**
     * Computes and executes all provided functions against the given data source with the specified arguments.
     * <p>
     * This method processes all functions in the list, applying them to the data source using the shared
     * arguments. Each function's result is mapped to its corresponding {@link FunctionProvider} in the
     * returned map. Implementations may optimize execution by batching operations or executing functions
     * in parallel.
     * </p>
     *
     * @param data the data source or dataset to process (e.g., database connection, entity, collection)
     * @param args shared arguments/parameters available to all functions during execution
     * @param functions list of function providers to execute against the data
     * @return a map where each function provider is associated with its computed result
     */
    Map<FunctionProvider, R> compute(D data, Map<String, Object> args, List<FunctionProvider> functions);


}
