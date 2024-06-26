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


/**
 * The Class NotEquals.
 *
 * @author Mario A. Serrano Leones
 */
public class NotEquals extends AbstractQueryCondition<Object> {

    /**
     * Instantiates a new not equals.
     *
     * @param value     the value
     * @param booleanOp the boolean op
     */
    public NotEquals(Object value, BooleanOp booleanOp) {
        super(value, booleanOp);
    }

    /**
     * Instantiates a new not equals.
     *
     * @param value the value
     */
    public NotEquals(Object value) {
        super(value);
    }

    /* (non-Javadoc)
     * @see AbstractQueryCondition#getOperator()
     */
    @Override
    protected String getOperator() {
        return "<>";
    }

    @Override
    public boolean match(Object otherValue) {
        return getValue() != null && !getValue().equals(otherValue);
    }
}
