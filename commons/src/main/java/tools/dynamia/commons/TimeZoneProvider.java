package tools.dynamia.commons;

import java.time.ZoneId;

/**
 * Interface for providing time zone information.
 */
public interface TimeZoneProvider {

    /**
     * Higher number, lower priority
     */
    int getPriority();

    /**
     * Returns the default time zone for the application.
     *
     * @return the default ZoneId
     */
    ZoneId getDefaultTimeZone();
}
