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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Class Inlist.
 *
 * @author Mario A. Serrano Leones
 * @param <T>
 *            the generic type
 */
public class Inlist<T> extends AbstractQueryCondition<List<T>> {

	private String subquery;
	private Map<String, Object> subqueryParams;

	/**
	 * Instantiates a new inlist.
	 */
	public Inlist() {

	}

	/**
	 * Instantiates a new inlist.
	 *
	 * @param values
	 *            the values
	 */
	public Inlist(List<T> values) {
		super(values);
	}

	/**
	 * Instantiates a new inlist.
	 *
	 * @param values
	 *            the values
	 * @param booleanOp
	 *            the boolean op
	 */
	public Inlist(List<T> values, BooleanOp booleanOp) {
		super(values, booleanOp);
	}

	/**
	 * Instantiates a new inlist.
	 *
	 * @param values
	 *            the values
	 */
	@SafeVarargs
	public Inlist(T... values) {
		super(Arrays.asList(values));
	}

	/**
	 * Instantiates a new inlist.
	 *
	 * @param booleanOp
	 *            the boolean op
	 * @param values
	 *            the values
	 */
	@SafeVarargs
	public Inlist(BooleanOp booleanOp, T... values) {
		super(Arrays.asList(values), booleanOp);
	}

	/**
	 * Create an inlist condition using a subquery (JPAQL)
	 * 
	 * @param subquery
	 */
	public Inlist(String subquery) {
		this.subquery = subquery;
	}

	/**
	 * Create an inlist condition using a subquery (JPAQL) and params are
	 * applied to subquery
	 * 
	 * @param subquery
	 * @param params
	 */
	public Inlist(String subquery, Map<String, Object> params) {
		this.subquery = subquery;
		this.subqueryParams = params;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dynamia.tools.domain.query.AbstractQueryCondition#render(java.lang.
	 * String)
	 */
	@Override
	public String render(String property) {
		String operator = getOperator();
		String content = ":" + QueryConditionUtils.cleanProperty(property);
		if (subquery != null) {
			content = subquery;
		}

		return property + " " + operator + " (" + content + ")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dynamia.tools.domain.query.AbstractQueryCondition#apply(java.lang.
	 * String, javax.persistence.Query)
	 */
	@Override
	public void apply(String property, AbstractQuery query) {
		if (query != null) {
			if (subquery != null && subqueryParams != null) {
				subqueryParams.forEach(query::setParameter);
			} else {
				query.setParameter(QueryConditionUtils.cleanProperty(property), getValue());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dynamia.tools.domain.query.AbstractQueryCondition#getOperator()
	 */
	@Override
	protected String getOperator() {
		return "in";
	}
}
