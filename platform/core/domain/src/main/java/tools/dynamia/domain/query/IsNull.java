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
 * Render an is null condition
 *
 * @author Mario A. Serrano Leones
 */
public class IsNull implements QueryCondition {

    /**
     * The op.
     */
    private BooleanOp op = BooleanOp.AND;

    /**
     * Instantiates a new checks if is null.
     */
    public IsNull() {
    }

    /**
     * Instantiates a new checks if is null.
     *
     * @param op the op
     */
    public IsNull(BooleanOp op) {
        this.op = op;
    }

    /*
     * (non-Javadoc)
     *
     * @see QueryCondition#render(java.lang.String)
     */
    @Override
    public String render(String property) {
        return property + " is null";
    }

    /*
     * (non-Javadoc)
     *
     * @see QueryCondition#apply(java.lang.String,
     * javax.persistence.Query)
     */
    @Override
    public void apply(String property, AbstractQuery query) {
    }

    /*
     * (non-Javadoc)
     *
     * @see QueryCondition#getBooleanOperator()
     */
    @Override
    public BooleanOp getBooleanOperator() {
        return op;
    }

    /*
     * (non-Javadoc)
     *
     * @see QueryCondition#getValue()
     */
    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public boolean match(Object otherValue) {
        return otherValue == null;
    }
}
