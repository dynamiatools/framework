package tools.dynamia.web.util;

import tools.dynamia.commons.TimeZoneProvider;
import tools.dynamia.integration.sterotypes.Component;

import java.time.ZoneId;
import java.util.Locale;

/**
 * Provides the time zone stored in the user session
 */
@Component
public class SessionTimeZoneProvider implements TimeZoneProvider {
    @Override
    public int getPriority() {
        return 99;
    }


    @Override
    public ZoneId getDefaultTimeZone() {
        try {
            return (ZoneId) SessionCache.getInstance().get("session-timezone");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Sets the time zone in the user session
     *
     * @param zoneId the new time zone
     */
    public static void setSessionTimeZone(ZoneId zoneId) {
        try {
            SessionCache.getInstance().add("session-timezone", zoneId);
        } catch (Exception e) {
        }
    }
}

