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


/**
 * The Class Group.
 *
 * @author Ing. Mario Serrano
 */
public class Group {

    /**
     * The params.
     */
    private QueryParameters params;

    /**
     * The boolean op.
     */
    private BooleanOp booleanOp;

    /**
     * Instantiates a new group.
     *
     * @param params the params
     * @param booleanOp the boolean op
     */
    public Group(QueryParameters params, BooleanOp booleanOp) {
        this.params = params;
        this.booleanOp = booleanOp;
    }

    /**
     * Gets the boolean op.
     *
     * @return the boolean op
     */
    public BooleanOp getBooleanOp() {
        return booleanOp;
    }

    /**
     * Gets the params.
     *
     * @return the params
     */
    public QueryParameters getParams() {
        return params;
    }
}
