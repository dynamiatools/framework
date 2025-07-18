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
 * The Interface Xmlable. Allows POJO classes to be converted to XML string representations.
 * This interface provides automatic XML serialization capabilities for Plain Old Java Objects,
 * making it easy to convert object instances to XML format for data interchange, persistence,
 * configuration files, or API responses. The default implementation uses the framework's
 * StringPojoParser utility for automatic conversion.
 * <br><br>
 * <b>Usage:</b><br>
 * <br>
 * <code>
 * public class Product implements Xmlable {
 *     private String name;
 *     private double price;
 *     
 *     // The toXml() method is inherited and works automatically
 *     // Usage: String xml = product.toXml();
 * }
 * </code>
 *
 * @author Mario A. Serrano Leones
 */
public interface Xmlable {

    /**
     * Converts this object to an XML string representation.
     *
     * @return the XML as string
     */
    default String toXml() {
        return StringPojoParser.convertPojoToXml(this);
    }
}
