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

package tools.dynamia.domain.util;

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.domain.DataTransferObjectPropertyProvider;
import tools.dynamia.integration.Containers;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Automatically pass all values from target class to a new instance of DTO class. DTO should be a POJO with getters and setters.
 * If target class has no standar java class properties, like other entity this method try to parse that value to a String or id property in DTO class.
 * Example: <br/>
 * <b>Case 1</b> <br/>
 * <pre>
 *
 *     class Target{
 *         private Category category:
 *         //get and set
 *     }
 *
 *     class TargetDTO{
 *         private String category;
 *         //get and set
 *     }
 * </pre>
 * <b>Case 2</b><br/>
 * <pre>
 *     class Target{
 *         private Category category:
 *         //get and set
 *     }
 *
 *     class TargetDTO{
 *         private Long categoryId;
 *         //get and set
 *     }
 * </pre>
 * <b>Case 3</b><br/>
 * <pre>
 *     class Target{
 *         private Category category:
 *         //get and set
 *     }
 *
 *     class TargetDTO{
 *         private Long categoryId;
 *         private String category;
 *         //getters and setters
 *     }
 * </pre>
 */
public class DataTransferObjectBuilder {

    private final static LoggingService LOGGER = new SLF4JLoggingService(DataTransferObjectBuilder.class);

    private DataTransferObjectBuilder() {
        //empty
    }

    static <D> D buildDTO(Object target, Class<D> dtoClass) {
        D dto = BeanUtils.newInstance(dtoClass);
        BeanUtils.setupBean(dto, target);
        List<PropertyInfo> properties = BeanUtils.getPropertiesInfo(target.getClass());
        for (PropertyInfo p : properties) {
            Object value = null;
            if (p.getField() != null) {
                value = BeanUtils.getFieldValue(p.getName(), target);
            }
            if (value != null && DomainUtils.isEntity(value)) {
                autoTransferIdProperty(dtoClass, dto, p, value);
                autoTransferStringProperty(dtoClass, dto, p, value);
                autoTransferUnknowProperty(dto, p, value);
            }
        }
        return dto;
    }

    private static <D> void autoTransferUnknowProperty(D dto, PropertyInfo p, Object value) {
        try {

            if (p.getField() == null || BeanUtils.getFieldValue(p.getName(), dto) == null) {
                //Find all instances of DataTransferObjectPropertyProvider to auto convert target property value to DTO value
                Collection<DataTransferObjectPropertyProvider> instances = Containers.get().findObjects(DataTransferObjectPropertyProvider.class);
                for (DataTransferObjectPropertyProvider atp : instances) {
                    if (atp.transferPropertyValue(dto, value, p)) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Cannot auto transfer Unknow  property " + dto + " -- >" + p);
        }
    }

    private static <D> void autoTransferStringProperty(Class<D> dtoClass, D dto, PropertyInfo p, Object value) {
        PropertyInfo dtoStringPro = BeanUtils.getPropertyInfo(dtoClass, p.getName());
        if (dtoStringPro != null && dtoStringPro.is(String.class)) {
            BeanUtils.setFieldValue(dtoStringPro, dto, value.toString());
        }
    }

    private static <D> void autoTransferIdProperty(Class<D> dtoClass, D dto, PropertyInfo p, Object value) {
        PropertyInfo dtoIdPro = BeanUtils.getPropertyInfo(dtoClass, p.getName() + "Id");
        if (dtoIdPro == null) {
            dtoIdPro = BeanUtils.getPropertyInfo(dtoClass, p.getName() + "_id");
        }
        if (dtoIdPro == null) {
            dtoIdPro = BeanUtils.getPropertyInfo(dtoClass, p.getName() + "ID");
        }

        if (dtoIdPro != null && dtoIdPro.is(Serializable.class)) {
            BeanUtils.setFieldValue(dtoIdPro, dto, DomainUtils.findEntityId(value));
        }
    }
}
