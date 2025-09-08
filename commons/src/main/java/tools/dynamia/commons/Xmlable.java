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
 * Interface for POJO classes that can be converted to XML strings.
 * <p>
 * Implementing this interface allows an object to be serialized to XML using the {@link StringPojoParser#convertPojoToXml(Object)} utility method.
 * <p>
 * Typical usage:
 * <pre>
 *     public class MyPojo implements Xmlable {
 *         // fields and methods
 *     }
 *     MyPojo pojo = new MyPojo();
 *     String xml = pojo.toXml();
 * </pre>
 * <p>
 * This is useful for exporting, logging, or transmitting object data in XML format.
 */
public interface Xmlable {

    /**
     * Converts this POJO instance to an XML string representation.
     * <p>
     * Uses {@link StringPojoParser#convertPojoToXml(Object)} to perform the conversion.
     * <p>
     * The resulting XML will include all serializable fields of the implementing class.
     *
     * @return XML string representation of this object
     */
    default String toXml() {
        return StringPojoParser.convertPojoToXml(this);
    }
}
