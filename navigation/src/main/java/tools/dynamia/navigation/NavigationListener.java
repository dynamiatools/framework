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
 * Listener for navigation events
 *
 * @author Ing. Mario Serrano Leones
 */
public interface NavigationListener {

    /**
     * Fired when some page in loaded
     *
     * @param evt
     */
    void onPageLoad(PageEvent evt);

    /**
     * Fired when current page is unloaded
     *
     * @param evt
     */
    void onPageUnload(PageEvent evt);

    /**
     * Fired some page is closed
     *
     * @param evt
     */
    void onPageClose(PageEvent evt);
}
