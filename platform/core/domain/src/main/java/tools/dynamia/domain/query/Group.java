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
 * The Class Group.
 *
 * @param params    The params.
 * @param booleanOp The boolean op.
 * @author Ing. Mario Serrano
 */
public record Group(QueryParameters params, BooleanOp booleanOp) {

    /**
     * Instantiates a new group.
     *
     * @param params    the params
     * @param booleanOp the boolean op
     */
    public Group {
    }

    /**
     * Gets the boolean op.
     *
     * @return the boolean op
     */
    @Override
    public BooleanOp booleanOp() {
        return booleanOp;
    }

    /**
     * Gets the params.
     *
     * @return the params
     */
    @Override
    public QueryParameters params() {
        return params;
    }


}
