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
package tools.dynamia.domain.query;

/**
 * The Interface QueryCondition.
 *
 * @author Mario A. Serrano Leones
 * @param <T>
 *            the generic type
 */
public interface QueryCondition<T> {

	/**
	 * Render.
	 *
	 * @param property
	 *            the property
	 * @return the string
	 */
    String render(String property);

	/**
	 * Apply.
	 *
	 * @param property
	 *            the property
	 * @param query
	 *            the query
	 */
    void apply(String property, AbstractQuery query);

	/**
	 * Gets the boolean operator.
	 *
	 * @return the boolean operator
	 */
    BooleanOp getBooleanOperator();

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
    T getValue();
}
