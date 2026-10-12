package tools.dynamia.navigation;

import tools.dynamia.commons.LocalizedMessagesProvider;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.integration.Containers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Resolves navigation labels through the registered {@link LocalizedMessagesProvider}s, independent of any UI
 * technology. The classifier is always {@code null}: it is the provider's job to derive any grouping it needs
 * from the key (the element's virtual path).
 */
final class NavigationLabels {

    private static final LoggingService LOGGER = LoggingService.get(NavigationLabels.class);

    private NavigationLabels() {
    }

    /**
     * Returns the registered providers sorted by priority (lower number first), or an empty list when no
     * container is available.
     */
    static List<LocalizedMessagesProvider> providers() {
        try {
            return Containers.get().findObjects(LocalizedMessagesProvider.class).stream()
                    .sorted(Comparator.comparingInt(LocalizedMessagesProvider::getPriority))
                    .toList();
        } catch (RuntimeException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Returns the first non-null provider message for the key, or {@code defaultValue} when none provides one.
     * Providers are asked with a {@code null} default, never with {@code defaultValue}: a provider that echoes the
     * default it receives when it has no message (as the ZK one does) would otherwise short-circuit the providers
     * with a lower priority. The default is only applied at the end.
     * A failing provider is skipped so it can never break the navigation tree.
     */
    static String resolve(List<LocalizedMessagesProvider> providers, String key, Locale locale, String defaultValue) {
        for (var provider : providers) {
            try {
                var message = provider.getMessage(key, null, locale, null);
                if (message != null) {
                    return message;
                }
            } catch (RuntimeException e) {
                LOGGER.warn("Localized messages provider " + provider.getClass().getName() + " failed for key " + key + ": " + e.getMessage());
            }
        }
        return defaultValue;
    }
}
