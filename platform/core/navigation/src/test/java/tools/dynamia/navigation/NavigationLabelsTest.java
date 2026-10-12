package tools.dynamia.navigation;

import org.junit.jupiter.api.Test;
import tools.dynamia.commons.LocalizedMessagesProvider;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NavigationLabelsTest {

    private static LocalizedMessagesProvider provider(String value) {
        return (key, classifier, locale, defaultValue) -> {
            assertNull(classifier);
            return value;
        };
    }

    @Test
    void firstNonNullProviderWins() {
        var providers = List.of(provider(null), provider("Ventas"), provider("Sales"));
        assertEquals("Ventas", NavigationLabels.resolve(providers, "store/sales", Locale.of("es"), "Sales"));
    }

    /** Like the ZK provider: answers the default it receives when it has no message of its own. */
    private static LocalizedMessagesProvider echoingDefault() {
        return (key, classifier, locale, defaultValue) -> defaultValue;
    }

    @Test
    void aProviderThatEchoesTheDefaultDoesNotShortCircuitLowerPriorityProviders() {
        var providers = List.of(echoingDefault(), provider("Ventas"));
        assertEquals("Ventas", NavigationLabels.resolve(providers, "store/sales", Locale.of("es"), "Sales"));
    }

    @Test
    void defaultIsAppliedAtTheEndWhenOnlyEchoingProvidersExist() {
        assertEquals("Sales", NavigationLabels.resolve(List.of(echoingDefault()), "store/sales", Locale.ENGLISH, "Sales"));
    }

    @Test
    void fallsBackToDefaultWithoutProviders() {
        assertEquals("Sales", NavigationLabels.resolve(List.of(), "store/sales", Locale.ENGLISH, "Sales"));
    }

    @Test
    void failingProviderIsSkipped() {
        LocalizedMessagesProvider failing = (key, classifier, locale, defaultValue) -> {
            throw new IllegalStateException("boom");
        };
        assertEquals("Ventas", NavigationLabels.resolve(List.of(failing, provider("Ventas")), "store/sales", Locale.ENGLISH, "Sales"));
    }

    @Test
    void nodeUsesLocalizedElementNameWhenNoProviders() {
        var page = new Page("sales", "Sales", "store/sales");
        assertEquals("Sales", new NavigationNode(page).getName());
    }
}
