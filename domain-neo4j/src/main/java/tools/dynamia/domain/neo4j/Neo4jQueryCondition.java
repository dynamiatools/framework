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

package tools.dynamia.domain.neo4j;

import org.neo4j.ogm.cypher.ComparisonOperator;
import tools.dynamia.domain.query.*;

public interface Neo4jQueryCondition<T> extends QueryCondition<T> {

	ComparisonOperator getOperator();

	static ComparisonOperator getOperator(QueryCondition c) {
		if (c instanceof Neo4jQueryCondition) {
			return ((Neo4jQueryCondition) c).getOperator();
		} else {
			if (c instanceof Equals) {
				return ComparisonOperator.EQUALS;
			} else if (c instanceof GreaterThan) {
				return ComparisonOperator.GREATER_THAN;
			} else if (c instanceof GreaterEqualsThan) {
				return ComparisonOperator.GREATER_THAN_EQUAL;
			} else if (c instanceof LessThan) {
				return ComparisonOperator.LESS_THAN;
			} else if (c instanceof LessEqualsThan) {
				return ComparisonOperator.LESS_THAN_EQUAL;
			} else if (c instanceof LikeEquals) {
				return ComparisonOperator.LIKE;
			} else if (c instanceof Inlist) {
				return ComparisonOperator.IN;
			} else if (c instanceof IsNull) {
				return ComparisonOperator.IS_NULL;
			}

			return ComparisonOperator.MATCHES;
		}
	}

	static QueryCondition build(ComparisonOperator op) {
		return new Neo4jQueryCondition() {

			@Override
			public String render(String property) {
				return null;
			}

			@Override
			public void apply(String property, AbstractQuery query) {

			}

			@Override
			public BooleanOp getBooleanOperator() {
				return BooleanOp.AND;
			}

			@Override
			public Object getValue() {
				return null;
			}

			@Override
			public ComparisonOperator getOperator() {
				return op;
			}
		};
	}
}
