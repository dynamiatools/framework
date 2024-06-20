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
 * The Class Between.
 *
 * @author Mario A. Serrano Leones
 */
public class Between implements QueryCondition {

    /**
     * The value lo.
     */
    private Object valueLo;

    /**
     * The value hi.
     */
    private Object valueHi;

    /**
     * The boolean op.
     */
    private BooleanOp booleanOp = BooleanOp.AND;

    /**
     * Instantiates a new between.
     */
    public Between() {

    }

    /**
     * Instantiates a new between.
     *
     * @param valueLo the value lo
     * @param valueHi the value hi
     */
    public Between(Object valueLo, Object valueHi) {
        this.valueLo = valueLo;
        this.valueHi = valueHi;
    }

    /**
     * Instantiates a new between.
     *
     * @param valueLo   the value lo
     * @param valueHi   the value hi
     * @param booleanOp the boolean op
     */
    public Between(Object valueLo, Object valueHi, BooleanOp booleanOp) {
        super();
        this.valueLo = valueLo;
        this.valueHi = valueHi;
        this.booleanOp = booleanOp;
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.QueryCondition#render(java.lang.String)
     */
    @Override
    public String render(String property) {
        return property + " between :" + format(property, 1) + " and :" + format(property, 2);
    }

    /**
     * Format.
     *
     * @param property the property
     * @param i        the i
     * @return the string
     */
    private String format(String property, int i) {
        return property.replace(".", "").replace(" ", "").replace("=", "").replace("(", "").replace(")", "").replace(",", "").replace("'", "").replace("\"", "") + i;
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.QueryCondition#apply(java.lang.String, javax.persistence.Query)
     */
    @Override
    public void apply(String property, AbstractQuery query) {
        if (query != null) {
            query.setParameter(format(property, 1), valueLo);
            query.setParameter(format(property, 2), valueHi);
        }
    }

    /**
     * Sets the boolean op.
     *
     * @param booleanOp the new boolean op
     */
    public void setBooleanOp(BooleanOp booleanOp) {
        this.booleanOp = booleanOp;
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.QueryCondition#getBooleanOperator()
     */
    @Override
    public BooleanOp getBooleanOperator() {
        return booleanOp;
    }

    /**
     * Gets the value lo.
     *
     * @return the value lo
     */
    public Object getValueLo() {
        return valueLo;
    }

    /**
     * Sets the value lo.
     *
     * @param valueLo the new value lo
     */
    public void setValueLo(Object valueLo) {
        this.valueLo = valueLo;
    }

    /**
     * Gets the value hi.
     *
     * @return the value hi
     */
    public Object getValueHi() {
        return valueHi;
    }

    /**
     * Sets the value hi.
     *
     * @param valueHi the new value hi
     */
    public void setValueHi(Object valueHi) {
        this.valueHi = valueHi;
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.QueryCondition#getValue()
     */
    @Override
    public Object getValue() {
        if (getValueHi() != null && getValueLo() != null) {
            return new Object[]{getValueHi(), getValueLo()};
        } else {
            return null;
        }
    }

    @Override
    public boolean match(Object otherValue) {
        if (getValueLo() instanceof Number lo && getValueHi() instanceof Number hi && otherValue instanceof Number other) {
            return other.doubleValue() >= lo.doubleValue() && other.doubleValue() <= hi.doubleValue();
        } else if (getValueLo() instanceof Date lo && getValueHi() instanceof Date hi && otherValue instanceof Date other) {
            return DateTimeUtils.isBetween(other, lo, hi);
        } else if (getValueLo() instanceof LocalDate lo && getValueHi() instanceof LocalDate hi && otherValue instanceof LocalDate other) {
            return DateTimeUtils.isBetween(other, lo, hi);
        } else if (getValueLo() instanceof LocalDateTime lo && getValueHi() instanceof LocalDateTime hi && otherValue instanceof LocalDateTime other) {
            return DateTimeUtils.isBetween(other, lo, hi);
        } else if (getValueLo() instanceof Instant lo && getValueHi() instanceof Instant hi && otherValue instanceof Instant other) {
            return DateTimeUtils.isBetween(other, lo, hi);
        }
        return false;
    }
}
