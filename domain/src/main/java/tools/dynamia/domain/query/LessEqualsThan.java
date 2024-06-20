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

import tools.dynamia.commons.DateTimeUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;


/**
 * The Class LessEqualsThan.
 *
 * @author Mario A. Serrano Leones
 */
public class LessEqualsThan extends AbstractQueryCondition<Object> {

    /**
     * Instantiates a new less equals than.
     */
    public LessEqualsThan() {

    }

    /**
     * Instantiates a new less equals than.
     *
     * @param value     the value
     * @param booleanOp the boolean op
     */
    public LessEqualsThan(Number value, BooleanOp booleanOp) {
        super(value, booleanOp);
    }

    /**
     * Instantiates a new less equals than.
     *
     * @param value the value
     */
    public LessEqualsThan(Number value) {
        super(value);
    }

    /**
     * Instantiates a new less equals than.
     *
     * @param value     the value
     * @param booleanOp the boolean op
     */
    public LessEqualsThan(Date value, BooleanOp booleanOp) {
        super(value, booleanOp);
    }

    /**
     * Instantiates a new less equals than.
     *
     * @param value the value
     */
    public LessEqualsThan(Date value) {
        super(value);
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.AbstractQueryCondition#getOperator()
     */
    @Override
    protected String getOperator() {
        return "<=";
    }

    public boolean match(Object otherValue) {
        if (getValue() instanceof Number num && otherValue instanceof Number other) {
            return num.doubleValue() <= other.doubleValue();
        }else if (getValue() instanceof Date date && otherValue instanceof Date other) {
            return DateTimeUtils.isBeforeOrEquals(date, other);
        } else if (getValue() instanceof LocalDate date && otherValue instanceof LocalDate other) {
            return DateTimeUtils.isBeforeOrEquals(date, other);
        } else if (getValue() instanceof LocalDateTime date && otherValue instanceof LocalDateTime other) {
            return DateTimeUtils.isBeforeOrEquals(date, other);
        } else if (getValue() instanceof Instant date && otherValue instanceof Instant other) {
            return DateTimeUtils.isBeforeOrEquals(date, other);
        }
        return false;
    }
}
