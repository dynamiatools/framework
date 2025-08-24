package tools.dynamia.zk;

import org.springframework.stereotype.Component;
import org.zkoss.util.TimeZones;
import tools.dynamia.commons.TimeZoneProvider;
import tools.dynamia.zk.util.ZKUtil;

import java.time.ZoneId;

/**
 * ZKTimeZoneProvider is an implementation of {@link TimeZoneProvider} for ZK framework environments.
 * <p>
 * It provides the default {@link ZoneId} based on the current ZK event listener context, using ZKoss TimeZones utility.
 * If no ZK event listener is active or no time zone is set, it returns null.
 * <p>
 * The priority for this provider is set to 100.
 */
@Component
public class ZKTimeZoneProvider implements TimeZoneProvider {
    /**
     * Returns the priority of this provider. Higher values indicate higher priority.
     *
     * @return the priority value (100)
     */
    @Override
    public int getPriority() {
        return 100;
    }

    /**
     * Returns the default {@link ZoneId} for the current ZK context.
     * <p>
     * If called within a ZK event listener and a time zone is set, returns the corresponding ZoneId.
     * Otherwise, returns null.
     *
     * @return the default ZoneId for the current ZK context, or null if not available
     */
    @Override
    public ZoneId getDefaultTimeZone() {

        if (ZKUtil.isInEventListener()) {
            var current = TimeZones.getCurrent();
            if (current != null) {
                return ZoneId.of(current.getID());
            }
        }
        return null;
    }
}
