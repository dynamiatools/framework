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
package tools.dynamia.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Ing. Mario Serrano Leones
 */
public class EntityWrapperController {

    private EntityWrapperController() {
        throw new IllegalAccessError("Private Constructor");
    }

    public static <T> List<EntityWrapper<T>> wrap(Collection<T> entities) {
        List<EntityWrapper<T>> result = new ArrayList<>();
        for (T entity : entities) {
            result.add(new EntityWrapper<>(entity));
        }
        return result;
    }

    public static <T> EntityWrapper<T> wrap(T entity) {
        return new EntityWrapper<>(entity);
    }

    public static <T> EntityWrapper<T> find(T entity, List<? extends EntityWrapper<T>> wrappedEntities) {
        if (wrappedEntities != null && entity != null) {
            for (EntityWrapper<T> entityWrapper : wrappedEntities) {
                if (entityWrapper.getEntity().equals(entity)) {
                    return entityWrapper;
                }
            }
        }
        return null;
    }

    public static <T> T getFirstSelected(List<? extends EntityWrapper<T>> wrappedEntities) {
        if (wrappedEntities != null) {
            for (EntityWrapper<T> entityWrapper : wrappedEntities) {
                if (entityWrapper.isSelected()) {
                    return entityWrapper.getEntity();
                }
            }
        }
        return null;
    }

    public static <T> List<T> getAllSelected(List<? extends EntityWrapper<T>> wrappedEntities) {
        List<T> selectedList = new ArrayList<>();
        if (wrappedEntities != null) {
            for (EntityWrapper<T> entityWrapper : wrappedEntities) {
                if (entityWrapper.isSelected()) {
                    selectedList.add(entityWrapper.getEntity());
                }
            }
        }
        return selectedList;
    }

    public static <T> void setSelected(T entity, List<? extends EntityWrapper<T>> wrappedEntities) {
        if (wrappedEntities != null && entity != null) {
            for (EntityWrapper<T> entityWrapper : wrappedEntities) {
                entityWrapper.setSelected(entityWrapper.getEntity().equals(entity));
            }
        }
    }

    public static <T> void selectAll(List<T> entities, List<? extends EntityWrapper<T>> wrappedEntities) {
        if (entities != null && wrappedEntities != null) {
            for (EntityWrapper<T> entityWrapper : wrappedEntities) {
                if (entities.contains(entityWrapper.getEntity())) {
                    entityWrapper.setSelected(true);
                }
            }
        }
    }
}
