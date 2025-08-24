package tools.dynamia.web.util;

import tools.dynamia.commons.LocaleProvider;
import tools.dynamia.integration.sterotypes.Component;

import java.util.Locale;


@Component
public class SessionLocaleProvider implements LocaleProvider {
    @Override
    public int getPriority() {
        return 99;
    }

    @Override
    public Locale getDefaultLocale() {
        try {
            return (Locale) SessionCache.getInstance().get("session-locale");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Sets the locale in the user session
     *
     * @param locale the new locale
     */
    public static void setSessionLocale(Locale locale) {
        try {
            SessionCache.getInstance().add("session-locale", locale);
        } catch (Exception e) {
            //ignore
        }
    }
}
