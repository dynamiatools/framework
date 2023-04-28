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

import java.util.Date;


/**
 * The Class LessThan.
 *
 * @author Mario A. Serrano Leones
 */
public class LessThan extends AbstractQueryCondition<Object> {

    /**
     * Instantiates a new less than.
     */
    public LessThan() {

    }

    /**
     * Instantiates a new less than.
     *
     * @param value the value
     * @param booleanOp the boolean op
     */
    public LessThan(Number value, BooleanOp booleanOp) {
        super(value, booleanOp);
    }

    /**
     * Instantiates a new less than.
     *
     * @param value the value
     */
    public LessThan(Number value) {
        super(value);
    }

    /**
     * Instantiates a new less than.
     *
     * @param value the value
     * @param booleanOp the boolean op
     */
    public LessThan(Date value, BooleanOp booleanOp) {
        super(value, booleanOp);
    }

    /**
     * Instantiates a new less than.
     *
     * @param value the value
     */
    public LessThan(Date value) {
        super(value);
    }

    /* (non-Javadoc)
	 * @see com.dynamia.tools.domain.query.AbstractQueryCondition#getOperator()
     */
    @Override
    protected String getOperator() {
        return "<";
    }
}
