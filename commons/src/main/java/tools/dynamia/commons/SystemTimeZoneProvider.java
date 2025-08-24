package tools.dynamia.commons;

import java.time.ZoneId;

/**
 * SystemTimeZoneProvider is an implementation of {@link TimeZoneProvider} that provides the system default time zone.
 * <p>
 * This provider always returns the default {@link ZoneId} of the JVM, regardless of context.
 * <p>
 * The priority for this provider is set to 1000, making it a fallback when other providers are not available.
 *
 * @author Mario
 */
public class SystemTimeZoneProvider implements TimeZoneProvider {
    /**
     * Returns the priority of this provider. Higher values indicate higher priority.
     *
     * @return the priority value (1000)
     */
    @Override
    public int getPriority() {
        return 1000;
    }

    /**
     * Returns the default {@link ZoneId} of the JVM.
     * <p>
     * This method always returns {@link ZoneId#systemDefault()}.
     *
     * @return the system default ZoneId
     */
    @Override
    public ZoneId getDefaultTimeZone() {
        return ZoneId.systemDefault();
    }
}
