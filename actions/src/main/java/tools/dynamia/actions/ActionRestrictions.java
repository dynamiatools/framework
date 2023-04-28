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

package tools.dynamia.actions;

import tools.dynamia.integration.Containers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Utilis for manage actions restrictions
 */
public final class ActionRestrictions {

    private ActionRestrictions() {
    }

    public static Boolean allowAccess(Action action) {
        Boolean allowed = true;
        Collection<ActionRestriction> restrictions = Containers.get().findObjects(ActionRestriction.class);
        if (restrictions != null) {
            List<ActionRestriction> restrictionsSorted = new ArrayList<>(restrictions);
            restrictionsSorted.sort((Comparator.comparingInt(ActionRestriction::getOrder)));
            for (ActionRestriction actionRestriction : restrictionsSorted) {
                allowed = actionRestriction.actionAllowed(action);
                if (allowed != null) {
                    break;
                }
            }
        }
        return allowed;
    }
}
