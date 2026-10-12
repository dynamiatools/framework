package tools.dynamia.modules.reports.core;

import tools.dynamia.integration.Containers;

import java.util.Locale;

/**
 * Front end used by the reports navigation pages.
 * <ul>
 *     <li>{@link #VUE} (default): pages are {@link tools.dynamia.modules.reports.core.navigation.ReportViewerPage}s that
 *     a Vue shell renders natively. The ZK shell still renders their legacy view.</li>
 *     <li>{@link #ZK}: legacy behaviour, plain ZK pages. A Vue shell embeds them with its ZK bridge.</li>
 * </ul>
 * Selected with the property {@code dynamia.reports.ui} ({@code vue} or {@code zk}).
 */
public enum ReportsUi {
    VUE, ZK;

    public static final String PROPERTY = "dynamia.reports.ui";

    /**
     * Resolves the front end from the application settings, then from the system property, defaulting to {@link #VUE}.
     */
    public static ReportsUi current() {
        try {
            var settings = Containers.get().findObject(ReportsSettings.class);
            if (settings != null) {
                return parse(settings.getUi());
            }
        } catch (RuntimeException ignored) {
            // no container yet, fall back to the system property
        }
        return parse(System.getProperty(PROPERTY));
    }

    /**
     * Parses a configuration value.
     *
     * @param value {@code vue} or {@code zk}, case-insensitive; blank means {@link #VUE}
     * @return the front end
     * @throws ReportsException if the value is not valid
     */
    public static ReportsUi parse(String value) {
        if (value == null || value.isBlank()) {
            return VUE;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ReportsException("Invalid value for " + PROPERTY + ": " + value + ". Use vue or zk");
        }
    }
}
