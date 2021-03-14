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
package tools.dynamia.commons;

import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.commons.reflect.PropertyInfo;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class handle internationalization of bean properties (getXxx and
 * setXxx). This allow you to internatinalize a bean class or POJO to many
 * languages. To use this class you need create a property file with the same
 * name in same package of the bean class. Inside that property file you put the
 * property (get/set) name in the language you want. This follow the same rules
 * of ResourceBundles.
 *
 * <p>
 * <b>Example:</b><br/>
 * <br/>
 * <b>bean class:</b> my.company.model.Product<br/>
 * <b>default locale:</b> /com/company/model/Product.properties<br/>
 * <b>spanish locale:</b> /com/company/model/Product_es.properties<br/>
 * <b>french locale:</b> /com/company/model/Product_fr.properties<br/>
 * </p>
 *
 * <p>
 * <b>Product_es.properties</b> content<br/>
 * Product: Producto<br/>
 * name: Nombre <br/>
 * price: Precio<br/>
 * image: Imagen<br/>
 * </p>
 *
 * <p>
 * This class also support inheritance, its means that you can localize a parent
 * class and all children will use the same locale info. You can also override
 * property translations in children properties files.
 * </p>
 *
 * @author Ing. Mario Serrano Leones
 */
public class BeanMessages {

    /**
     * The Constant GLOBAL_CACHE.
     */
    private static final Map<Class, Map<Locale, BeanMessages>> GLOBAL_CACHE = new ConcurrentHashMap<>();

    /**
     * Gets the.
     *
     * @param clazz  the clazz
     * @param locale the locale
     * @return the bean messages
     */
    public static BeanMessages get(Class clazz, Locale locale) {
        if (clazz == null) {
            return null;
        }


        Map<Locale, BeanMessages> subcache = GLOBAL_CACHE.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());

        BeanMessages msg = subcache.get(locale);

        if (msg == null) {
            msg = new BeanMessages(clazz, locale);
            subcache.put(locale, msg);
        }

        return msg;
    }

    /**
     * The bean class.
     */
    private Class<?> beanClass;

    /**
     * The locale.
     */
    private Locale locale;

    /**
     * The bundles.
     */
    private List<ResourceBundle> bundles = new ArrayList<>();

    /**
     * The logger.
     */
    private LoggingService logger = new SLF4JLoggingService(BeanMessages.class);

    /**
     * The cache.
     */
    private Map<String, String> cache = new ConcurrentHashMap<>();

    /**
     * Instantiates a new bean messages.
     *
     * @param beanClass the bean class
     */
    public BeanMessages(Class<?> beanClass) {
        this(beanClass, Messages.getDefaultLocale());
    }

    /**
     * Instantiates a new bean messages.
     *
     * @param beanClass the bean class
     * @param locale    the locale
     */
    public BeanMessages(Class<?> beanClass, Locale locale) {
        super();
        this.beanClass = beanClass;
        this.locale = locale;
        initBundles();

    }

    /**
     * Inits the bundles.
     */
    private void initBundles() {
        findBundle(beanClass);
    }

    /**
     * Find bundle.
     *
     * @param clazz the clazz
     */
    private void findBundle(Class<?> clazz) {
        if (clazz == null) {
            return;
        }

        try {
            if (clazz != Object.class) {

                ResourceBundle bundle = ResourceBundle.getBundle(clazz.getName(), getLocale());
                bundles.add(bundle);
            }
        } catch (MissingResourceException e) {
            logger.debug("ResourceBundle not found for " + clazz);
        } finally {
            if (clazz.getSuperclass() != null) {
                findBundle(clazz.getSuperclass());
            }
        }

    }

    /**
     * Return the current bean class.
     *
     * @return the bean class
     */
    public Class<?> getBeanClass() {
        return beanClass;
    }

    /**
     * return current locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Return the name of the bean class localized.
     *
     * @return the localized name
     */
    public String getLocalizedName() {
        String simpleName = beanClass.getSimpleName();
        String localizedName = getMessage(simpleName);

        if (simpleName.equals(localizedName)) {
            localizedName = StringUtils.addSpaceBetweenWords(simpleName);
        }
        return localizedName;
    }

    /**
     * return the bean propertyInfo's name localized.
     *
     * @param propertyInfo the property info
     * @return the message
     */
    public String getMessage(PropertyInfo propertyInfo) {
        return getMessage(propertyInfo.getName());
    }

    /**
     * return the bean propertyInfo's name localized.
     *
     * @param propertyInfo the property info
     * @param params       the params
     * @return the message
     */
    public String getMessage(PropertyInfo propertyInfo, Object... params) {
        return getMessage(propertyInfo.getName(), params);
    }

    /**
     * return the property's bean localized name.
     *
     * @param propertyName the property name
     * @return the message
     */
    public String getMessage(String propertyName) {
        return getMessage(propertyName, (Object[]) null);
    }

    /**
     * Gets the message.
     *
     * @param propertyName the property name
     * @param params       the params
     * @return the message
     */
    public String getMessage(String propertyName, Object... params) {
        if (propertyName == null) {
            return "";
        }

        String message = cache.get(propertyName);
        if (message == null) {

            for (ResourceBundle bundle : bundles) {
                if (bundle.containsKey(propertyName)) {
                    message = bundle.getString(propertyName);
                    cache.put(propertyName, message);
                    break;
                }
            }
        }

        if (message == null) {
            message = propertyName;
        } else if (params != null && params.length > 0) {
            message = MessageFormat.format(message, params, locale);
        }
        return message;
    }
}
