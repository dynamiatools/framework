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
 * The Class StartsWith.
 */
public class StartsWith extends AbstractQueryCondition<String> {

    /**
     * Instantiates a new starts with.
     */
    public StartsWith() {

    }

    /**
     * Instantiates a new starts with.
     *
     * @param value the value
     */
    public StartsWith(String value) {
        super(value + "%");
    }

    /**
     * Instantiates a new starts with.
     *
     * @param value the value
     * @param op the op
     */
    public StartsWith(String value, BooleanOp op) {
        super(value + "%", op);
    }

    /* (non-Javadoc)
     * @see AbstractQueryCondition#setValue(java.lang.Object)
     */
    @Override
    public void setValue(String value) {
        if (value == null) {
            value = "";
        }
        super.setValue(value + "%");
    }

    /* (non-Javadoc)
     * @see AbstractQueryCondition#getOperator()
     */
    @Override
    protected String getOperator() {
        return "like";
    }
}
