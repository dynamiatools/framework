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
package tools.dynamia.navigation.restrictions;

import org.springframework.stereotype.Component;
import tools.dynamia.navigation.NavigationElement;
import tools.dynamia.navigation.NavigationRestriction;

/**
 *
 * @author Mario A. Serrano Leones
 */
@Component
public class NoVisibleRestriction implements NavigationRestriction {

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Boolean allowAccess(NavigationElement element) {
        if (!element.isVisible()) {
            return false;
        }

        return null;
    }
}
