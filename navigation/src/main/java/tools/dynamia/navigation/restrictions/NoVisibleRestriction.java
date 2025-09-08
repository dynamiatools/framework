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
package tools.dynamia.navigation.restrictions;

import org.springframework.stereotype.Component;
import tools.dynamia.navigation.NavigationElement;
import tools.dynamia.navigation.NavigationRestriction;

/**
 * <p>
 * Navigation restriction that denies access to navigation elements that are not visible.
 * This restriction is intended to be used as a last check in the restriction chain,
 * since it returns {@code null} if the element is visible, allowing other restrictions to decide.
 * </p>
 * <p>
 * Usage: Add this restriction as a Spring component to automatically apply visibility checks
 * to navigation elements in your application.
 * </p>
 *
 * @author Mario A. Serrano Leones
 * @since 2023
 */
@Component
public class NoVisibleRestriction implements NavigationRestriction {

    /**
     * Returns the order of this restriction. This restriction has the maximum order value,
     * meaning it will be evaluated last in the restriction chain.
     *
     * @return {@link Integer#MAX_VALUE} to indicate lowest priority in the chain
     */
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    /**
     * Checks if access to the given navigation element is allowed based on its visibility.
     * <ul>
     *     <li>If the element is not visible, access is denied ({@code false}).</li>
     *     <li>If the element is visible, returns {@code null} to allow other restrictions to decide.</li>
     * </ul>
     *
     * @param element the {@link NavigationElement} to check
     * @return {@code false} if the element is not visible, {@code null} otherwise
     */
    @Override
    public Boolean allowAccess(NavigationElement element) {
        if (!element.isVisible()) {
            return false;
        }

        return null;
    }
}
