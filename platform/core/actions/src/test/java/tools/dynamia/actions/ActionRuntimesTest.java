package tools.dynamia.actions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionRuntimesTest {

    static class Plain extends AbstractAction {
    }

    static class Headless extends AbstractAction implements HeadlessCapable {
    }

    static class OptedOut extends AbstractAction implements HeadlessCapable {
        @Override
        public boolean headlessSupported() {
            return false;
        }
    }

    @RunsOn(ActionRuntime.FRONTEND)
    static class Find extends AbstractAction {
    }

    @RunsOn(ActionRuntime.FRONTEND)
    static class Legacy extends AbstractAction implements HeadlessCapable {
    }

    @RunsOn(ActionRuntime.HEADLESS)
    static class Declared extends AbstractAction {
    }

    static class SubOfDeclared extends Declared {
    }

    static class SubOfMarked extends Headless {
    }

    @RunsOn(ActionRuntime.HEADLESS)
    static class DeclaredAgainInSub extends SubOfDeclared {
    }

    @Test
    void aSubclassDoesNotInheritWhatItsParentDeclaredNorTheMarker() {
        assertEquals(ActionRuntime.HEADLESS, ActionRuntimes.of(new Declared()));
        assertEquals(ActionRuntime.UNDECLARED, ActionRuntimes.of(new SubOfDeclared()));
        assertEquals(ActionRuntime.UNDECLARED, ActionRuntimes.of(new SubOfMarked()));
        assertEquals(ActionRuntime.HEADLESS, ActionRuntimes.of(new DeclaredAgainInSub()), "it can declare it itself");
    }

    @Test
    void theDefaultRemoteIdDropsTheActionSuffix() {
        assertEquals("headless", ActionRuntimes.headlessId(Headless.class));
        assertEquals("find", ActionRuntimes.headlessId(Find.class));
    }

    @Test
    void derivesFromTheTypeAndLetsAnActionDeclareIt() {
        assertEquals(ActionRuntime.UNDECLARED, ActionRuntimes.of(new Plain()));
        assertEquals(ActionRuntime.HEADLESS, ActionRuntimes.of(new Headless()));
        assertEquals(ActionRuntime.UNDECLARED, ActionRuntimes.of(new OptedOut()));
        assertEquals(ActionRuntime.FRONTEND, ActionRuntimes.of(new Find()));
        assertEquals(ActionRuntime.FRONTEND, ActionRuntimes.of(new Legacy()), "a declaration beats the derivation");
    }
}
