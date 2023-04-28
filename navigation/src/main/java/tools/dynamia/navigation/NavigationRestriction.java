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

/**
 * Implement this interface if you want control the access for a specific element(s)
 *
 * @author Mario A. Serrano Leones
 */
public interface NavigationRestriction {

    /**
     * Restriction order
     *
     * @return
     */
    int getOrder();

    /**
     * Return true if access is granted, false is access is denied or null if access is unknow to let other {@link NavigationRestriction} check
     *
     * @param element
     * @return
     */
    Boolean allowAccess(NavigationElement element);

}
