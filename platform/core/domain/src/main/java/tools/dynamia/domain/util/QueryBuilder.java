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
import tools.dynamia.domain.query.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A fluent API builder for constructing JPA query strings programmatically.
 * Supports SELECT, UPDATE, and DELETE operations with dynamic conditions,
 * joins, ordering, grouping, and parameter binding.
 *
 * <p>This class simplifies the creation of complex JPQL queries by providing
 * a chainable interface for building query components. It integrates with
 * {@link QueryParameters} to handle dynamic conditions and parameter values.</p>
 *
 * <p>Example usage for a SELECT query:</p>
 * <pre>{@code
 * QueryBuilder qb = QueryBuilder.select(Person.class, "p", "name", "email")
 *     .where("p.age > 18")
 *     .orderBy("name")
 *     .build();
 * String jpql = qb.toString(); // "select p.name, p.email from Person as p where p.age > 18 order by name"
 * }</pre>
 *
 * <p>Example with QueryParameters:</p>
 * <pre>{@code
 * QueryParameters params = QueryParameters.with("name", "John")
 *     .add("age", QueryConditions.gt(18));
 * QueryBuilder qb = QueryBuilder.fromParameters(Person.class, "p", params);
 * String jpql = qb.toString(); // "select p from Person as p where p.name like :name and p.age > :age"
 * }</pre>
 *
 * <p>Example for an UPDATE query:</p>
 * <pre>{@code
 * Map<String, Object> updates = Map.of("status", "ACTIVE", "lastUpdate", new Date());
 * QueryBuilder qb = QueryBuilder.update(Person.class, "p")
 *     .set(updates)
 *     .where("p.id = 123")
 *     .build();
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"rawtypes"})
public class QueryBuilder implements Cloneable {

    /**
     * Constant for the SQL AS keyword.
     */
    public static final String AS = "as";

    /**
     * Enumeration of supported query types.
     */
    enum QueryType {
        /**
         * SELECT query type
         */
        SELECT,
        /**
         * UPDATE query type
         */
        UPDATE,
        /**
         * DELETE query type
         */
        DELETE
    }

    /**
     * The type of query being built (SELECT, UPDATE, or DELETE)
     */
    private QueryType queryType = QueryType.SELECT;

    /**
     * Constant for the FROM keyword
     */
    private static final String FROM_WORD = "from";

    /**
     * Query parameters for dynamic conditions and parameter binding
     */
    private QueryParameters queryParameters;

    /**
     * The entity type for the query
     */
    private Class<?> type;

    /**
     * The alias/variable name for the entity in the query (default: "t")
     */
    private String var = "t";

    /**
     * The SELECT clause of the query
     */
    private String select;

    /**
     * The FROM clause of the query
     */
    private String from;

    /**
     * List of WHERE conditions
     */
    private List<String> wheres = new ArrayList<>();

    /**
     * List of ORDER BY fields
     */
    private List<String> orders = new ArrayList<>();

    /**
     * List of GROUP BY fields
     */
    private List<String> groups = new ArrayList<>();

    /**
     * List of JOIN clauses
     */
    private List<String> joins = new ArrayList<>();

    /**
     * List of HAVING conditions
     */
    private List<String> havings = new ArrayList<>();

    /**
     * Query parameters for HAVING clause
     */
    private QueryParameters havingParameters;

    /**
     * The result type for the query (can differ from entity type for DTOs)
     */
    private Class resultType;

    /**
     * Fields to select in the query
     */
    private String[] fields;

    /**
     * Custom SELECT clause override
     */
    private String customSelect;

    /**
     * Custom FROM clause override
     */
    private String customFrom;

    /**
     * Flag indicating if the query has been built
     */
    private boolean builded;

    /**
     * Map of fields and values for UPDATE queries
     */
    private Map<String, Object> mapFields;

    /**
     * Flag to control whether to prepend variable name to fields
     */
    private boolean appendVarNameToFields = true;

    /**
     * Checks if variable names should be appended to field names.
     *
     * @return true if variable names are appended, false otherwise
     */
    public boolean isAppendVarNameToFields() {
        return appendVarNameToFields;
    }

    /**
     * Sets whether variable names should be appended to field names.
     *
     * @param appendVarNameToFields true to append variable names, false otherwise
     */
    public void setAppendVarNameToFields(boolean appendVarNameToFields) {
        this.appendVarNameToFields = appendVarNameToFields;
    }

    /**
     * Constant for space character
     */
    private static final String SPACE = " ";

    /**
     * Private constructor to enforce factory method usage.
     */
    private QueryBuilder() {
    }

    /**
     * Creates a new SELECT QueryBuilder for the specified entity type with an alias and optional fields.
     * If no fields are provided, selects the entire entity.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, "p", "name", "email");
     * // Produces: "select p.name, p.email from Person as p"
     * }</pre>
     *
     * @param type   the entity class to query
     * @param var    the alias/variable name for the entity
     * @param fields optional field names to select
     * @return a new QueryBuilder instance configured for SELECT
     */
    public static QueryBuilder select(Class<?> type, String var, String... fields) {
        return select(type, null, var, fields);
    }

    /**
     * Creates a new SELECT QueryBuilder with a specific result type for DTO projections.
     * The result type should have a constructor matching the selected fields.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, PersonDTO.class, "p", "name", "email");
     * // Produces: "select new com.example.PersonDTO(p.name, p.email) from Person as p"
     * }</pre>
     *
     * @param entityType the entity class to query
     * @param resultType the class for the query result (for DTO projections)
     * @param var        the alias/variable name for the entity
     * @param fields     field names to select
     * @return a new QueryBuilder instance configured for SELECT with result type
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
     * Creates a new SELECT QueryBuilder with specified fields but no entity type yet.
     * The entity type can be set later using {@link #from(Class, String)}.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select("name", "email")
     *     .from(Person.class, "p");
     * }</pre>
     *
     * @param fields field names to select (if empty, selects all)
     * @return a new QueryBuilder instance
     */
    public static QueryBuilder select(String... fields) {
        QueryBuilder qb = new QueryBuilder();
        qb.fields = fields;
        qb.queryType = QueryType.SELECT;
        return qb;
    }

    /**
     * Sets the entity type and alias for this query.
     * Clears any custom FROM clause previously set.
     *
     * @param entityType the entity class to query
     * @param var        the alias/variable name for the entity
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder from(Class<?> entityType, String var) {
        this.type = entityType;
        this.var = var;
        this.customFrom = null;
        return this;
    }

    /**
     * Sets the result type for this query, useful for creating DTO collections.
     * The result type should have a constructor matching the selected fields.
     *
     * @param resultType the class for query results
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder resultType(Class<?> resultType) {
        this.resultType = resultType;
        return this;
    }

    /**
     * Builds the query string by processing all clauses and parameters.
     * This method is idempotent - calling it multiple times has no additional effect.
     *
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder build() {
        if (!builded) {
            toString();
            builded = true;
        }
        return this;
    }

    /**
     * Creates a QueryBuilder from QueryParameters, automatically generating WHERE conditions
     * from the parameters.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryParameters params = QueryParameters.with("name", "John")
     *     .add("age", QueryConditions.greaterThan(18));
     * QueryBuilder qb = QueryBuilder.fromParameters(Person.class, "p", params);
     * // Produces: "select p from Person as p where p.name like :name and p.age > :age"
     * }</pre>
     *
     * @param type       the entity class to query
     * @param var        the alias/variable name for the entity
     * @param parameters the query parameters to convert to WHERE conditions
     * @return a new QueryBuilder instance with conditions from parameters
     */
    public static QueryBuilder fromParameters(Class<?> type, String var, QueryParameters parameters) {

        return QueryBuilder.select().from(type, var).where(parameters);
    }

    /**
     * Processes and applies all query parameters to the WHERE clause.
     * This method converts QueryParameters into appropriate WHERE conditions,
     * handles sorting, and processes nested query groups.
     * This method is called automatically during query building.
     */
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
     * Adds a query condition to the WHERE clause with the specified boolean operator.
     * The condition is rendered and added to the list of WHERE conditions.
     *
     * @param property  the property name to apply the condition to
     * @param qc        the query condition to apply
     * @param booleanOp the boolean operator (AND/OR) for combining this condition
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

    /**
     * Renders a query condition for the specified property.
     * Ensures field variable names are properly handled.
     *
     * @param property the property name
     * @param qc       the query condition to render
     * @return the rendered condition string
     */
    private String renderCondition(String property, QueryCondition qc) {
        String render = qc.render(property);
        return checkFieldVar(render);
    }

    /**
     * Adds a WHERE condition to the query.
     * Conditions are combined with AND by default.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, "p")
     *     .where("p.age > 18")
     *     .where("p.active = true");
     * // Produces: "select p from Person as p where p.age > 18 and p.active = true"
     * }</pre>
     *
     * @param conditions the condition string to add
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder where(String conditions) {
        if (!wheres.contains(conditions)) {
            wheres.add(conditions);
        }
        return this;
    }

    /**
     * Sets the WHERE conditions using QueryParameters.
     * The parameters are converted to appropriate WHERE conditions during query building.
     *
     * @param params the query parameters to use
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder where(QueryParameters params) {
        this.queryParameters = params;
        return this;
    }

    /**
     * Adds a WHERE condition using a field name and QueryCondition.
     * Automatically renders the condition and adds it to the query parameters.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, "p")
     *     .where("age", QueryConditions.greaterThan(18));
     * }</pre>
     *
     * @param field     the field name
     * @param condition the query condition to apply
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder where(String field, QueryCondition condition) {
        where(QueryParameters.with(field, condition));
        where(renderCondition(field, condition));
        return this;

    }

    /**
     * Adds a condition combined with AND operator.
     * This is a convenience method equivalent to {@link #and(String, boolean)} with append=true.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, "p")
     *     .where("p.age > 18")
     *     .and("p.city = 'NYC'");
     * // Produces: "select p from Person as p where p.age > 18 and p.city = 'NYC'"
     * }</pre>
     *
     * @param condition the condition to add with AND
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder and(String condition) {
        return and(condition, true);
    }

    /**
     * Adds a condition combined with AND operator, with control over whether to append.
     * If append is false, the condition is validated but not added to the query.
     *
     * @param condition the condition to add with AND
     * @param append    whether to actually append the condition
     * @return this QueryBuilder for method chaining
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

    /**
     * Adds a field condition with AND operator using QueryCondition.
     * Automatically renders the condition and adds it to query parameters.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, "p")
     *     .where("age", QueryConditions.greaterThan(18))
     *     .and("salary", QueryConditions.lessThan(50000));
     * }</pre>
     *
     * @param field     the field name
     * @param condition the query condition to apply
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder and(String field, QueryCondition condition) {
        addCondition(field, condition, BooleanOp.AND);
        getQueryParameters().add(field, condition);
        return this;
    }

    /**
     * Adds a condition combined with OR operator.
     * This is a convenience method equivalent to {@link #or(String, boolean)} with append=true.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, "p")
     *     .where("p.age > 18")
     *     .or("p.verified = true");
     * // Produces: "select p from Person as p where p.age > 18 or p.verified = true"
     * }</pre>
     *
     * @param condition the condition to add with OR
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder or(String condition) {
        return or(condition, true);
    }

    /**
     * Adds a condition combined with OR operator, with control over whether to append.
     * If append is false, the condition is validated but not added to the query.
     *
     * @param condition the condition to add with OR
     * @param append    whether to actually append the condition
     * @return this QueryBuilder for method chaining
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

    /**
     * Adds a field condition with OR operator using QueryCondition.
     * Automatically renders the condition and adds it to query parameters.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, "p")
     *     .where("age", QueryConditions.lessThan(18))
     *     .or("verified", QueryConditions.isTrue());
     * }</pre>
     *
     * @param field     the field name
     * @param condition the query condition to apply
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder or(String field, QueryCondition condition) {
        addCondition(field, condition, BooleanOp.OR);
        getQueryParameters().add(field, condition);
        return this;
    }

    /**
     * Adds ORDER BY clause to the query for sorting results.
     * Multiple fields can be specified for multi-level sorting.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, "p")
     *     .orderBy("name ASC", "age DESC");
     * // Produces: "select p from Person as p order by p.name ASC, p.age DESC"
     * }</pre>
     *
     * @param field         the primary field to order by
     * @param anotherFields additional fields for multi-level sorting
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder orderBy(String field, String... anotherFields) {
        addField(orders, field);
        addFields(orders, anotherFields);
        return this;
    }

    /**
     * Adds GROUP BY clause to the query for aggregating results.
     * Multiple fields can be specified for multi-level grouping.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, "p", "city", "count(p)")
     *     .groupBy("city");
     * // Produces: "select p.city, count(p) from Person as p group by p.city"
     * }</pre>
     *
     * @param field         the primary field to group by
     * @param anotherFields additional fields for multi-level grouping
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder groupBy(String field, String... anotherFields) {
        addField(groups, field);
        addFields(groups, anotherFields);
        return this;
    }

    /**
     * Adds HAVING clause conditions for filtering grouped results.
     * Multiple conditions can be specified.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, "p", "city", "count(p)")
     *     .groupBy("city")
     *     .having("count(p) > 5");
     * }</pre>
     *
     * @param field         the primary having condition
     * @param anotherFields additional having conditions
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder having(String field, String... anotherFields) {
        addField(havings, field);
        addFields(havings, anotherFields);
        return this;
    }

    /**
     * Adds a HAVING clause condition using a QueryCondition.
     * Creates or reuses HAVING parameters for binding values.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, "p", "city", "count(p) as total")
     *     .groupBy("city")
     *     .having("count(p)", QueryConditions.greaterThan(5));
     * }</pre>
     *
     * @param field     the field or aggregate function expression
     * @param condition the query condition to apply
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder having(String field, QueryCondition condition) {
        String rendered = renderCondition(field, condition);
        having(rendered);
        if (havingParameters == null) {
            havingParameters = new QueryParameters();
            getQueryParameters().setNestedParameters(havingParameters);
        }
        havingParameters.add(QueryConditionUtils.cleanProperty(field), condition);
        return this;
    }

    /**
     * Adds multiple fields to a target list.
     * Used internally for processing field arrays in orderBy, groupBy, etc.
     *
     * @param list   the target list to add fields to
     * @param fields the array of field names to add
     */
    private void addFields(List<String> list, String[] fields) {
        if (fields != null) {
            for (String field : fields) {
                addField(list, field);
            }
        }
    }

    /**
     * Constructs and returns the complete JPQL query string.
     * This method delegates to the appropriate build method based on query type
     * (SELECT, UPDATE, or DELETE). It processes all configured clauses and parameters.
     *
     * @return the complete JPQL query string
     */
    @Override
    public String toString() {

        return switch (queryType) {
            case UPDATE -> buildUpdate();
            case DELETE -> buildDelete();
            default -> buildSelect();
        };
    }

    /**
     * Builds a SELECT query string including all configured clauses.
     * Processes fields, FROM clause, JOINs, WHERE conditions, GROUP BY, HAVING, and ORDER BY.
     *
     * @return the complete SELECT query string
     */
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
            from = FROM_WORD + SPACE + type.getName() + SPACE + AS + SPACE + var;
        }

        String whereWord = getWhereWord();
        String parseGroups = groups.isEmpty() ? "" : " group by " + parse(groups, ", ");
        String parseHavings = havings.isEmpty() ? "" : " having " + parse(havings, ", ");
        String parseOrders = orders.isEmpty() ? "" : " order by " + parse(orders, ", ");
        String parseJoins = joins.isEmpty() ? "" : parse(joins, " ") + SPACE;

        return (select + SPACE + from + SPACE + parseJoins + whereWord + SPACE + parse(wheres, " ") + parseGroups + parseHavings + parseOrders).trim();
    }

    /**
     * Builds an UPDATE query string with SET clause and WHERE conditions.
     * Requires field values to be set using {@link #set(Map)}.
     *
     * @return the complete UPDATE query string
     * @throws ValidationError if no fields to update are provided
     */
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
            from = SPACE + type.getName() + SPACE + AS + SPACE + var;
        }

        String whereWord = getWhereWord();
        String parseJoins = joins.isEmpty() ? "" : parse(joins, " ") + SPACE;

        return update + from + SPACE + parseJoins + "set" + SPACE + fieldList + SPACE + whereWord + SPACE + parse(wheres, " ");
    }

    /**
     * Builds a DELETE query string with WHERE conditions.
     * Used for bulk deletion operations.
     *
     * @return the complete DELETE query string
     */
    private String buildDelete() {
        configureParameters();

        String delete = "delete";


        if (customFrom != null) {
            from = customFrom + SPACE;
        } else {
            from = SPACE + type.getName() + SPACE + AS + SPACE + var;
        }

        String whereWord = getWhereWord();
        String parseJoins = joins.isEmpty() ? "" : parse(joins, " ") + SPACE;

        return delete + SPACE + FROM_WORD + from + SPACE + parseJoins + whereWord + SPACE + parse(wheres, " ");
    }

    /**
     * Parses a list of strings into a single string with the specified delimiter.
     * Used internally to join clauses like WHERE, ORDER BY, etc.
     *
     * @param list      the list of strings to parse
     * @param delimiter the delimiter to join with
     * @return the joined string, or empty string if list is empty
     */
    private String parse(List<String> list, String delimiter) {
        String parsed = "";
        if (!list.isEmpty()) {
            parsed = String.join(delimiter, list);
        }
        return parsed;
    }

    /**
     * Adds a field to the target list, ensuring it's properly prefixed with the variable name.
     * Prevents duplicate entries in the list.
     *
     * @param list  the target list to add the field to
     * @param field the field name to add
     */
    private void addField(List<String> list, String field) {
        String toAdd = checkFieldVar(field);

        if (!list.contains(toAdd)) {
            list.add(toAdd);
        }
    }

    /**
     * Checks and adds the variable name prefix to a field if needed.
     * Fields starting with "(" or already containing the variable are left unchanged.
     *
     * @param field the field name to check
     * @return the field name with variable prefix if applicable
     */
    private String checkFieldVar(String field) {
        String toAdd = field;
        if (!field.startsWith("(") && !field.contains(var + ".") && appendVarNameToFields) {
            toAdd = var + "." + field;
        }
        return toAdd;
    }

    /**
     * Gets the WHERE keyword if there are WHERE conditions.
     *
     * @return "where" if conditions exist, empty string otherwise
     */
    private String getWhereWord() {
        String whereWord = "";
        if (wheres != null && !wheres.isEmpty()) {
            whereWord = "where";
        }
        return whereWord;
    }

    /**
     * Creates a projection query using a JPQL aggregate function (count, sum, avg, etc.).
     * This method builds a query string with the specified aggregate function applied to a field.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, "p")
     *     .where("p.age > 18");
     * String countQuery = qb.createProjection("count", "id");
     * // Produces: "select count(p.id) from Person as p where p.age > 18"
     * }</pre>
     *
     * @param function the aggregate function name (e.g., "count", "sum", "avg", "max", "min")
     * @param field    the field to apply the function to
     * @return the complete projection query string
     */
    public String createProjection(String function, String field) {
        build();
        String whereWord = getWhereWord();
        String varName = getVarName();

        String projection = "select " + function + "(" + varName + field + ") ";
        String parseJoins = joins.isEmpty() ? "" : parse(joins, " ") + SPACE;
        return projection + from + SPACE + parseJoins + whereWord + SPACE + parse(wheres, " ");
    }

    /**
     * Gets the variable name prefix for fields.
     * Returns the variable name followed by a dot if {@code appendVarNameToFields} is true,
     * otherwise returns an empty string.
     *
     * @return the variable name prefix for field references
     */
    public String getVarName() {
        return appendVarNameToFields ? var + "." : "";
    }

    /**
     * Sets a custom SELECT clause, overriding the default field selection.
     * This allows for complete control over the SELECT portion of the query.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select()
     *     .customSelect("distinct p.city, count(p)")
     *     .from(Person.class, "p")
     *     .groupBy("city");
     * }</pre>
     *
     * @param select the custom SELECT clause
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder customSelect(String select) {
        this.customSelect = select;
        return this;
    }

    /**
     * Sets a custom FROM clause, overriding the default entity FROM clause.
     * This is useful for complex queries involving subqueries or non-entity sources.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select("p.name")
     *     .customFrom("(select * from Person where active = true) as p");
     * }</pre>
     *
     * @param from the custom FROM clause
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder customFrom(String from) {
        this.customFrom = from;
        return this;
    }

    /**
     * Adds a JOIN clause to the query if it doesn't already exist.
     * This is an internal method used by the specific join methods.
     *
     * @param text the JOIN clause text to add
     */
    private void addJoin(String text) {
        if (text != null) {
            text = text.trim();
            if (!joins.contains(text)) {
                joins.add(text);
            }
        }
    }

    /**
     * Adds an INNER JOIN clause to the query.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, "p")
     *     .innerJoin("p.address a")
     *     .where("a.city = 'NYC'");
     * // Produces: "select p from Person as p inner join p.address a where a.city = 'NYC'"
     * }</pre>
     *
     * @param join the join expression (e.g., "p.address a")
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder innerJoin(String join) {
        addJoin("inner join " + join);
        return this;
    }

    /**
     * Adds a LEFT JOIN clause to the query.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, "p")
     *     .leftJoin("p.address a")
     *     .where("p.age > 18");
     * }</pre>
     *
     * @param join the join expression (e.g., "p.address a")
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder leftJoin(String join) {
        addJoin("left join " + join);
        return this;
    }

    /**
     * Adds a RIGHT JOIN clause to the query.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, "p")
     *     .rightJoin("p.department d");
     * }</pre>
     *
     * @param join the join expression (e.g., "p.department d")
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder rightJoin(String join) {
        addJoin("right join " + join);
        return this;
    }

    /**
     * Adds a generic JOIN clause to the query.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.select(Person.class, "p")
     *     .join("p.projects pr");
     * }</pre>
     *
     * @param join the join expression (e.g., "p.projects pr")
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder join(String join) {
        addJoin("join " + join);
        return this;
    }

    /**
     * Gets the entity type being queried.
     *
     * @return the entity class
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Gets the query parameters associated with this query.
     * If no parameters exist yet, creates a new QueryParameters instance.
     *
     * @return the query parameters object
     */
    public QueryParameters getQueryParameters() {
        if (queryParameters == null) {
            queryParameters = new QueryParameters();
            queryParameters.setType(getType());
        }
        return queryParameters;
    }

    /**
     * Gets the result type for this query.
     * The result type may differ from the entity type when using DTO projections.
     *
     * @return the result type class, or null if not set
     */
    public Class getResultType() {
        return resultType;
    }

    /**
     * Creates a deep copy of this QueryBuilder with all its current configuration.
     * The clone maintains all WHERE conditions, JOINs, ORDER BY clauses, and other settings.
     *
     * @return a new QueryBuilder instance with the same configuration
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

    /**
     * Gets the array of fields selected in this query.
     *
     * @return the array of field names, or null if not set
     */
    public String[] getFields() {
        return fields;
    }

    /**
     * Creates a new UPDATE QueryBuilder for bulk update operations.
     * Use {@link #set(Map)} to specify fields and values to update.
     *
     * <p>Example:</p>
     * <pre>{@code
     * Map<String, Object> updates = Map.of("status", "ACTIVE");
     * QueryBuilder qb = QueryBuilder.update(Person.class, "p")
     *     .set(updates)
     *     .where("p.age > 18");
     * // Produces: "update Person as p set p.status = :newValuestatus where p.age > 18"
     * }</pre>
     *
     * @param entityType the entity class to update
     * @param var        the alias/variable name for the entity
     * @return a new QueryBuilder instance configured for UPDATE
     */
    public static QueryBuilder update(Class entityType, String var) {
        var builder = new QueryBuilder();
        builder.queryType = QueryType.UPDATE;
        builder.type = entityType;
        builder.var = var;
        builder.customSelect = null;

        return builder;
    }

    /**
     * Sets the fields and values to update in an UPDATE query.
     * The map keys are field names and values are the new values to set.
     * String values are used directly, while other types are converted to query parameters.
     *
     * <p>Example:</p>
     * <pre>{@code
     * Map<String, Object> updates = new HashMap<>();
     * updates.put("status", "ACTIVE");
     * updates.put("lastUpdate", new Date());
     * qb.set(updates);
     * }</pre>
     *
     * @param fields a map of field names to their new values
     * @return this QueryBuilder for method chaining
     * @throws ValidationError if this is not an UPDATE query
     */
    public QueryBuilder set(Map<String, Object> fields) {
        if (queryType != QueryType.UPDATE) {
            throw new ValidationError("Query builder type should be " + QueryType.UPDATE);
        }

        this.mapFields = fields;
        return this;
    }

    /**
     * Creates a new DELETE QueryBuilder for bulk delete operations.
     *
     * <p>Example:</p>
     * <pre>{@code
     * QueryBuilder qb = QueryBuilder.delete(Person.class, "p")
     *     .where("p.age < 18");
     * // Produces: "delete from Person as p where p.age < 18"
     * }</pre>
     *
     * @param entityType the entity class to delete from
     * @param var        the alias/variable name for the entity
     * @return a new QueryBuilder instance configured for DELETE
     */
    public static QueryBuilder delete(Class entityType, String var) {
        var builder = new QueryBuilder();
        builder.queryType = QueryType.DELETE;
        builder.type = entityType;
        builder.var = var;
        builder.customSelect = null;

        return builder;
    }

    /**
     * Sets whether to automatically append variable names to field references.
     * When true, fields like "name" become "p.name" (where p is the variable).
     * This is a convenience method equivalent to {@link #setAppendVarNameToFields(boolean)}.
     *
     * @param append true to append variable names, false otherwise
     * @return this QueryBuilder for method chaining
     */
    public QueryBuilder appendVarNames(boolean append) {
        setAppendVarNameToFields(append);
        return this;
    }

}
