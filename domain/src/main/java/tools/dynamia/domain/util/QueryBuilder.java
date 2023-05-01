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
package tools.dynamia.domain.util;

import tools.dynamia.commons.BeanMap;
import tools.dynamia.commons.BeanSorter;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.AbstractQueryCondition;
import tools.dynamia.domain.query.BooleanOp;
import tools.dynamia.domain.query.Group;
import tools.dynamia.domain.query.QueryCondition;
import tools.dynamia.domain.query.QueryConditionGroup;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is a simple JPA query text builder.
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"rawtypes"})
public class QueryBuilder implements Cloneable {


    enum QueryType {
        SELECT, UPDATE, DELETE
    }

    private QueryType queryType = QueryType.SELECT;

    private static final String FROM_WORD = "from";


    private QueryParameters queryParameters;
    private Class<?> type;
    private String var = "t";
    private String select;
    private String from;
    private List<String> wheres = new ArrayList<>();
    private List<String> orders = new ArrayList<>();
    private List<String> groups = new ArrayList<>();
    private List<String> joins = new ArrayList<>();
    private Class resultType;
    private String[] fields;
    private String customSelect;
    private String customFrom;
    private boolean builded;
    private Map<String, Object> mapFields;


    public boolean isAppendVarNameToFields() {
        return appendVarNameToFields;
    }

    public void setAppendVarNameToFields(boolean appendVarNameToFields) {
        this.appendVarNameToFields = appendVarNameToFields;
    }

    private boolean appendVarNameToFields = true;

    /**
     * The Constant SPACE.
     */
    private static final String SPACE = " ";

    /**
     * Instantiates a new query builder.
     */
    private QueryBuilder() {
    }

    /**
     * Build a new QueryBuilder for the type, example: String queryText =
     * QueryBuilder.select(Person.class,"p").toString(); is the same that:
     * String queryText = "select p from Person p"; but QueryBuilder allow you
     * use or, ands, create projections
     *
     * @param type   the type
     * @param var    the var
     * @param fields the fields
     * @return the query builder
     */
    public static QueryBuilder select(Class<?> type, String var, String... fields) {
        return select(type, null, var, fields);
    }

    /**
     * Build a new QueryBuilder for the type, example: String queryText =
     * QueryBuilder.select(Person.class,"p").toString(); is the same that:
     * String queryText = "select p from Person p"; but QueryBuilder allow you
     * use or, ands, create projections using a result type with appropiate
     * constructor
     *
     * @param entityType the type
     * @param var        the var
     * @param fields     the fields
     * @return the query builder
     */
    public static QueryBuilder select(Class<?> entityType, Class resultType, String var, String... fields) {
        QueryBuilder qb = new QueryBuilder();
        qb.type = entityType;
        qb.var = var;
        qb.resultType = resultType;
        qb.fields = fields;
        qb.queryType = QueryType.SELECT;
        return qb;
    }

    /**
     * If fields is empty means all fields
     *
     */
    public static QueryBuilder select(String... fields) {
        QueryBuilder qb = new QueryBuilder();
        qb.fields = fields;
        qb.queryType = QueryType.SELECT;
        return qb;
    }

    /**
     * Query type and query var
     *
     */
    public QueryBuilder from(Class<?> entityType, String var) {
        this.type = entityType;
        this.var = var;
        this.customFrom = null;
        return this;
    }

    /**
     * List result type can be different from query type, use this to create DTO
     * like collections
     *
     */
    public QueryBuilder resultType(Class<?> resultType) {
        this.resultType = resultType;
        return this;
    }

    public QueryBuilder build() {
        if (!builded) {
            toString();
            builded = true;
        }
        return this;
    }

    /**
     * Crearte a QueryBuilder object using the QueryParameters in the where
     * clause example: QueryParameters qp =
     * QueryParameters.with("name","mario"); String queryText =
     * QueryBuilder.fromParameters(Person.class,"p",qp).toString(); produce:
     * "select p from Person p where p.name like :name";
     *
     * @param type       the type
     * @param var        the var
     * @param parameters the parameters
     * @return the query builder
     */
    public static QueryBuilder fromParameters(Class<?> type, String var, QueryParameters parameters) {

        return QueryBuilder.select().from(type, var).where(parameters);
    }

    private void configureParameters() {
        if (queryParameters == null || builded) {
            return;
        }

        if (!queryParameters.isEmpty()) {
            for (String property : queryParameters.getSortedKeys()) {
                Object value = queryParameters.get(property);
                if (value != null) {
                    if (value instanceof QueryCondition qc) {
                        if (qc instanceof QueryConditionGroup qcGroup) {
                            for (QueryCondition nestedQC : qcGroup.getValue()) {
                                addCondition(property, nestedQC, nestedQC.getBooleanOperator());
                            }
                        } else {
                            addCondition(property, qc, qc.getBooleanOperator());
                        }
                    } else if (value instanceof String && queryParameters.isAutocreateSearcheableStrings()) {
                        addCondition(property, QueryConditions.like(value), BooleanOp.AND);
                    } else {
                        String condition = QueryConditions.eq(value).render(property);
                        condition = appendVarNameToFields ? var + "." + condition : condition;
                        and(condition);
                    }
                }
            }
        }
        if (queryParameters.getGroups() != null) {
            for (Group group : queryParameters.getGroups()) {
                if (group.params() != null && !group.params().isEmpty()) {
                    QueryBuilder subquery = QueryBuilder.fromParameters(type, var, group.params()).build();
                    String subqueryWhere = "(" + subquery.parse(subquery.wheres, " ") + ")";

                    if (group.booleanOp() == BooleanOp.AND) {
                        and(subqueryWhere);
                    } else {
                        or(subqueryWhere);
                    }
                }
            }
        }

        if (queryParameters.isSorted()) {
            BeanSorter sorter = queryParameters.getSorter();
            String direction = sorter.isAscending() ? "ASC" : "DESC";
            if (sorter.getColumnName().toLowerCase().endsWith(" desc")
                    || sorter.getColumnName().toLowerCase().endsWith(" asc")) {
                direction = " ";
            }

            String orderField = sorter.getColumnName() + " " + direction;
            orderBy(orderField);

        }
    }

    /**
     * Adds the condition.
     *
     * @param property  the property
     * @param qc        the qc
     */
    private void addCondition(String property, QueryCondition qc, BooleanOp booleanOp) {
        String condition = renderCondition(property, qc);
        if (condition != null) {


            if (booleanOp == BooleanOp.AND) {
                and(condition);
            } else if (booleanOp == BooleanOp.OR) {
                or(condition);
            }


            if (qc instanceof AbstractQueryCondition) {
                ((AbstractQueryCondition) qc).setBooleanOperator(booleanOp);
            }
        }
    }

    private String renderCondition(String property, QueryCondition qc) {
        String render = qc.render(property);
        return checkFieldVar(render);
    }

    /**
     * Where.
     *
     * @param conditions the conditions
     * @return the query builder
     */
    public QueryBuilder where(String conditions) {
        if (!wheres.contains(conditions)) {
            wheres.add(conditions);
        }
        return this;
    }

    /**
     * Where.
     *
     * @param params the params
     * @return the query builder
     */
    public QueryBuilder where(QueryParameters params) {
        this.queryParameters = params;
        return this;
    }

    public QueryBuilder where(String field, QueryCondition condition) {
        where(QueryParameters.with(field, condition));
        where(renderCondition(field, condition));
        return this;

    }

    /**
     * And.
     *
     * @param condition the condition
     * @return the query builder
     */
    public QueryBuilder and(String condition) {
        return and(condition, true);
    }

    /**
     * And.
     *
     * @param condition the condition
     * @param append    the append
     * @return the query builder
     */
    public QueryBuilder and(String condition, boolean append) {
        if (!wheres.contains(condition)) {
            if (!wheres.isEmpty()) {
                condition = "and " + condition;
            }
            if (append) {
                where(condition);
            }
        }
        return this;
    }

    public QueryBuilder and(String field, QueryCondition condition) {
        addCondition(field, condition, BooleanOp.AND);
        getQueryParameters().add(field, condition);
        return this;
    }

    /**
     * Or.
     *
     * @param condition the condition
     * @return the query builder
     */
    public QueryBuilder or(String condition) {
        return or(condition, true);
    }

    /**
     * Or.
     *
     * @param condition the condition
     * @param append    the append
     * @return the query builder
     */
    public QueryBuilder or(String condition, boolean append) {
        if (!wheres.contains(condition)) {
            if (!wheres.isEmpty()) {
                condition = "or " + condition;
            }
            if (append) {
                where(condition);
            }
        }
        return this;
    }

    public QueryBuilder or(String field, QueryCondition condition) {
        addCondition(field, condition, BooleanOp.OR);
        getQueryParameters().add(field, condition);
        return this;
    }

    /**
     * Order by.
     *
     * @param field         the field
     * @param anotherFields the another fields
     * @return the query builder
     */
    public QueryBuilder orderBy(String field, String... anotherFields) {
        addField(orders, field);
        addFields(orders, anotherFields);
        return this;
    }

    /**
     * Group by.
     *
     * @param field         the field
     * @param anotherFields the another fields
     * @return the query builder
     */
    public QueryBuilder groupBy(String field, String... anotherFields) {
        addField(groups, field);
        addFields(groups, anotherFields);
        return this;
    }

    private void addFields(List<String> list, String[] fields) {
        if (fields != null) {
            for (String field : fields) {
                addField(list, field);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return switch (queryType) {
            case SELECT -> buildSelect();
            case UPDATE -> buildUpdate();
            case DELETE -> buildDelete();
            default -> buildSelect();
        };
    }

    private String buildSelect() {
        configureParameters();

        if (customSelect != null && !customSelect.isEmpty()) {
            select = customSelect;
        } else {
            String parseFields = var;
            if (fields != null && fields.length > 0) {
                List<String> fieldsList = new ArrayList<>();
                for (String field : fields) {
                    addField(fieldsList, field);
                }
                parseFields = parse(fieldsList, ", ");
            }

            String fieldList = parseFields;
            if (resultType != null && resultType != BeanMap.class) {
                fieldList = " new " + resultType.getName() + "(" + parseFields + ") ";
            }
            select = "select " + fieldList;
        }

        if (customFrom != null) {
            from = customFrom + SPACE;
        } else {
            from = FROM_WORD + SPACE + type.getName() + SPACE + var;
        }

        String whereWord = getWhereWord();
        String parseGroups = groups.isEmpty() ? "" : " group by " + parse(groups, ", ");
        String parseOrders = orders.isEmpty() ? "" : " order by " + parse(orders, ", ");
        String parseJoins = joins.isEmpty() ? "" : parse(joins, " ") + SPACE;

        return select + SPACE + from + SPACE + parseJoins + whereWord + SPACE + parse(wheres, " ") + parseGroups + parseOrders;
    }

    private String buildUpdate() {
        configureParameters();

        if (mapFields == null || mapFields.isEmpty()) {
            throw new ValidationError("No fields to update provided");
        }


        String parseFields = var;

        List<String> fieldsList = new ArrayList<>();
        mapFields.forEach((field, value) -> {

            String parsedValue;
            if (value instanceof String) {
                parsedValue = (String) value;
            } else {
                String key = "newValue" + field.replace(".", "");
                getQueryParameters().add(key, value);
                parsedValue = ":" + key;
            }

            var exp = field + "=" + parsedValue;
            addField(fieldsList, exp);
        });

        parseFields = parse(fieldsList, ", ");


        String fieldList = parseFields;

        String update = "update";


        if (customFrom != null) {
            from = customFrom + SPACE;
        } else {
            from = SPACE + type.getName() + SPACE + var;
        }

        String whereWord = getWhereWord();
        String parseJoins = joins.isEmpty() ? "" : parse(joins, " ") + SPACE;

        return update + from + SPACE + parseJoins + "set" + SPACE + fieldList + SPACE + whereWord + SPACE + parse(wheres, " ");
    }

    private String buildDelete() {
        configureParameters();

        String delete = "delete";


        if (customFrom != null) {
            from = customFrom + SPACE;
        } else {
            from = SPACE + type.getName() + SPACE + var;
        }

        String whereWord = getWhereWord();
        String parseJoins = joins.isEmpty() ? "" : parse(joins, " ") + SPACE;

        return delete + SPACE + FROM_WORD + from + SPACE + parseJoins + whereWord + SPACE + parse(wheres, " ");
    }


    private String parse(List<String> list, String delimiter) {
        String parsed = "";
        if (!list.isEmpty()) {
            parsed = String.join(delimiter, list);
        }
        return parsed;
    }

    private void addField(List<String> list, String field) {
        String toAdd = checkFieldVar(field);

        if (!list.contains(toAdd)) {
            list.add(toAdd);
        }
    }

    private String checkFieldVar(String field) {
        String toAdd = field;
        if (!field.startsWith("(") && !field.contains(var + ".") && appendVarNameToFields) {
            toAdd = var + "." + field;
        }
        return toAdd;
    }

    /**
     * Gets the where word.
     *
     * @return the where word
     */
    private String getWhereWord() {
        String whereWord = "";
        if (wheres != null && !wheres.isEmpty()) {
            whereWord = "where";
        }
        return whereWord;
    }

    /**
     * create a projection using a jpql function name (count, sum,avg...)
     *
     * @param function the function
     * @param field    the field
     * @return the string
     */
    public String createProjection(String function, String field) {
        build();
        String whereWord = getWhereWord();
        String varName = getVarName();

        String projection = "select " + function + "(" + varName + field + ") ";
        String parseJoins = joins.isEmpty() ? "" : parse(joins, " ") + SPACE;
        return projection + from + SPACE + parseJoins + whereWord + SPACE + parse(wheres, " ");
    }

    public String getVarName() {
        return appendVarNameToFields ? var + "." : "";
    }

    /**
     * Custom select.
     *
     * @param select the select
     * @return the query builder
     */
    public QueryBuilder customSelect(String select) {
        this.customSelect = select;
        return this;
    }

    /**
     * Custom from.
     *
     * @param from the from
     * @return the query builder
     */
    public QueryBuilder customFrom(String from) {
        this.customFrom = from;
        return this;
    }

    public QueryBuilder innerJoin(String join) {
        joins.add("inner join " + join);
        return this;
    }

    public QueryBuilder leftJoin(String join) {
        joins.add("left join " + join);
        return this;
    }

    public QueryBuilder rightJoin(String join) {
        joins.add("right join " + join);
        return this;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Gets the query parameters.
     *
     * @return the query parameters
     */
    public QueryParameters getQueryParameters() {
        if (queryParameters == null) {
            queryParameters = new QueryParameters();
            queryParameters.setType(getType());
        }
        return queryParameters;
    }

    public Class getResultType() {
        return resultType;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public QueryBuilder clone() {
        QueryBuilder qb = new QueryBuilder();
        qb.var = var;
        qb.from = from;
        qb.customFrom = customFrom;
        qb.select = select;
        qb.type = type;
        qb.resultType = resultType;
        qb.fields = fields;
        qb.wheres = new ArrayList<>(wheres);
        qb.joins = new ArrayList<>(joins);
        qb.orders = new ArrayList<>(orders);
        qb.groups = new ArrayList<>(groups);
        qb.queryParameters = queryParameters;
        qb.appendVarNameToFields = appendVarNameToFields;


        return qb;
    }

    public String[] getFields() {
        return fields;
    }

    /**
     * Update query
     *
     */
    public static QueryBuilder update(Class entityType, String var) {
        var builder = new QueryBuilder();
        builder.queryType = QueryType.UPDATE;
        builder.type = entityType;
        builder.var = var;
        builder.customSelect = null;

        return builder;
    }

    public QueryBuilder set(Map<String, Object> fields) {
        if (queryType != QueryType.UPDATE) {
            throw new ValidationError("Query builder type should be " + QueryType.UPDATE);
        }

        this.mapFields = fields;
        return this;
    }

    public static QueryBuilder delete(Class entityType, String var) {
        var builder = new QueryBuilder();
        builder.queryType = QueryType.DELETE;
        builder.type = entityType;
        builder.var = var;
        builder.customSelect = null;

        return builder;
    }

}
