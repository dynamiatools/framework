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

/**
 * Interface for providing named functions that can be executed dynamically within the framework.
 * <p>
 * This interface allows components to expose reusable functions or expressions that can be evaluated
 * at runtime. Functions can represent business logic, calculations, queries, scripts, or any other
 * executable code that needs to be referenced by name. This is particularly useful for dynamic
 * behavior, plugin architectures, rule engines, and expression evaluation systems.
 * </p>
 *
 * <p>
 * <b>Key use cases:</b>
 * <ul>
 *   <li>Dynamic field calculations and computed properties</li>
 *   <li>Business rule definitions and validation logic</li>
 *   <li>Query builders and filter expressions</li>
 *   <li>Scripting and formula evaluation</li>
 *   <li>Plugin-based extensibility</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * @Component
 * public class TaxCalculationFunction implements FunctionProvider {
 *
 *     @Override
 *     public String getName() {
 *         return "calculateTax";
 *     }
 *
 *     @Override
 *     public String getFunction() {
 *         return "amount * 0.19"; // 19% tax formula
 *     }
 * }
 *
 * // Or for SQL functions
 * @Component
 * public class FullNameFunction implements FunctionProvider {
 *
 *     @Override
 *     public String getName() {
 *         return "fullName";
 *     }
 *
 *     @Override
 *     public String getFunction() {
 *         return "CONCAT(firstName, ' ', lastName)";
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @author Mario A. Serrano Leones
 * @see Function
 * @see MultiFunctionProcessor
 */
public interface FunctionProvider {

    /**
     * Returns the unique name of this function.
     * <p>
     * The name is used to identify and reference the function within the system. It should be
     * unique, descriptive, and follow naming conventions (e.g., camelCase or snake_case).
     * </p>
     *
     * @return the function name
     */
    String getName();

    /**
     * Returns the function definition, expression, or implementation code.
     * <p>
     * The returned string can be a formula, expression, SQL snippet, script code, or any other
     * representation of the function logic. The format depends on how the function will be
     * processed by the function processor or evaluation engine.
     * </p>
     *
     * @return the function definition or expression
     */
    String getFunction();

}
