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
import java.util.Collection;


/**
 * The Class QueryConditionGroup.
 */
public class QueryConditionGroup extends AbstractQueryCondition<Collection<QueryCondition>> {

    /**
     * Instantiates a new query condition group.
     */
    public QueryConditionGroup() {
        
    }

    /**
     * Instantiates a new query condition group.
     *
     * @param conditions the conditions
     */
    public QueryConditionGroup(QueryCondition... conditions) {
        this(Arrays.asList(conditions));
    }

    /**
     * Instantiates a new query condition group.
     *
     * @param booleanOp the boolean op
     * @param conditions the conditions
     */
    public QueryConditionGroup(BooleanOp booleanOp, QueryCondition... conditions) {
        this(Arrays.asList(conditions), booleanOp);
    }

    /**
     * Instantiates a new query condition group.
     *
     * @param value the value
     * @param booleanOp the boolean op
     */
    public QueryConditionGroup(Collection<QueryCondition> value, BooleanOp booleanOp) {
        super(value, booleanOp);
        
    }

    /**
     * Instantiates a new query condition group.
     *
     * @param value the value
     */
    public QueryConditionGroup(Collection<QueryCondition> value) {
        super(value);
        
    }

    /* (non-Javadoc)
	 * @see AbstractQueryCondition#render(java.lang.String)
     */
    @Override
    public String render(String property) {
        return null;
    }

    /* (non-Javadoc)
	 * @see AbstractQueryCondition#apply(java.lang.String, javax.persistence.Query)
     */
    @Override
    public void apply(String property, AbstractQuery query) {
        if (getValue() != null && !getValue().isEmpty()) {
            for (QueryCondition qc : getValue()) {
                qc.apply(property, query);
            }
        }
    }

    /* (non-Javadoc)
	 * @see AbstractQueryCondition#getOperator()
     */
    @Override
    protected String getOperator() {
        
        return null;
    }

}
