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
package tools.dynamia.domain.query;

import java.util.List;
import java.util.Map;

/**
 * The Class NotInList.
 *
 * @author Mario A. Serrano Leones
 * @param <T>
 *            the generic type
 */
public class NotInList<T> extends Inlist<T> {

	/**
	 * Instantiates a new not in list.
	 *
	 * @param values
	 *            the values
	 */
	public NotInList(T... values) {
		super(values);
	}

	/**
	 * Instantiates a new not in list.
	 *
	 * @param values
	 *            the values
	 */
	public NotInList(List<T> values) {
		super(values);
	}

	/**
	 * Instantiates a new not in list.
	 *
	 * @param booleanOp
	 *            the boolean op
	 * @param values
	 *            the values
	 */
	public NotInList(BooleanOp booleanOp, T... values) {
		super(booleanOp, values);
	}

	/**
	 * Instantiates a new not in list.
	 *
	 * @param values
	 *            the values
	 * @param booleanOp
	 *            the boolean op
	 */
	public NotInList(List<T> values, BooleanOp booleanOp) {
		super(values, booleanOp);
	}

	public NotInList(String subquery, Map<String, Object> params) {
		super(subquery, params);

	}

	public NotInList(String subquery) {
		super(subquery);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Inlist#getOperator()
	 */
	@Override
	protected String getOperator() {
		return "not in";
	}
}
