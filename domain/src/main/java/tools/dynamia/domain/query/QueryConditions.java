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

import tools.dynamia.commons.DateRange;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Usefull class for creating query conditions or criterias, you can use it using import
 * static.
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class QueryConditions {

	/**
	 * Create an equal condition '='.
	 *
	 * @param value
	 *            the value
	 * @return the query condition
	 */
	public static QueryCondition eq(Object value) {
		return new Equals(value);
	}

	/**
	 * Create an equal condition '='.
	 *
	 * @param value
	 *            the value
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition eq(Object value, BooleanOp booleanOp) {
		return new Equals(value, booleanOp);
	}

	/**
	 * Create a like condition.
	 *
	 * @param value
	 *            the value
	 * @return the query condition
	 */
	public static QueryCondition like(Object value) {
		return new LikeEquals(value);
	}

	/**
	 * Create a like condition, it transform value to a searcheable string ex:
	 * value="find this" become value="%find%this%".
	 *
	 * @param value
	 *            the value
	 * @param searcheableString
	 *            the searcheable string
	 * @return the query condition
	 */
	public static QueryCondition like(Object value, boolean searcheableString) {
		return new LikeEquals(value, searcheableString);
	}

	/**
	 * Create a like condition, it transform value to a searcheable string ex:
	 * value="find this" become value="%find%this%".
	 *
	 * @param value
	 *            the value
	 * @param searcheableString
	 *            the searcheable string
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition like(Object value, boolean searcheableString, BooleanOp booleanOp) {
		return new LikeEquals(value, searcheableString, booleanOp);
	}

	/**
	 * Create a like condition for starts with value.
	 *
	 * @param value
	 *            the value
	 * @return the query condition
	 */
	public static QueryCondition startsWith(String value) {
		return new StartsWith(value);
	}

	/**
	 * Create a like condition for starts with value.
	 *
	 * @param value
	 *            the value
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition startsWith(String value, BooleanOp booleanOp) {
		return new StartsWith(value, booleanOp);
	}

	/**
	 * Create a like condition for ends with value.
	 *
	 * @param value
	 *            the value
	 * @return the query condition
	 */
	public static QueryCondition endsWith(String value) {
		return new EndsWith(value);
	}

	/**
	 * Create a like condition for ends with value.
	 *
	 * @param value
	 *            the value
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition endsWith(String value, BooleanOp booleanOp) {
		return new EndsWith(value, booleanOp);
	}

	/**
	 * Create a greate than condition '>'.
	 *
	 * @param value
	 *            the value
	 * @return the query condition
	 */
	public static QueryCondition gt(Number value) {
		return new GreaterThan(value);
	}

	/**
	 * Create a greate than condition '>'.
	 *
	 * @param value
	 *            the value
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition gt(Number value, BooleanOp booleanOp) {
		return new GreaterThan(value, booleanOp);
	}

	/**
	 * Create a greate than condition '>'.
	 *
	 * @param value
	 *            the value
	 * @return the query condition
	 */
	public static QueryCondition gt(Date value) {
		return new GreaterThan(value);
	}


	/**
	 * Create a greate than condition '>'.
	 *
	 * @param value
	 *            the value
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition gt(Date value, BooleanOp booleanOp) {
		return new GreaterThan(value, booleanOp);
	}

	/**
	 * Create a greate equals than condition '>='.
	 *
	 * @param value
	 *            the value
	 * @return the query condition
	 */
	public static QueryCondition geqt(Number value) {
		return new GreaterEqualsThan(value);
	}

	/**
	 * Create a greate equals than condition '>='.
	 *
	 * @param value
	 *            the value
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition geqt(Number value, BooleanOp booleanOp) {
		return new GreaterEqualsThan(value, booleanOp);
	}

	/**
	 * Create a greate equals than condition '>='.
	 *
	 * @param value
	 *            the value
	 * @return the query condition
	 */
	public static QueryCondition geqt(Date value) {
		return new GreaterEqualsThan(value);
	}

	/**
	 * Create a greate equals than condition '>='.
	 *
	 * @param value
	 *            the value
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition geqt(Date value, BooleanOp booleanOp) {
		return new GreaterEqualsThan(value, booleanOp);
	}

	/**
	 * Create a less than condition '<'.
	 *
	 * @param value
	 *            the value
	 * @return the query condition
	 */
	public static QueryCondition lt(Number value) {
		return new LessThan(value);
	}

	/**
	 * Create a less than condition '<'.
	 *
	 * @param value
	 *            the value
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition lt(Number value, BooleanOp booleanOp) {
		return new LessThan(value, booleanOp);
	}

	/**
	 * Create a less than condition '<'.
	 *
	 * @param value
	 *            the value
	 * @return the query condition
	 */
	public static QueryCondition lt(Date value) {
		return new LessThan(value);
	}

	/**
	 * Create a less than condition '<'.
	 *
	 * @param value
	 *            the value
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition lt(Date value, BooleanOp booleanOp) {
		return new LessThan(value, booleanOp);
	}

	/**
	 * Create a less equals than condition '<='.
	 *
	 * @param value
	 *            the value
	 * @return the query condition
	 */
	public static QueryCondition leqt(Number value) {
		return new LessEqualsThan(value);
	}

	/**
	 * Create a less equals than condition '<='.
	 *
	 * @param value
	 *            the value
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition leqt(Number value, BooleanOp booleanOp) {
		return new LessEqualsThan(value, booleanOp);
	}

	/**
	 * Create a less equals than condition '<='.
	 *
	 * @param value
	 *            the value
	 * @return the query condition
	 */
	public static QueryCondition leqt(Date value) {
		return new LessEqualsThan(value);
	}

	/**
	 * Create a less equals than condition '<='.
	 *
	 * @param value
	 *            the value
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition leqt(Date value, BooleanOp booleanOp) {
		return new LessEqualsThan(value, booleanOp);
	}

	/**
	 * Create a between condition.
	 *
	 * @param valueLo
	 *            the value lo
	 * @param valueHi
	 *            the value hi
	 * @return the query condition
	 */
	public static QueryCondition between(Object valueLo, Object valueHi) {
		return new Between(valueLo, valueHi);
	}

	/**
	 * Create a between condition.
	 *
	 * @param valueLo
	 *            the value lo
	 * @param valueHi
	 *            the value hi
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition between(Object valueLo, Object valueHi, BooleanOp booleanOp) {
		return new Between(valueLo, valueHi, booleanOp);
	}

	/**
	 * Between.
	 *
	 * @param dateRange
	 *            the date range
	 * @return the query condition
	 */
	public static QueryCondition between(DateRange dateRange) {
		return new Between(dateRange.getStartDate(), dateRange.getEndDate());
	}

	/**
	 * Between.
	 *
	 * @param dateRange
	 *            the date range
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition between(DateRange dateRange, BooleanOp booleanOp) {
		return new Between(dateRange.getStartDate(), dateRange.getStartDate(), booleanOp);
	}

	/**
	 * Create an in condition.
	 *
	 * @param values
	 *            the values
	 * @return the query condition
	 */
	public static QueryCondition in(Object... values) {
		return new Inlist(values);
	}

	/**
	 * Create an in condition.
	 *
	 * @param booleanOp
	 *            the boolean op
	 * @param values
	 *            the values
	 * @return the query condition
	 */
	public static QueryCondition in(BooleanOp booleanOp, Object... values) {
		return new Inlist(booleanOp, values);
	}

	/**
	 * Create an in condition.
	 *
	 * @param values
	 *            the values
	 * @return the query condition
	 */
	public static QueryCondition in(List values) {
		return new Inlist(values);
	}

	/**
	 * Create an in condition.
	 *
	 * @param values
	 *            the values
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition in(List values, BooleanOp booleanOp) {
		return new Inlist(values, booleanOp);
	}

	/**
	 * Create an in condition using a subquery (JPAQL)
	 *
	 * @param subquery
	 */
	public static QueryCondition in(String subquery) {
		return new Inlist(subquery);
	}

	/**
	 * Create an in condition using a subquery (JPAQL)
	 *
	 * @param subquery
	 */
	public static QueryCondition in(String subquery, BooleanOp booleanOp) {
		Inlist in = new Inlist(subquery);
		in.setBooleanOperator(booleanOp);
		return in;
	}

	/**
	 * Create an in condition using a subquery (JPAQL) and params are applied to
	 * subquery
	 *
	 * @param subquery
	 * @param params
	 */
	public static QueryCondition in(String subquery, Map<String, Object> params) {
		return new Inlist(subquery, params);
	}

	/**
	 * Create an in condition using a subquery (JPAQL) and params are applied to
	 * subquery
	 *
	 * @param subquery
	 * @param params
	 */
	public static QueryCondition in(String subquery, Map<String, Object> params, BooleanOp booleanOp) {
		Inlist inlist = new Inlist(subquery, params);
		inlist.setBooleanOperator(booleanOp);
		return inlist;
	}

	/**
	 * Create an is null condition.
	 *
	 * @return the query condition
	 */
	public static QueryCondition isNull() {
		return new IsNull();
	}

	/**
	 * Create an is null condition.
	 *
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition isNull(BooleanOp booleanOp) {
		return new IsNull(booleanOp);
	}

	/**
	 * Create an is not null condition.
	 *
	 * @return the query condition
	 */
	public static QueryCondition isNotNull() {
		return new IsNotNull();
	}

	/**
	 * Create an is not null condition.
	 *
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition isNotNull(BooleanOp booleanOp) {
		return new IsNotNull(booleanOp);
	}

	/**
	 * Create a not equals condition '<>'.
	 *
	 * @param value
	 *            the value
	 * @return the query condition
	 */
	public static QueryCondition notEq(Object value) {
		return new NotEquals(value);
	}

	/**
	 * Create a not equals condition '<>'.
	 *
	 * @param value
	 *            the value
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition notEq(Object value, BooleanOp booleanOp) {
		return new NotEquals(value, booleanOp);
	}

	/**
	 * Create a not in condition.
	 *
	 * @param values
	 *            the values
	 * @return the query condition
	 */
	public static QueryCondition notIn(Object... values) {
		return new NotInList(values);
	}

	/**
	 * Create a not in condition.
	 *
	 * @param booleanOp
	 *            the boolean op
	 * @param values
	 *            the values
	 * @return the query condition
	 */
	public static QueryCondition notIn(BooleanOp booleanOp, Object... values) {
		return new NotInList(booleanOp, values);
	}

	/**
	 * Create a not in condition.
	 *
	 * @param values
	 *            the values
	 * @return the query condition
	 */
	public static QueryCondition notIn(List values) {
		return new NotInList(values);
	}

	/**
	 * Create a not in condition.
	 *
	 * @param values
	 *            the values
	 * @param booleanOp
	 *            the boolean op
	 * @return the query condition
	 */
	public static QueryCondition notIn(List values, BooleanOp booleanOp) {
		return new NotInList(values, booleanOp);
	}

	/**
	 * Create a NOT in condition using a subquery (JPAQL)
	 *
	 * @param subquery
	 */
	public static QueryCondition notIn(String subquery) {
		return new NotInList(subquery);
	}

	/**
	 * Create a NOT in condition using a subquery (JPAQL)
	 *
	 * @param subquery
	 */
	public static QueryCondition notIn(String subquery, BooleanOp booleanOp) {
		NotInList notin = new NotInList(subquery);
		notin.setBooleanOperator(booleanOp);
		return notin;
	}

	/**
	 * Create a NOT in condition using a subquery (JPAQL) and params are applied
	 * to subquery
	 *
	 * @param subquery
	 * @param params
	 */
	public static QueryCondition notIn(String subquery, Map<String, Object> params) {
		return new NotInList(subquery, params);
	}

	/**
	 * Create a NOT in condition using a subquery (JPAQL) and params are applied
	 * to subquery
	 *
	 * @param subquery
	 * @param params
	 */
	public static QueryCondition notIn(String subquery, Map<String, Object> params, BooleanOp booleanOp) {
		NotInList notin = new NotInList(subquery, params);
		notin.setBooleanOperator(booleanOp);
		return notin;
	}

	/**
	 * Create a group of query conditions.
	 *
	 * @param conditions
	 *            the conditions
	 * @return the query condition
	 */
	public static QueryCondition group(QueryCondition... conditions) {
		return new QueryConditionGroup(conditions);
	}

	/**
	 * Create a group of query conditions.
	 *
	 * @param booleanOp
	 *            the boolean op
	 * @param conditions
	 *            the conditions
	 * @return the query condition
	 */
	public static QueryCondition group(BooleanOp booleanOp, QueryCondition... conditions) {
		return new QueryConditionGroup(booleanOp, conditions);
	}

}
