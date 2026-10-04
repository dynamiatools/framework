package tools.dynamia.crud;

import org.junit.jupiter.api.Test;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionEventBuilder;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutoExecuteActionCrudStateChangedListenerTest {

    private static class CountingAction extends AbstractCrudAction {
        final AtomicInteger runs = new AtomicInteger();

        @Override
        public void actionPerformed(CrudActionEvent evt) {
            runs.incrementAndGet();
        }
    }

    private static CrudViewComponent<?> view(List<CrudAction> actions) {
        return (CrudViewComponent<?>) Proxy.newProxyInstance(CrudViewComponent.class.getClassLoader(),
                new Class[]{CrudViewComponent.class, ActionEventBuilder.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getActions" -> actions;
                    case "buildActionEvent" -> new CrudActionEvent(null, args[0], (CrudViewComponent<?>) proxy, null);
                    default -> null;
                });
    }

    @Test
    void runsOnlyTheActionsMarkedAsAutoexecute() {
        var auto = new CountingAction();
        auto.setAttribute("autoexecute", Boolean.TRUE);
        var manual = new CountingAction();

        new AutoExecuteActionCrudStateChangedListener()
                .changedState(new ChangedStateEvent(CrudState.READ, CrudState.CREATE, view(List.of(auto, manual))));

        assertEquals(1, auto.runs.get());
        assertEquals(0, manual.runs.get());
    }

    @Test
    void doesNothingWhenTheViewHasNoActions() {
        new AutoExecuteActionCrudStateChangedListener()
                .changedState(new ChangedStateEvent(CrudState.READ, CrudState.CREATE, view(null)));
    }
}
