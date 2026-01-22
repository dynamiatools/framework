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

package tools.dynamia.modules.entityfile;

import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.domain.DataTransferObjectPropertyProvider;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.modules.entityfile.domain.EntityFile;

@Provider
public class DTOEntityFileToStringProperty implements DataTransferObjectPropertyProvider {

    private LoggingService logger = new SLF4JLoggingService(DTOEntityFileToStringProperty.class);

    @Override
    public boolean transferPropertyValue(Object dto, Object propertyValue, PropertyInfo propertyInfo) {
        if (propertyValue instanceof EntityFile) {
            EntityFile entityFile = (EntityFile) propertyValue;
            PropertyInfo dtoProperty = ObjectOperations.getPropertyInfo(dto.getClass(), propertyInfo.getName());
            if (dtoProperty != null && dtoProperty.is(String.class)) {
                try {
                    String url = entityFile.getStoredEntityFile().getUrl();
                    ObjectOperations.setFieldValue(dtoProperty, dto, url);
                    return true;
                } catch (Exception e) {
                    logger.error("Cannot transfer property value " + propertyValue + " to DTO  " + dto);
                }
            }
        }

        return false;
    }
}
