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

    @RunsOn(ActionRuntime.ZK_ONLY)
    static class Find extends AbstractAction {
    }

    @RunsOn(ActionRuntime.ZK_ONLY)
    static class Legacy extends AbstractAction implements HeadlessCapable {
    }

    @Test
    void derivesFromTheTypeAndLetsAnActionDeclareIt() {
        assertEquals(ActionRuntime.UNDECLARED, ActionRuntimes.of(new Plain()));
        assertEquals(ActionRuntime.HEADLESS, ActionRuntimes.of(new Headless()));
        assertEquals(ActionRuntime.UNDECLARED, ActionRuntimes.of(new OptedOut()));
        assertEquals(ActionRuntime.ZK_ONLY, ActionRuntimes.of(new Find()));
        assertEquals(ActionRuntime.ZK_ONLY, ActionRuntimes.of(new Legacy()), "a declaration beats the derivation");
    }
}
