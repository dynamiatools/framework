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

import java.text.MessageFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Supplier;


/**
 * <p>
 * Messages is a utility class for internationalization and localization in Java applications. It provides convenient methods to retrieve localized messages and resources using {@link ResourceBundle} files, supporting multiple locales and time zones.
 * </p>
 *
 * <p>
 * To use this class, create Messages.properties files for each locale (e.g., Messages_en.properties, Messages_es.properties) and place them in the same package as the class you want to internationalize. The class will automatically resolve the correct resource bundle based on the package and locale.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 *     String msg = Messages.get(MyClass.class, "welcome.message");
 *     String msgWithParams = Messages.get(MyClass.class, "error.message", "File not found", 404);
 * </pre>
 * </p>
 *
 * <p>
 * Messages also supports custom providers for locale and time zone resolution, allowing integration with frameworks or user preferences.
 * </p>
 *
 * @author Mario A. Serrano Leones
 */
public class Messages {


    /**
     * The locale providers.
     */
    private static Supplier<Collection<LocaleProvider>> localeProviders;

    /**
     * The time zone providers.
     */
    private static Supplier<Collection<TimeZoneProvider>> timeZoneProviders;

    /**
     * Returns the {@link ResourceBundle} for the package of the given class using the default JVM locale.
     * The bundle file should be named Messages.properties and placed in the same package as the class.
     *
     * @param clazz the class whose package is used to locate the resource bundle
     * @return the resource bundle for the class's package
     */
    public static ResourceBundle getBundle(Class<?> clazz) {
        return getBundle(clazz, getDefaultLocale());
    }

    /**
     * Returns the {@link ResourceBundle} for the package of the given class using the specified locale.
     *
     * @param clazz  the class whose package is used to locate the resource bundle
     * @param locale the locale to use for resource resolution
     * @return the resource bundle for the class's package and locale
     */
    public static ResourceBundle getBundle(Class<?> clazz, Locale locale) {
        String baseName = clazz.getPackage().getName() + ".Messages";

        return ResourceBundle.getBundle(baseName, locale);
    }

    /**
     * Retrieves the localized message for the specified key from the resource bundle of the given class, using the default locale.
     *
     * @param clazz the class whose resource bundle is used
     * @param key   the message key
     * @return the localized message, or the key if not found
     */
    public static String get(Class<?> clazz, String key) {
        return get(clazz, getDefaultLocale(), key, new Object[0]);
    }

    /**
     * Retrieves the localized message for the specified key from the resource bundle of the given class and locale.
     *
     * @param clazz  the class whose resource bundle is used
     * @param locale the locale to use
     * @param key    the message key
     * @return the localized message, or the key if not found
     */
    public static String get(Class<?> clazz, Locale locale, String key) {
        return get(clazz, locale, key, new Object[0]);
    }

    /**
     * Retrieves the localized message for the specified key and formats it with the given parameters, using the default locale.
     * Message parameters are placed using the {0}, {1}, {n} notation in the message text.
     *
     * Example in Messages.properties:
     * error_message: The system has failed because {0}. Error code: {1}
     *
     * @param clazz  the class whose resource bundle is used
     * @param key    the message key
     * @param params the parameters to format into the message
     * @return the formatted localized message, or the key if not found
     */
    public static String get(Class<?> clazz, String key, Object... params) {
        return get(clazz, getDefaultLocale(), key, params);

    }

    /**
     * Retrieves the localized message for the specified key and formats it with the given parameters, using the specified locale.
     * Message parameters are placed using the {0}, {1}, {n} notation in the message text.
     *
     * Example in Messages.properties:
     * error_message: The system has failed because {0}. Error code: {1}
     *
     * @param clazz  the class whose resource bundle is used
     * @param locale the locale to use
     * @param key    the message key
     * @param params the parameters to format into the message
     * @return the formatted localized message, or the key if not found
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

    /**
     * Formats a message string using the given parameters, following the MessageFormat pattern.
     *
     * @param message the message pattern
     * @param params  the parameters to format into the message
     * @return the formatted message string
     */
    public static String format(String message, Object... params) {
        return MessageFormat.format(message, params);
    }

    /**
     * Returns the default {@link Locale} used for message resolution.
     * If custom locale providers are configured, the highest priority provider is used.
     * Otherwise, the JVM default locale is returned.
     *
     * @return the default locale
     */
    public static Locale getDefaultLocale() {

        if (localeProviders != null) {
            List<LocaleProvider> providers = localeProviders.get().stream().sorted(Comparator.comparingInt(LocaleProvider::getPriority)).toList();
            for (LocaleProvider provider : providers) {
                Locale locale = provider.getDefaultLocale();
                if (locale != null && locale.getLanguage() != null && !locale.getLanguage().isEmpty()) {

                    return locale;
                }
            }
        }
        return Locale.getDefault();
    }

    /**
     * Returns the default {@link ZoneId} used for time zone resolution.
     * If custom time zone providers are configured, the highest priority provider is used.
     * Otherwise, the system default time zone is returned.
     *
     * @return the default time zone
     */
    public static ZoneId getDefaultTimeZone() {

        if (timeZoneProviders != null) {
            List<TimeZoneProvider> providers = timeZoneProviders.get().stream().sorted(Comparator.comparingInt(TimeZoneProvider::getPriority)).toList();
            for (TimeZoneProvider provider : providers) {
                ZoneId timeZone = provider.getDefaultTimeZone();
                if (timeZone != null) {
                    return timeZone;
                }
            }
        }
        return ZoneId.systemDefault();
    }

    /**
     * Sets the supplier for locale providers, allowing custom locale resolution strategies.
     *
     * @param supplier the supplier of locale providers
     */
    public static void setLocaleProvidersSupplier(Supplier<Collection<LocaleProvider>> supplier) {
        localeProviders = supplier;
    }

    /**
     * Sets the supplier for time zone providers, allowing custom time zone resolution strategies.
     *
     * @param timeZoneProviders the supplier of time zone providers
     */
    public static void setTimeZoneProviders(Supplier<Collection<TimeZoneProvider>> timeZoneProviders) {
        Messages.timeZoneProviders = timeZoneProviders;
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Messages() {
    }
}
