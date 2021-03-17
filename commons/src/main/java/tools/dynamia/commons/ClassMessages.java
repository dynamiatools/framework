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

import java.util.Locale;


/**
 * This is a utility class for multilanguage Messages in a specified class
 * package. Internally it use {@link Messages} class to get internationalize
 * messages
 *
 * @author Mario A. Serrano Leones
 */
@SuppressWarnings({"rawtypes"})
public class ClassMessages {

    /**
     * The clazz.
     */
    private final Class clazz;

    /**
     * The locale.
     */
    private final Locale locale;

    /**
     * Gets the.
     *
     * @param clazz the clazz
     * @return the class messages
     */
    public static ClassMessages get(Class clazz) {
        return new ClassMessages(clazz);
    }

    /**
     * Gets the.
     *
     * @param clazz the clazz
     * @param locale the locale
     * @return the class messages
     */
    public static ClassMessages get(Class clazz, Locale locale) {
        return new ClassMessages(clazz,locale);
    }

    /**
     * Instantiates a new class messages.
     *
     * @param clazz the clazz
     */
    private ClassMessages(Class clazz) {
        this.clazz = clazz;
        this.locale = Messages.getDefaultLocale();
    }

    /**
     * Instantiates a new class messages.
     *
     * @param clazz the clazz
     * @param locale the locale
     */
    private ClassMessages(Class clazz, Locale locale) {
        this.clazz = clazz;
        this.locale = locale;
    }

    /**
     * Gets the.
     *
     * @param key the key
     * @return the string
     */
    public String get(String key) {
        return Messages.get(clazz, locale, key);
    }

    /**
     * Gets the.
     *
     * @param key the key
     * @param params the params
     * @return the string
     */
    public String get(String key, Object... params) {
        return Messages.get(clazz, locale, key, params);
    }
}
