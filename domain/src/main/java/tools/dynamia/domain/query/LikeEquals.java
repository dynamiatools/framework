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

import tools.dynamia.domain.util.DomainUtils;


/**
 * The Class LikeEquals.
 *
 * @author Mario A. Serrano Leones
 */
public class LikeEquals extends AbstractQueryCondition<Object> {

    /**
     * The auto searchable string.
     */
    private final boolean autoSearchableString;

    /**
     * Instantiates a new like equals.
     */
    public LikeEquals() {
        this("", true);
    }

    /**
     * Instantiates a new like equals.
     *
     * @param value the value
     */
    public LikeEquals(Object value) {
        this(value, true);
    }

    /**
     * Instantiates a new like equals.
     *
     * @param value the value
     * @param autoSearchableString the auto searchable string
     */
    public LikeEquals(Object value, boolean autoSearchableString) {
        this(value, autoSearchableString, BooleanOp.AND);
    }

    /**
     * Instantiates a new like equals.
     *
     * @param value the value
     * @param autoSearchableString the auto searchable string
     * @param booleanOp the boolean op
     */
    public LikeEquals(Object value, boolean autoSearchableString, BooleanOp booleanOp) {
        super(value, booleanOp);
        this.autoSearchableString = autoSearchableString;
    }

    /* (non-Javadoc)
     * @see AbstractQueryCondition#render(java.lang.String)
     */
    @Override
    public String render(String property) {
        String operator = getValue() instanceof String && autoSearchableString? " like " : " = ";
        return property + operator + ":" + format(property);

    }

    /* (non-Javadoc)
     * @see AbstractQueryCondition#apply(java.lang.String, javax.persistence.Query)
     */
    @Override
    public void apply(String property, AbstractQuery query) {
    	String paramName = format(property);
        if (query != null) {
            if (getValue() instanceof String) {
                query.setParameter(paramName, autoSearchableString ? DomainUtils.buildSearcheableString(getValue().toString()) : getValue());
            } else {
                query.setParameter(paramName, getValue());
            }
        }
    }

    /**
     * Format.
     *
     * @param property the property
     * @return the string
     */
    private String format(String property) {
        return property.replace(".", "").replace(" ", "").replace("=", "").replace("(", "").replace(")", "").replace(",", "").replace("'", "").replace("\"", "");
    }

    /* (non-Javadoc)
     * @see AbstractQueryCondition#getOperator()
     */
    @Override
    protected String getOperator() {
        return "=";
    }
}
