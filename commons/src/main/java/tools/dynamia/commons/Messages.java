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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Useful class for Locatization, iternally it use ResourceBundles to get
 * message in differente languages. To use this class you should create
 * Messages.properties files for each Locale (Messages_en.properties, etc) and
 * place that files in the same location (package) of the class you want
 * internationalize.
 * <p>
 * Example:<br/>
 * <p>
 * Class: <b>my.company.SomeClass</b> need a <b>Messages.properties</b> file
 * placed in /my/company/ in the classpath
 *
 * <p>
 * That file will be use it to all classes in the same package
 * </p>
 *
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public class Messages {


    private static Supplier<Collection<LocaleProvider>> localeProviders;

    /**
     * Get the ResourceBundle for the package of the class using the default JVM
     * Locale. For example the ResourceBundle for my.company.SomeClass will be
     * /my/company/Messages.properties
     *
     * @param clazz the clazz
     * @return the bundle
     */
    public static ResourceBundle getBundle(Class<?> clazz) {
        return getBundle(clazz, getDefaultLocale());
    }

    /**
     * Get the ResourceBundle for the package of the class using the Locale. For
     * example the ResourceBundle for my.company.SomeClass will be
     * /my/company/Messages.properties
     *
     * @param clazz  the clazz
     * @param locale the locale
     * @return the bundle
     */
    public static ResourceBundle getBundle(Class<?> clazz, Locale locale) {
        String baseName = clazz.getPackage().getName() + ".Messages";

        return ResourceBundle.getBundle(baseName, locale);
    }

    /**
     * Get the message for the specified key in the ResourceBundle.
     *
     * @param clazz the clazz
     * @param key   the key
     * @return the string
     */
    public static String get(Class<?> clazz, String key) {
        return get(clazz, getDefaultLocale(), key, new Object[0]);
    }

    /**
     * Get the message for the specified key in the ResourceBundle using the
     * specified locale.
     *
     * @param clazz  the clazz
     * @param locale the locale
     * @param key    the key
     * @return the string
     */
    public static String get(Class<?> clazz, Locale locale, String key) {
        return get(clazz, locale, key, new Object[0]);
    }

    /**
     * Get the message for the specified key in the ResourceBundle and parse the
     * parameters in the message. A message parameter is placed using the {0}
     * {1} {n} notation in the message text.
     * <p>
     * For example:<br/>
     * <br/>
     * <b>Messages.properties</b><br/>
     * error_message: The systema has fail beacause {0}. Error code: {1}
     * <p/>
     *
     * @param clazz  the clazz
     * @param key    the key
     * @param params the params
     * @return the string
     */
    public static String get(Class<?> clazz, String key, Object... params) {
        return get(clazz, getDefaultLocale(), key, params);

    }

    /**
     * Get the message for the specified key in the ResourceBundle and parse the
     * parameters in the message. A message parameter is placed using the {0}
     * {1} {n} notation in the message text.
     * <p>
     * For example:<br/>
     * <br/>
     * <b>Messages.properties</b><br/>
     * error_message: The system has fail beacause {0}. Error code: {1}
     * <p/>
     *
     * @param clazz  the clazz
     * @param locale the locale
     * @param key    the key
     * @param params the params
     * @return the string
     */
    public static String get(Class<?> clazz, Locale locale, String key, Object... params) {
        if (key == null) {
            return "";
        }

        if (clazz == null) {
            return key;
        }

        String message = null;
        try {
            message = getBundle(clazz, locale).getString(key);
            if (params != null && params.length > 0) {
                message = format(message, params);
            }
        } catch (MissingResourceException e) {
            message = key;
        }
        return message;
    }

    public static String format(String message, Object... params) {
        return MessageFormat.format(message, params);
    }

    /**
     * Return the default Locale usef for this class.
     *
     * @return the default locale
     */
    public static Locale getDefaultLocale() {


        if (localeProviders != null) {
            List<LocaleProvider> providers = localeProviders.get().stream().sorted(Comparator.comparingInt(LocaleProvider::getPriority)).collect(Collectors.toList());
            for (LocaleProvider provider : providers) {
                Locale locale = provider.getDefaultLocale();
                if (locale != null && locale.getLanguage() != null && !locale.getLanguage().isEmpty()) {

                    return locale;
                }
            }
        }
        return Locale.getDefault();
    }

    private Messages() {
    }

    public static void setLocaleProvidersSupplier(Supplier<Collection<LocaleProvider>> supplier) {
        localeProviders = supplier;
    }

    static {
        try {
            Files.readAllLines(Paths.get(Messages.class.getResource("/dynamia/banner.txt").toURI())).forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("----------------------------------------------------");
            System.out.println("DynamiaTools is Powered By: Dynamia Soluciones IT");
            System.out.println("https://dynamia.tools");
            System.out.println("https://www.dynamiasoluciones.com");
            System.out.println("----------------------------------------------------");
        }
    }

}


