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
package tools.dynamia.domain;

import org.junit.Test;
import tools.dynamia.commons.MapBuilder;
import tools.dynamia.domain.query.BooleanOp;
import tools.dynamia.domain.query.QueryCondition;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.util.QueryBuilder;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Mario A. Serrano Leones
 */
public class QueryConditionsTest {

	public QueryConditionsTest() {
	}

	@Test
	public void testEq_Object() {
		Object value = null;
		QueryCondition result = QueryConditions.eq(value);
		String expected = "select s from java.lang.String s where s.prop = :prop";
		String realResult = createRealResult(result);
		assertEquals(expected, realResult);
	}

	@Test
	public void testLike_Object() {
		String value = "value";
		QueryCondition result = QueryConditions.like(value);
		String expected = "select s from java.lang.String s where s.prop like :prop";
		String realResult = createRealResult(result);
		assertEquals(expected, realResult);
	}

	@Test
	public void testGt_Number() {
		Number value = null;
		QueryCondition result = QueryConditions.gt(value);
		String expected = "select s from java.lang.String s where s.prop > :prop";
		String realResult = createRealResult(result);
		assertEquals(expected, realResult);
	}

	@Test
	public void testGeqt_Number() {
		Number value = null;
		QueryCondition result = QueryConditions.geqt(value);
		String expected = "select s from java.lang.String s where s.prop >= :prop";
		String realResult = createRealResult(result);
		assertEquals(expected, realResult);
	}

	@Test
	public void testLt_Number() {
		Number value = null;
		QueryCondition result = QueryConditions.lt(value);
		String expected = "select s from java.lang.String s where s.prop < :prop";
		String realResult = createRealResult(result);
		assertEquals(expected, realResult);
	}

	@Test
	public void testLeqt_Number() {
		Number value = null;
		QueryCondition result = QueryConditions.leqt(value);
		String expected = "select s from java.lang.String s where s.prop <= :prop";
		String realResult = createRealResult(result);
		assertEquals(expected, realResult);
	}

	@Test
	public void testBetween_Object_Object() {
		Object valueLo = null;
		Object valueHi = null;
		QueryCondition result = QueryConditions.between(valueLo, valueHi);
		String expected = "select s from java.lang.String s where s.prop between :prop1 and :prop2";
		String realResult = createRealResult(result);
		assertEquals(expected, realResult);
	}

	@Test
	public void testIn_ObjectArr() {
		QueryCondition result = QueryConditions.in("a", "b", "c");
		String expected = "select s from java.lang.String s where s.prop in (:prop)";
		String realResult = createRealResult(result);
		assertEquals(expected, realResult);
	}

	@Test
	public void testIn_List() {
		List values = null;
		QueryCondition result = QueryConditions.in(values);
		String expected = "select s from java.lang.String s where s.prop in (:prop)";
		String realResult = createRealResult(result);
		assertEquals(expected, realResult);
	}

	@Test
	public void testIsNull() {
		QueryCondition result = QueryConditions.isNull();
		String expected = "select s from java.lang.String s where s.prop is null";
		String realResult = createRealResult(result);
		assertEquals(expected, realResult);
	}

	@Test
	public void testIsNotNull() {
		QueryCondition result = QueryConditions.isNotNull();
		String expected = "select s from java.lang.String s where s.prop is not null";
		String realResult = createRealResult(result);
		assertEquals(expected, realResult);
	}

	@Test
	public void testNotEq_Object() {
		Object value = null;
		QueryCondition result = QueryConditions.notEq(value);
		String expected = "select s from java.lang.String s where s.prop <> :prop";
		String realResult = createRealResult(result);
		assertEquals(expected, realResult);
	}

	@Test
	public void testNotIn_ObjectArr() {
		QueryCondition result = QueryConditions.notIn("a", "b", "c");
		String expected = "select s from java.lang.String s where s.prop not in (:prop)";
		String realResult = createRealResult(result);
		assertEquals(expected, realResult);
	}

	@Test
	public void testNotIn_List() {
		List values = null;
		QueryCondition result = QueryConditions.notIn(values);
		String expected = "select s from java.lang.String s where s.prop not in (:prop)";
		String realResult = createRealResult(result);
		assertEquals(expected, realResult);
	}

	@Test
	public void testIn_Subquery() {
		String subquery = "select b from Entity b";
		QueryCondition result = QueryConditions.in(subquery);
		String expected = "select s from java.lang.String s where s.prop in (" + subquery + ")";
		String realResult = createRealResult(result);
		assertEquals(expected, realResult);
	}

	@Test
	public void testIn_SubqueryComplex() {
		String subquery = "select b from Entity b";

		QueryParameters params = QueryParameters.with("prop", 1L).addGroup(
				QueryParameters.with("prop2", 2L).add("prop3", QueryConditions.in(subquery, BooleanOp.OR)),
				BooleanOp.AND);

		String expected = "select s from java.lang.String s where s.prop = :prop and (s.prop2 = :prop2 or s.prop3 in ("
				+ subquery + "))";
		String realResult = QueryBuilder.fromParameters(String.class, "s", params).toString();

		assertEquals(expected, realResult);
	}

	@Test
	public void testIn_SubqueryComplexWithParams() {
		String subquery = "select b from Entity b where b.data = :data";

		QueryParameters params = QueryParameters.with("prop", 1L).addGroup(QueryParameters.with("prop2", 2L)
				.add("prop3", QueryConditions.in(subquery, MapBuilder.put("result", 2L), BooleanOp.OR)), BooleanOp.AND);

		String expected = "select s from java.lang.String s where s.prop = :prop and (s.prop2 = :prop2 or s.prop3 in ("
				+ subquery + "))";
		String realResult = QueryBuilder.fromParameters(String.class, "s", params).toString();
		assertEquals(expected, realResult);
	}

	private String createRealResult(QueryCondition condition) {
		QueryParameters qp = QueryParameters.with("prop", condition);
		QueryBuilder qb = QueryBuilder.fromParameters(String.class, "s", qp);
		return qb.toString();
	}
}
