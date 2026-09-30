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
     * A failing provider is skipped so it can never break the navigation tree.
     */
    static String resolve(List<LocalizedMessagesProvider> providers, String key, Locale locale, String defaultValue) {
        for (var provider : providers) {
            try {
                var message = provider.getMessage(key, null, locale, defaultValue);
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
