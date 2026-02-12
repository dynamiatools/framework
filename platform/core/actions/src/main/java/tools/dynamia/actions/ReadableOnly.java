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
package tools.dynamia.actions;

/**
 * Marker interface for actions that do not perform write operations.
 * <p>
 * Implement this interface in actions that are strictly read-only, meaning they do not modify data or state.
 * This can be used to distinguish read-only actions from those that may alter application data, enabling
 * additional security, auditing, or UI logic based on the type of operation.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     public class ViewDetailsAction implements Action, ReadableOnly {
 *         // Implementation for viewing details without modifying data
 *     }
 * </pre>
 * </p>
 *
 * @author Mario Serrano Leones
 */
public interface ReadableOnly {
    // Marker interface, no methods
}
