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

package tools.dynamia.domain;

import tools.dynamia.commons.reflect.PropertyInfo;

/**
 * Provider class to help {@link tools.dynamia.domain.util.DataTransferObjectBuilder}  to  transfer from source objet to DTO
 * unknow properties values. For example you can convert a URL object to String representation for some 'location' property
 * in DTO class.
 */
public interface DataTransferObjectPropertyProvider {

    /**
     * Find and transfer property in DTO class
     * @return true if transfer is completed
     */
    boolean transferPropertyValue(Object dto, Object propertyValue, PropertyInfo propertyInfo);
}
