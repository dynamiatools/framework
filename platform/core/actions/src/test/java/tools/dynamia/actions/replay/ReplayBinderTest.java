package tools.dynamia.actions.replay;

import org.junit.jupiter.api.Test;
import tools.dynamia.ui.UIFacades;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Every {@link ReplayBinder} bean is applied around the action, so modules can add the headless side of a facade.
 */
class ReplayBinderTest {

    interface First {
        String name();
    }

    interface Second {
        String name();
    }

    private static <S> ReplayBinder binding(Class<S> spi, S implementation) {
        return new ReplayBinder() {
            @Override
            public <T> T bind(ReplaySession session, Supplier<T> work) {
                return UIFacades.with(spi, implementation, work);
            }
        };
    }

    @Test
    void allBindersAreVisibleToTheAction() {
        var session = new ReplaySession(List.of());
        var binders = List.of(binding(First.class, () -> "one"), binding(Second.class, () -> "two"));

        var seen = ReplayExecutor.bindAll(session, binders, 0,
                () -> UIFacades.bound(First.class).name() + "+" + UIFacades.bound(Second.class).name());

        assertEquals("one+two", seen);
    }

    @Test
    void noBindersJustRunsTheWork() {
        assertEquals("done", ReplayExecutor.bindAll(new ReplaySession(List.of()), List.of(), 0, () -> "done"));
    }
}
