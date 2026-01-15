package tools.dynamia.commons;

import java.time.ZoneId;

/**
 * Interface for providing time zone information to applications.
 * <p>
 * Implementations of this interface supply the default time zone and a priority value for selection among multiple providers.
 * <p>
 * Usage example:
 * <pre>
 *     public class AppTimeZoneProvider implements TimeZoneProvider {
 *         &#64;Override
 *         public int getPriority() {
 *             return 10;
 *         }
 *
 *         &#64;Override
 *         public ZoneId getDefaultTimeZone() {
 *             return ZoneId.of("America/Bogota");
 *         }
 *     }
 * </pre>
 * <p>
 * Priority: Lower values indicate higher priority. The provider with the lowest priority value will be preferred when multiple providers are available.
 * <p>
 * Typical use cases include customizing time zone handling for multi-tenant, internationalized, or user-specific applications.
 */
public interface TimeZoneProvider {

    /**
     * Returns the priority of this provider.
     * <p>
     * Lower values indicate higher priority. Used to select among multiple providers.
     *
     * @return the priority value (lower means higher priority)
     */
    int getPriority();

    /**
     * Returns the default time zone for the application or context.
     * <p>
     * Implementations should return a valid {@link ZoneId} representing the default time zone.
     *
     * @return the default {@link ZoneId} for this provider
     */
    ZoneId getDefaultTimeZone();
}
