package tools.dynamia.modules.saas;

import org.junit.jupiter.api.Test;
import tools.dynamia.integration.ScopedValueObjectContainer;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.integration.scheduling.SchedulerUtil;
import tools.dynamia.modules.saas.api.AccountException;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link AccountSessionHolder#get()} reads the holder from the {@link ScopedValueObjectContainer}, which is how the
 * holder reaches the async tasks started with {@link SchedulerUtil}.
 */
class AccountSessionHolderScopeTest {

    @Test
    void getReturnsTheHolderBoundInTheScope() {
        var holder = new AccountSessionHolder(null);
        var container = new SimpleObjectContainer();
        container.addObject(holder);

        ScopedValueObjectContainer.run(container, () -> assertSame(holder, AccountSessionHolder.get()));
    }

    @Test
    void getFailsClearlyWhenThereIsNoHolder() {
        assertThrows(AccountException.class, AccountSessionHolder::get);
    }

    @Test
    void anAsyncTaskStartedFromAScopeSeesTheSameHolder() throws Exception {
        var holder = new AccountSessionHolder(null);
        var container = new SimpleObjectContainer();
        container.addObject(holder);

        var seen = ScopedValueObjectContainer.get(container, () -> {
            try {
                return SchedulerUtil.runWithResult(AccountSessionHolder::get).get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        assertSame(holder, seen);
    }
}
