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
package tools.dynamia.navigation;

import tools.dynamia.integration.Containers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Mario A. Serrano Leones
 */
public class NavigationRestrictions {


    public static boolean allowAccess(Page page) {
        return allowAccess((NavigationElement) page);
    }

    public static boolean allowAccess(NavigationElement element) {

        if (element.isAlwaysAllowed()) {
            return true;
        }

        Collection<NavigationRestriction> result = Containers.get().findObjects(NavigationRestriction.class);
        List<NavigationRestriction> restrictions = new ArrayList<>(result);
        restrictions.sort(new NavigationRestrictionComparator());
        if (!restrictions.isEmpty()) {
            for (NavigationRestriction nr : restrictions) {
                Boolean response = nr.allowAccess(element);
                if (response != null) {
                    return response;
                }
            }
        }
        return true;
    }

    public static void verifyAccess(NavigationElement element) throws NavigationNotAllowedException {
        if (!allowAccess(element)) {
            throw new NavigationNotAllowedException(element, "Cannot navigate to " + element.getPrettyVirtualPath());
        }
    }

    public static boolean allowAccess(String path) {
        var elem = NavigationManager.getCurrent().findElement(path);
        if (elem != null) {
            return allowAccess(elem);
        } else {
            return false;
        }
    }

    private NavigationRestrictions() {
    }

}
