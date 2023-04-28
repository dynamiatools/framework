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

import static tools.dynamia.domain.query.QueryConditionUtils.cleanProperty;


/**
 * The Class AbstractQueryCondition.
 *
 * @author Mario A. Serrano Leones
 * @param <T> the generic type
 */
public abstract class AbstractQueryCondition<T> implements QueryCondition<T> {

    /**
     * The value.
     */
    private T value;

    /**
     * The boolean op.
     */
    private BooleanOp booleanOp;

    /**
     * Instantiates a new abstract query condition.
     */
    public AbstractQueryCondition() {
        this(null);
    }

    /**
     * Instantiates a new abstract query condition.
     *
     * @param value the value
     */
    public AbstractQueryCondition(T value) {
        this(value, BooleanOp.AND);
    }

    /**
     * Instantiates a new abstract query condition.
     *
     * @param value the value
     * @param booleanOp the boolean op
     */
    public AbstractQueryCondition(T value, BooleanOp booleanOp) {
        this.value = value;
        this.booleanOp = booleanOp;
    }

    /* (non-Javadoc)
     * @see QueryCondition#render(java.lang.String)
     */
    @Override
    public String render(String property) {
        String operator = getOperator();
        return new StringBuilder(property).append(" ").append(operator).append(" :").append(cleanProperty(property)).toString();

    }

    /* (non-Javadoc)
     * @see QueryCondition#apply(java.lang.String, javax.persistence.Query)
     */
    @Override
    public void apply(String property, AbstractQuery query) {
        query.setParameter(cleanProperty(property), value);
    }

    /* (non-Javadoc)
     * @see QueryCondition#getBooleanOperator()
     */
    @Override
    public BooleanOp getBooleanOperator() {
        return booleanOp;
    }

    /**
     * Sets the boolean operator.
     *
     * @param booleanOp the new boolean operator
     */
    public void setBooleanOperator(BooleanOp booleanOp) {
        this.booleanOp = booleanOp;
    }

    /**
     * Gets the operator.
     *
     * @return the operator
     */
    protected abstract String getOperator();

    /* (non-Javadoc)
     * @see QueryCondition#getValue()
     */
    @Override
    public T getValue() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(T value) {
        this.value = value;
    }
}
