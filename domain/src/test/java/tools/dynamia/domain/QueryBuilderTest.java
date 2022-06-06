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
import tools.dynamia.domain.query.BooleanOp;
import tools.dynamia.domain.query.Parameter;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.util.QueryBuilder;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static tools.dynamia.domain.query.QueryConditions.eq;
import static tools.dynamia.domain.query.QueryConditions.in;
import static tools.dynamia.domain.query.QueryParameters.with;
import static tools.dynamia.domain.util.QueryBuilder.fromParameters;
import static tools.dynamia.domain.util.QueryBuilder.select;

public class QueryBuilderTest {

    @Test
    public void testGroups() {
        QueryParameters params = with("name", "123")
                .addGroup(
                        with("value", eq(123))
                                .add("id", in(BooleanOp.OR, 1, 2, 3, 4)), BooleanOp.AND)
                .orderBy("name", false);

        String queryText = fromParameters(Parameter.class, "p", params).toString();

        String expected = "select p from " + Parameter.class.getName()
                + " p where p.name like :name and (p.value = :value or p.id in (:id)) order by p.name DESC";
        assertEquals(expected, queryText);

    }

    @Test
    public void testSelectWhereWithParams() {
        QueryParameters params = with("name", "123");

        String expected = "select p from " + Parameter.class.getName() + " p where p.name like :name";
        String queryText = select(Parameter.class, "p").where(params).toString();
        assertEquals(expected, queryText);

    }

    @Test
    public void testFluentApi() {
        String expected = "select p.name, p.value from " + Parameter.class.getName() + " p where p.name = :name and p.value = :value order by p.name";
        QueryBuilder query = QueryBuilder.select("name", "value")
                .from(Parameter.class, "p")
                .where("name", eq("TEST"))
                .and("value", eq("true"))
                .orderBy("name");


        assertEquals(2, query.getQueryParameters().size());


        String queryText = query.toString();
        assertEquals(expected, queryText);
    }

    @Test
    public void joinsTest() {
        String expected = "select p from " + Parameter.class.getName() + " p left join p.test t where p.id > :pid";
        String queryText = QueryBuilder.select().from(Parameter.class, "p").leftJoin("p.test t").where("p.id", QueryConditions.gt(1)).toString();
        assertEquals(expected, queryText);
    }

    @Test
    public void shouldBuildComplexWhere() {
        String expected = "select p from " + Parameter.class.getName() + " p where (p.id + 100) > 1000";
        String queryText = QueryBuilder.select().from(Parameter.class, "p").where("(p.id + 100) > 1000").toString();
        assertEquals(expected, queryText);
    }

    @Test
    public void testOr() {
        String expected = "select p from " + Parameter.class.getName() + " p where p.value = :value or p.id in (:id)";
        String queryText = QueryBuilder.select().from(Parameter.class, "p").where("value", eq(1)).or("id", in(1, 2, 3)).toString();
        assertEquals(expected, queryText);
    }

    @Test
    public void shouldBuildCountProjection() {
        QueryBuilder query = QueryBuilder.select().from(Parameter.class, "p").leftJoin("p.test t").where("p.id", QueryConditions.gt(1));
        String expected = "select count(p.id) from " + Parameter.class.getName() + " p left join p.test t where p.id > :pid";
        String queryText = query.createProjection("count", "id");
        assertEquals(expected, queryText);

    }


    @Test
    public void shouldGenerateUpdateQuery() {
        var fields = new TreeMap<String, Object>();
        fields.put("label", "'El Param'");
        fields.put("p.id", "p.id+1");
        fields.put("value", 1000);


        var query = QueryBuilder.update(Parameter.class, "p").set(fields)
                .where("p.id > 1000");
        var expected = "update " + Parameter.class.getName() + " p set p.label='El Param', p.id=p.id+1, p.value=:newValuevalue where p.id > 1000";
        var queryText = query.toString();

        assertEquals(expected, queryText);
    }

    @Test
    public void shouldGenerateDeleteQuery() {


        var query = QueryBuilder.delete(Parameter.class, "p")
                .where("p.id > 1000");
        var expected = "delete from " + Parameter.class.getName() + " p where p.id > 1000";
        var queryText = query.toString();

        assertEquals(expected, queryText);
    }

    public void shouldSendPrintln(){
        var num = 1;
        var numer2 = 2;

    }

}
