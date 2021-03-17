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
package tools.dynamia.navigation;

public class NavigationNotAllowedException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -6714211373877978620L;
    private final NavigationElement navigationElement;

    public NavigationNotAllowedException(NavigationElement navigationElement, String message) {
        super(message);
        this.navigationElement = navigationElement;
    }

    public NavigationElement getNavigationElement() {
        return navigationElement;
    }

}
