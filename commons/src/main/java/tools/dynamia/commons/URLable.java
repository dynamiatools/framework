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

package tools.dynamia.commons;

/**
 * The Interface URLable. Represents objects that can generate or expose URL string representations.
 * This interface is commonly implemented by entities, resources, and web components that need to
 * provide URL access or navigation capabilities. It's particularly useful for creating dynamic
 * links, REST endpoints, web resources, and navigation elements in web applications.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * public class Document implements URLable {
 *     private String id;
 *     private String baseUrl;
 *     
 *     public String toURL() {
 *         return baseUrl + "/documents/" + id;
 *     }
 *     
 *     public void url(String url) {
 *         this.baseUrl = url;
 *     }
 * }
 * </code>
 *
 * @author Mario A. Serrano Leones
 */
public interface URLable {

    /**
     * Converts this object to a URL string representation.
     *
     * @return the URL as string
     */
    String toURL();

    /**
     * Sets the URL for this object.
     *
     * @param url the URL to set
     */
    default void url(String url) {

    }
}
