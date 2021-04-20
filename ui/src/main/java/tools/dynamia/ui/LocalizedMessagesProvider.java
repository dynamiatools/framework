package tools.dynamia.ui;

import java.util.Locale;

/**
 * Basic API for user interface localization provider
 */
public interface LocalizedMessagesProvider {

    /**
     * Return a localized value based on locale
     *
     * @param key          target
     * @param classifier   group or category
     * @param locale       language
     * @param defaultValue value if not value word
     * @return a localized value
     */
    String getMessage(String key, String classifier, Locale locale, String defaultValue);

    default int getPriority() {
        return Integer.MAX_VALUE;
    }
}
