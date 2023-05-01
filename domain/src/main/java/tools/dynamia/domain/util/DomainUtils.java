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
package tools.dynamia.domain.util;

import tools.dynamia.commons.BeanSorter;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.Identifiable;
import tools.dynamia.domain.CurrencyExchangeProvider;
import tools.dynamia.domain.EntityReferenceRepository;
import tools.dynamia.domain.EntityUtilsProvider;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.Parameter;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.services.GraphCrudService;
import tools.dynamia.domain.services.ValidatorService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.NotImplementationFoundException;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Objects;

/**
 * The Class DomainUtils.
 *
 * @author Mario Serrano Leones
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class DomainUtils {

    /**
     * The formatter.
     */
    private static DecimalFormat formatter;

    /**
     * Clean string.
     *
     * @param original    the original
     * @param onlyNumbers the only numbers
     * @return the string
     */
    public static String cleanString(String original, boolean onlyNumbers) {
        StringBuilder newStr = new StringBuilder();
        for (int i = 0; i < original.length(); i++) {
            char c = original.charAt(i);
            if (onlyNumbers) {
                if (Character.isDigit(c) || c == '-') {
                    newStr.append(c);
                }
            } else if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)) {
                newStr.append(c);
            }
        }

        return newStr.toString().toUpperCase();
    }

    /**
     * Clean string.
     *
     * @param original the original
     * @return the string
     */
    public static String cleanString(String original) {
        return cleanString(original, false);
    }

    /**
     * Builds the searcheable string.
     *
     * @param src the src
     * @return the string
     */
    public static String buildSearcheableString(Object src) {
        if (src == null) {
            return "%";
        }
        char comodin = '%';
        String string = src.toString();
        string = string.replace('*', comodin);
        StringBuilder sb = new StringBuilder();
        sb.append(comodin);
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (Character.isDigit(c) || Character.isLetter(c)) {
                sb.append(c);
            } else {
                sb.append(comodin);
            }
        }
        sb.append(comodin);
        return sb.toString();
    }


    /**
     * Configure sorter.
     *
     * @param queryText the query text
     * @param sorter    the sorter
     * @return the string
     */
    public static String configureSorter(String queryText, BeanSorter sorter) {
        if (sorter != null && sorter.getColumnName() != null && !sorter.getColumnName().isEmpty()) {

            String direction = sorter.isAscending() ? " asc " : " desc ";
            if (sorter.getColumnName().toLowerCase().endsWith(" desc")
                    || sorter.getColumnName().toLowerCase().endsWith(" asc")) {
                direction = " ";
            }

            return queryText + " order by " + sorter.getColumnName() + direction;
        } else {
            return queryText;
        }
    }

    /**
     * Simple round.
     *
     * @param n     the n
     * @param zeros the zeros
     * @return the big decimal
     */
    public static BigDecimal simpleRound(BigDecimal n, int zeros) {
        String patternBuilder = "1" +
                "0".repeat(Math.max(0, zeros));

        int pattern = Integer.parseInt(patternBuilder);

        long value = n.longValue();
        double dec = (double) value / Double.parseDouble(pattern + ".0");
        value = value / pattern;
        dec = dec - value;
        value *= pattern;
        if (dec > 0.5) {
            value += pattern;
        }
        return new BigDecimal(value);
    }

    /**
     * Simple round.
     *
     * @param n the n
     * @return the big decimal
     */
    public static BigDecimal simpleRound(BigDecimal n) {
        if (n == null) {
            return null;
        }

        if (formatter == null) {
            formatter = new DecimalFormat("###,###");
        }
        String val = formatter.format(n);

        val = val.replace(".", "").replace(",", "").trim();

        return new BigDecimal(val);
    }

    /**
     * Format a number using a number reference example: <code>
     * String result = formatNumberWithZeroes(5,1000);
     * Result is "0005";
     * <p>
     * result = formatNumberWithZeroes(89,1000);
     * Result should be "0089";
     * </code>.
     *
     * @param number          the number
     * @param numberReference the number reference
     * @return the string
     */
    public static String formatNumberWithZeroes(long number, long numberReference) {
        int numCeros = Long.toString(numberReference).length() - Long.toString(number).length();
        return "0".repeat(Math.max(0, numCeros)) + number;
    }

    /**
     * Sum.
     *
     * @param values the values
     * @return the big decimal
     */
    public static BigDecimal sum(BigDecimal... values) {
        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal v : values) {
            result = result.add(v);
        }
        return result;
    }

    /**
     * Substract.
     *
     * @param values the values
     * @return the big decimal
     */
    public static BigDecimal substract(BigDecimal... values) {
        BigDecimal result = null;
        for (BigDecimal v : values) {
            if (result == null) {
                result = v;
            } else {
                result = result.subtract(v);
            }
        }
        return result;
    }

    /**
     * Sum field.
     *
     * @param data      the data
     * @param clazz     the clazz
     * @param fieldName the field name
     * @return the big decimal
     */
    public static BigDecimal sumField(Collection data, Class clazz, String fieldName) {
        BigDecimal result = BigDecimal.ZERO;
        try {
            Method getField = clazz.getMethod(BeanUtils.formatGetMethod(fieldName));
            for (Object obj : data) {
                BigDecimal num = (BigDecimal) getField.invoke(obj);
                result = result.add(num);
            }
        } catch (Exception e) {
            throw new ValidationError("Error computing sum for field " + fieldName + ". Class: " + clazz);
        }
        return result;
    }


    /**
     * Gets the entity reference repository.
     *
     * @param entityClass the entity class
     * @return the entity reference repository
     */
    public static EntityReferenceRepository getEntityReferenceRepository(Class entityClass) {
        return getEntityReferenceRepository(entityClass.getName());
    }

    /**
     * Gets the entity reference repository or null if @{@link EntityReferenceRepository} not found.
     *
     * @param className the class name
     * @return the entity reference repository
     */
    public static EntityReferenceRepository getEntityReferenceRepository(String className) {
        if (className == null) {
            return null;
        }

        return Containers.get().findObjects(EntityReferenceRepository.class)
                .stream().filter(r -> r.getEntityClassName().equals(className))
                .findFirst()
                .orElse(null);


    }

    /**
     * Gets the entity reference repository by alias or null if {@link EntityReferenceRepository} not found.
     *
     * @param alias the alias
     * @return the entity reference repository by alias
     */
    public static EntityReferenceRepository getEntityReferenceRepositoryByAlias(String alias) {
        if (alias == null) {
            return null;
        }

        return Containers.get().findObjects(EntityReferenceRepository.class)
                .stream().filter(r -> r.getAlias().equals(alias))
                .findFirst()
                .orElse(null);
    }

    /**
     * Return {@link BigDecimal}.ZERO if number is null
     *
     */
    public static BigDecimal getZeroIfNull(BigDecimal number) {
        return Objects.requireNonNullElse(number, BigDecimal.ZERO);
    }


    /**
     * Find first implementation of CrudService. Throw {@link NotImplementationFoundException} if not found.
     * Use this method if you want create small queries for your entity
     *
     */
    public static CrudService lookupCrudService() {
        CrudService crudService = Containers.get().findObject(CrudService.class);
        if (crudService == null) {
            throw new NotImplementationFoundException("Cannot found a " + CrudService.class + " implementation");
        }
        return crudService;
    }

    /**
     * Find first implementation of CrudService. Throw {@link NotImplementationFoundException} if not found.
     * Use this method if you want create small queries for your entity
     *
     */
    public static <T extends CrudService> T lookupCrudService(Class<T> crudClass) {
        T crudService = Containers.get().findObject(crudClass);
        if (crudService == null) {
            throw new NotImplementationFoundException("Cannot found a " + crudClass + " implementation");
        }
        return crudService;
    }

    /**
     * Find first implementation of GraphCrudService.Throw {@link NotImplementationFoundException} if not found.
     * Use this method if you want create small queries for your entity
     *
     */
    public static GraphCrudService lookupGraphCrudService() {
        return lookupCrudService(GraphCrudService.class);
    }


    private DomainUtils() {
    }

    public static <E> Serializable findEntityId(E entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof Identifiable) {
            return ((Identifiable) entity).getId();
        } else {
            for (EntityUtilsProvider finder : Containers.get().findObjects(EntityUtilsProvider.class)) {
                Serializable id = finder.findId(entity);
                if (id != null) {
                    return id;
                }
            }
        }
        return null;
    }

    public static boolean isEntity(Object entity) {
        EntityUtilsProvider entityUtilsProvider = Containers.get().findObject(EntityUtilsProvider.class);
        if (entityUtilsProvider != null) {
            return entityUtilsProvider.isEntity(entity);
        }
        return false;
    }

    public static boolean isEntity(Class entityClass) {
        EntityUtilsProvider entityUtilsProvider = Containers.get().findObject(EntityUtilsProvider.class);
        if (entityUtilsProvider != null) {
            return entityUtilsProvider.isEntity(entityClass);
        }
        return false;
    }

    /**
     * By default return true
     *
     */
    public static boolean isPersitable(Field field) {
        EntityUtilsProvider entityUtilsProvider = Containers.get().findObject(EntityUtilsProvider.class);
        if (entityUtilsProvider != null) {
            return entityUtilsProvider.isPersitable(field);
        }
        return true;
    }

    /**
     * Find current {@link EntityUtilsProvider} implementation and get the default {@link Parameter} class
     *
     */
    public static Class<? extends Parameter> getDefaultParameterClass() {
        EntityUtilsProvider entityUtilsProvider = Containers.get().findObject(EntityUtilsProvider.class);
        if (entityUtilsProvider != null) {
            return entityUtilsProvider.getDefaultParameterClass();
        }
        return null;
    }

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
     *
     * @param target   The data source object
     * @param dtoClass DTO class
     * @param <DTO>    instance of DTO class with all common properties set
     */
    public static <DTO> DTO autoDataTransferObject(Object target, Class<DTO> dtoClass) {
        return DataTransferObjectBuilder.buildDTO(target, dtoClass);
    }


    /**
     * Shortcut utility to invoke {@link tools.dynamia.domain.services.ValidatorService} validation method
     *
     */
    public static void validate(Object obj) {
        ValidatorService service = Containers.get().findObject(ValidatorService.class);
        if (service != null) {
            service.validate(obj);
        }
    }

    /**
     * Shortcut method to Find EntityReferenceRepository by alias and then find EntityReference value using id
     *
     * @return name or null if nothing found
     */
    public static String getEntityReferenceName(String alias, Serializable id) {
        if (alias != null && id != null) {
            var repo = getEntityReferenceRepositoryByAlias(alias);
            if (repo != null) {
                var reference = repo.load(id);
                if (reference != null) {
                    return reference.getName();
                }
            }
        }
        return null;
    }

    /**
     * Increase counter and return de new value
     *
     */
    public static long findNextCounterValue(Object entity, String counterName) {
        var crud = lookupCrudService();
        crud.increaseCounter(entity, counterName);
        return crud.getFieldValue(entity, counterName, Long.class);
    }

    /**
     * Find an instance of {@link CurrencyExchangeProvider}
     *
     * @return null if nothing was found
     */
    public static CurrencyExchangeProvider getCurrencyExchangeProvider() {
        return Containers.get().findObject(CurrencyExchangeProvider.class);
    }
}
