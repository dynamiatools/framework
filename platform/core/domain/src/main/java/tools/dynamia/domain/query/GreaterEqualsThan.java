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
 * The Class GreaterEqualsThan.
 *
 * @author Mario A. Serrano Leones
 */
public class GreaterEqualsThan extends AbstractQueryCondition<Object> {

    /**
     * Instantiates a new greater equals than.
     */
    public GreaterEqualsThan() {

    }

    /**
     * Instantiates a new greater equals than.
     *
     * @param value     the value
     * @param booleanOp the boolean op
     */
    public GreaterEqualsThan(Number value, BooleanOp booleanOp) {
        super(value, booleanOp);
    }

    /**
     * Instantiates a new greater equals than.
     *
     * @param value the value
     */
    public GreaterEqualsThan(Number value) {
        super(value);
    }

    /**
     * Instantiates a new greater equals than.
     *
     * @param value     the value
     * @param booleanOp the boolean op
     */
    public GreaterEqualsThan(Date value, BooleanOp booleanOp) {
        super(value, booleanOp);
    }

    /**
     * Instantiates a new greater equals than.
     *
     * @param value the value
     */
    public GreaterEqualsThan(Date value) {
        super(value);
    }

    /**
     * Instantiates a new greater equals than.
     *
     * @param value     the value
     * @param booleanOp the boolean op
     */
    public GreaterEqualsThan(LocalDate value, BooleanOp booleanOp) {
        super(value, booleanOp);
    }

    /**
     * Instantiates a new greater equals than.
     *
     * @param value the value
     */
    public GreaterEqualsThan(LocalDate value) {
        super(value);
    }

    /**
     * Instantiates a new greater equals than.
     *
     * @param value     the value
     * @param booleanOp the boolean op
     */
    public GreaterEqualsThan(LocalDateTime value, BooleanOp booleanOp) {
        super(value, booleanOp);
    }

    /**
     * Instantiates a new greater equals than.
     *
     * @param value the value
     */
    public GreaterEqualsThan(LocalDateTime value) {
        super(value);
    }

    /* (non-Javadoc)
     * @see AbstractQueryCondition#getOperator()
     */
    @Override
    protected String getOperator() {
        return ">=";
    }

    public boolean match(Object otherValue) {
        if (getValue() instanceof Number num && otherValue instanceof Number other) {
            return num.doubleValue() >= other.doubleValue();
        } else if (getValue() instanceof Date date && otherValue instanceof Date other) {
            return DateTimeUtils.isAfterOrEquals(date, other);
        } else if (getValue() instanceof LocalDate date && otherValue instanceof LocalDate other) {
            return DateTimeUtils.isAfterOrEquals(date, other);
        } else if (getValue() instanceof LocalDateTime date && otherValue instanceof LocalDateTime other) {
            return DateTimeUtils.isAfterOrEquals(date, other);
        } else if (getValue() instanceof Instant date && otherValue instanceof Instant other) {
            return DateTimeUtils.isAfterOrEquals(date, other);
        }
        return false;
    }

}
