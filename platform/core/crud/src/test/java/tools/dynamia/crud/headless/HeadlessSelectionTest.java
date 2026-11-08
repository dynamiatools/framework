package tools.dynamia.crud.headless;

import org.junit.jupiter.api.Test;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.crud.TestEntity;
import tools.dynamia.crud.actions.DeleteAction;
import tools.dynamia.domain.InMemoryCrudService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeadlessSelectionTest {

    private static TestEntity entity(String name) {
        var e = new TestEntity();
        e.setName(name);
        return e;
    }

    @Test
    void theSelectionIsEmptyWhenNothingIsSelected() {
        var controller = new HeadlessCrudController<TestEntity>(TestEntity.class, new InMemoryCrudService());

        assertTrue(controller.getSelection().isEmpty());
    }

    @Test
    void aSingleSelectedRecordIsTheSelection() {
        var controller = new HeadlessCrudController<TestEntity>(TestEntity.class, new InMemoryCrudService());
        var ana = entity("ana");
        controller.setSelected(ana);

        assertEquals(List.of(ana), controller.getSelection());
    }

    @Test
    void aBulkRequestSelectsAllItsRecordsInOrder() {
        var controller = new HeadlessCrudController<TestEntity>(TestEntity.class, new InMemoryCrudService());
        var ana = entity("ana");
        var luis = entity("luis");
        controller.setSelected(ana);
        controller.setSelection(List.of(ana, luis));

        assertEquals(List.of(ana, luis), controller.getSelection());
    }

    @Test
    void aBulkRequestOverTheLimitIsRefusedBeforeLoadingAnyRecord() {
        var crud = new InMemoryCrudService();
        var action = new HeadlessCrudRemoteAction(new DeleteAction());
        var ids = new ArrayList<Object>();
        for (long i = 1; i <= HeadlessCrudRemoteAction.MAX_BULK_IDS + 1; i++) {
            ids.add(i);
        }
        var request = new ActionExecutionRequest();
        request.setData(Map.of("ids", ids));

        var failure = assertThrows(IllegalArgumentException.class, () -> action.entitiesOf(request, TestEntity.class, crud));

        assertTrue(failure.getMessage().contains("at most"));
    }
}
