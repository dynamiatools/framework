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
package tools.dynamia.crud;

import tools.dynamia.commons.ApplicableClass;
import tools.dynamia.commons.Messages;
import tools.dynamia.domain.AbstractEntity;
import tools.dynamia.domain.query.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public enum FilterCondition {

    EQUALS(msg("equals"), Equals.class, ApplicableClass.ALL),
    INLIST(msg("inlist"), Inlist.class, ApplicableClass.get(AbstractEntity.class, Enum.class)),
    CONTAINS(msg("contains"), LikeEquals.class, ApplicableClass.get(String.class)),
    STARTS(msg("startswith"), StartsWith.class, ApplicableClass.get(String.class)),
    ENDS(msg("endswith"), EndsWith.class, ApplicableClass.get(String.class)),
    BETWEEN(msg("between"), Between.class, ApplicableClass.get(Date.class, Number.class)),
    LESS(msg("less"), LessThan.class, ApplicableClass.get(Date.class, Number.class)),
    LESS_EQ(msg("lesseq"), LessEqualsThan.class, ApplicableClass.get(Date.class, Number.class)),
    GREATER(msg("greater"), GreaterThan.class, ApplicableClass.get(Date.class, Number.class)),
    GREATER_EQ(msg("greatereq"), GreaterEqualsThan.class, ApplicableClass.get(Date.class, Number.class));

    private final String label;
    private final Class<? extends QueryCondition> conditionClass;
    private final ApplicableClass[] applicableClasses;

    FilterCondition(String label, Class<? extends QueryCondition> conditionClass, ApplicableClass[] applicableClasses) {
        this.label = label;
        this.conditionClass = conditionClass;
        this.applicableClasses = applicableClasses;
    }

    public String getLabel() {
        return label;
    }

    public Class<? extends QueryCondition> getConditionClass() {
        return conditionClass;
    }

    @Override
    public String toString() {
        return getLabel();
    }

    public static FilterCondition[] getApplicableConditions(Class<?> type) {
        List<FilterCondition> app = new ArrayList<>();
        for (FilterCondition filterCondition : FilterCondition.values()) {
            if (ApplicableClass.isApplicable(type, filterCondition.applicableClasses, true)) {
                app.add(filterCondition);
            }
        }
        return app.toArray(new FilterCondition[0]);
    }

    private static String msg(String key) {
        return Messages.get(FilterCondition.class, key);
    }

}
