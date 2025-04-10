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

import tools.dynamia.commons.BeanSorter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class QueryParameters.
 *
 * @author Mario Serrano Leones
 */
@SuppressWarnings({"rawtypes"})
public class QueryParameters extends HashMap<String, Object> implements Serializable {


    private static final long serialVersionUID = 1319818461222466674L;
    public static final String HINT_TEXT_SEARCH = "TextSeach";
    public static final Null NULL = new Null();

    private BeanSorter<?> sorter;
    private DataPaginator paginator;
    private boolean autocreateSearcheableStrings = true;
    private List<Group> groups = new ArrayList<>();
    private Class<?> type;

    private final Map<String, Object> hints = new HashMap<>();
    private int depth = 0;
    private int maxResults;
    private final List<String> sortedKeys = new ArrayList<>();

    public QueryParameters() {
    }

    /**
     * Instantiates a new query parameters.
     *
     * @param sorter    the sorter
     * @param paginator the paginator
     */
    public QueryParameters(BeanSorter<?> sorter, DataPaginator paginator) {
        this.sorter = sorter;
        this.paginator = paginator;
    }

    /**
     * Instantiates a new query parameters.
     *
     * @param paginator the paginator
     */
    public QueryParameters(DataPaginator paginator) {
        this.paginator = paginator;
    }

    /**
     * Instantiates a new query parameters.
     *
     * @param m the m
     */
    public QueryParameters(Map<? extends String, ?> m) {
        if (m != null) {
            for (String key : m.keySet()) {
                add(key, m.get(key));
            }
        }
    }

    /**
     * Adds the group.
     *
     * @param params    the params
     * @param booleanOp the boolean op
     * @return the query parameters
     */
    public QueryParameters addGroup(QueryParameters params, BooleanOp booleanOp) {
        if (params != null && this != params) {
            groups.add(new Group(params, booleanOp));
        }
        return this;
    }

    /**
     * Gets the groups.
     *
     * @return the groups
     */
    public List<Group> getGroups() {
        return groups;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.HashMap#clear()
     */
    @Override
    public void clear() {
        super.clear();
        groups = new ArrayList<>();

    }

    /**
     * Put groups.
     *
     * @param parameters the parameters
     */
    public void putGroups(QueryParameters parameters) {
        groups.addAll(parameters.groups);
    }

    /**
     * With.
     *
     * @param name  the name
     * @param value the value
     * @return the query parameters
     */
    public static QueryParameters with(String name, Object value) {
        QueryParameters qp = new QueryParameters();
        qp.add(name, value);
        return qp;
    }

    /**
     * With.
     *
     * @param name           the name
     * @param queryCondition the query condition
     * @return public void addDefaultParameter(String name, Object value){
     * defaultParameters.add(name, value); }
     */
    public static QueryParameters with(String name, QueryCondition queryCondition) {
        QueryParameters qp = new QueryParameters();
        qp.add(name, queryCondition);
        return qp;
    }

    /**
     * Adds the.
     *
     * @param name  the name
     * @param value the value
     * @return the query parameters
     */
    public QueryParameters add(String name, Object value) {
        put(name, value);
        return this;
    }

    /**
     * Adds the.
     *
     * @param name      the name
     * @param condition the condition
     * @return the query parameters
     */
    public QueryParameters add(String name, QueryCondition condition) {
        put(name, condition);
        return this;
    }

    /**
     * Paginate.
     *
     * @param paginator the paginator
     * @return the query parameters
     */
    public QueryParameters paginate(DataPaginator paginator) {
        this.paginator = paginator;
        return this;
    }

    /**
     * Paginate.
     *
     * @param pageSize the page size
     * @return the query parameters
     */
    public QueryParameters paginate(int pageSize) {
        return paginate(new DataPaginator(pageSize));
    }

    /**
     * Sort.
     *
     * @param sorter the sorter
     * @return the query parameters
     */
    public QueryParameters sort(BeanSorter<?> sorter) {
        this.sorter = sorter;
        return this;
    }

    /**
     * Order by.
     *
     * @param column the column
     * @param asc    the asc
     * @return the query parameters
     */
    public QueryParameters orderBy(String column, boolean asc) {
        if (sorter == null) {
            sorter = new BeanSorter();
        }
        sorter.setColumnName(column);
        sorter.setAscending(asc);
        return this;
    }

    /**
     * Order by a column name ascending order
     */
    public QueryParameters orderBy(String column) {
        return orderBy(column, true);
    }


    /**
     * Gets the paginator.
     *
     * @return the paginator
     */
    public DataPaginator getPaginator() {
        return paginator;
    }

    /**
     * Gets the sorter.
     *
     * @return the sorter
     */
    public BeanSorter<?> getSorter() {
        return sorter;
    }

    public void setSorter(BeanSorter<?> sorter) {
        this.sorter = sorter;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.AbstractMap#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName()).append(": Values\n");
        for (Map.Entry<String, Object> entry : entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Contains entity id.
     *
     * @param key the key
     * @return true, if successful
     */
    public boolean containsEntityId(String key) {
        if (containsKey(key)) {
            Object id = get(key);
            if (id != null) {
                String idString = id.toString();
                if (!idString.equals("0")) {
                    return true;
                }
            }
        }
        remove(key);
        return false;
    }

    /**
     * Contains string.
     *
     * @param key the key
     * @return true, if successful
     */
    public boolean containsString(String key) {
        if (containsKey(key)) {
            String string = get(key).toString();
            if (!string.isEmpty()) {
                return true;
            }
        }
        remove(key);
        return false;
    }

    /**
     * Checks if is autocreate searcheable strings.
     *
     * @return true, if is autocreate searcheable strings
     */
    public boolean isAutocreateSearcheableStrings() {
        return autocreateSearcheableStrings;
    }

    /**
     * Sets the autocreate searcheable strings.
     *
     * @param autocreateSearcheableStrings the new autocreate searcheable strings
     */
    public QueryParameters setAutocreateSearcheableStrings(boolean autocreateSearcheableStrings) {
        this.autocreateSearcheableStrings = autocreateSearcheableStrings;
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
     * Sets the type.
     *
     * @param type the new type
     */
    public QueryParameters setType(Class<?> type) {
        this.type = type;
        return this;
    }

    public QueryParameters setHint(String name, Object value) {
        hints.put(name, value);
        return this;
    }

    public Object getHint(String name) {
        return hints.get(name);
    }

    public Map<String, Object> getHints() {
        return hints;
    }

    public int getDepth() {
        return depth;
    }

    public QueryParameters setDepth(int depth) {
        this.depth = depth;
        return this;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public QueryParameters setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * Apply to.
     *
     * @param query the query
     */
    public void applyTo(AbstractQuery query) {
        for (Map.Entry<String, Object> entry : entrySet()) {
            if (!entry.getKey().startsWith("#")) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value != null) {
                    if (value instanceof QueryCondition qc) {
                        qc.apply(key, query);
                    } else if (entry.getKey().equals("id") || entry.getKey().endsWith(".id")) {
                        if (!(value instanceof Long)) {
                            try {
                                value = Long.valueOf(value.toString());
                            } catch (NumberFormatException e) {
                                // Is not a number
                            }
                        }
                        query.setParameter(key.replace(".", ""), value);
                    } else if (value instanceof Null) {
                        query.setParameter(key, null);
                    } else {
                        new LikeEquals(value, autocreateSearcheableStrings).apply(key, query);
                    }
                } else {
                    query.setParameter(key, null);
                }
            }
        }
        for (Group group : groups) {
            group.params().applyTo(query);
        }

        if (!hints.isEmpty()) {
            hints.forEach(query::setHint);
        }
    }

    @Override
    public Object put(String key, Object value) {
        if (!sortedKeys.contains(key)) {
            sortedKeys.add(key);
        }
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        m.keySet().forEach(k -> {
            if (!sortedKeys.contains(k)) {
                sortedKeys.add(k);
            }
        });
        super.putAll(m);
    }

    /**
     * Return the list of keys sorted the order of add or put method invocation
     */
    public List<String> getSortedKeys() {
        return Collections.unmodifiableList(sortedKeys);
    }

    /**
     * Return true if query sorter is setted
     *
     * @return boolean
     */
    public boolean isSorted() {
        return sorter != null && sorter.getColumnName() != null && !sorter.getColumnName().isEmpty();
    }

    public QueryParameters clone() {
        QueryParameters clone = new QueryParameters();
        clone.putAll(this);
        clone.sorter = sorter;
        clone.paginator = paginator;
        clone.autocreateSearcheableStrings = autocreateSearcheableStrings;
        clone.groups = groups;
        clone.type = type;
        clone.hints.putAll(hints);
        clone.depth = depth;
        clone.maxResults = maxResults;
        clone.sortedKeys.addAll(sortedKeys);
        return clone;
    }

}
