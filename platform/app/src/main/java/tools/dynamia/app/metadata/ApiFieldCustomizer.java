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

package tools.dynamia.app.metadata;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;
import tools.dynamia.commons.ObjectOperations;
import tools.dynamia.commons.logger.Loggable;
import tools.dynamia.domain.DefaultEntityReferenceRepository;
import tools.dynamia.domain.Reference;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.viewers.Field;
import tools.dynamia.viewers.FieldCustomizer;

import java.util.Arrays;
import java.util.List;

/**
 * Gives the form fields what a client rendered outside ZK (the Vue UI) needs and that only the ZK customizers
 * provided before:
 * <ul>
 *   <li><b>Enums:</b> the {@code ENUM_CONSTANTS} param, with the constant names, so a combobox has its options.</li>
 *   <li><b>Entities and collections of entities:</b> the {@code entityAlias} param, so the client can search the
 *   referenced entity with {@code /api/app/metadata/entities/ref/{alias}/search}. Entities that do not declare an
 *   {@code EntityReferenceRepository} get a {@link DefaultEntityReferenceRepository} registered on demand.</li>
 * </ul>
 * Params already set by the view descriptor are never overwritten.
 *
 * @author Mario A. Serrano Leones
 */
@Component
public class ApiFieldCustomizer implements FieldCustomizer, Loggable {

    static final String ENUM_CONSTANTS = "ENUM_CONSTANTS";
    static final String ENTITY_ALIAS = "entityAlias";
    private static final String MIN_CHARS = "minChars";

    /**
     * Properties tried, in order, to search a referenced entity by text.
     */
    private static final List<String> SEARCH_PROPERTIES = List.of("name", "title", "fullName", "label", "description");

    private final ConfigurableListableBeanFactory beanFactory;

    public ApiFieldCustomizer(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void customize(String viewTypeName, Field field) {
        if (!"form".equalsIgnoreCase(viewTypeName) || field == null) {
            return;
        }

        try {
            if (field.isEnum()) {
                customizeEnum(field);
            } else if (field.isEntity()) {
                customizeEntity(field);
            }
        } catch (RuntimeException e) {
            logWarn("Cannot customize field " + field.getName() + " for the API: " + e.getMessage());
        }
    }

    private void customizeEnum(Field field) {
        if (field.getParams().containsKey(ENUM_CONSTANTS)) {
            return;
        }
        var type = field.getFieldClass();
        if (type != null && type.isEnum()) {
            field.addParam(ENUM_CONSTANTS, Arrays.stream(type.getEnumConstants()).map(c -> ((Enum<?>) c).name()).toArray(String[]::new));
        }
    }

    private void customizeEntity(Field field) {
        if (field.getParams().containsKey(ENTITY_ALIAS)) {
            return;
        }

        var reference = field.getPropertyInfo() != null ? field.getPropertyInfo().getAnnotation(Reference.class) : null;
        if (reference != null) {
            field.addParam(ENTITY_ALIAS, reference.value());
            return;
        }

        Class<?> entityClass = field.isCollection() ? field.getGenericType() : field.getFieldClass();
        if (entityClass == null) {
            return;
        }

        var alias = findOrRegisterRepository(entityClass);
        if (alias != null) {
            field.addParam(ENTITY_ALIAS, alias);
            field.getParams().putIfAbsent(MIN_CHARS, 1);
        }
    }

    private synchronized String findOrRegisterRepository(Class<?> entityClass) {
        var existing = DomainUtils.getEntityReferenceRepository(entityClass);
        if (existing != null) {
            return existing.getAlias();
        }

        var repo = new DefaultEntityReferenceRepository<>(entityClass, searchFields(entityClass));
        beanFactory.registerSingleton("entityReferenceRepository" + entityClass.getSimpleName(), repo);
        log("Registered default EntityReferenceRepository for " + entityClass.getName() + " (alias " + repo.getAlias() + ")");
        return repo.getAlias();
    }

    private static String[] searchFields(Class<?> entityClass) {
        return SEARCH_PROPERTIES.stream()
                .filter(name -> ObjectOperations.getPropertyInfo(entityClass, name) != null)
                .limit(1)
                .toArray(String[]::new);
    }
}
