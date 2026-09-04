package tools.dynamia.app.controllers;

import org.junit.jupiter.api.Test;
import tools.dynamia.actions.AbstractAction;
import tools.dynamia.actions.AbstractRemoteAction;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.commons.ApplicableClass;
import tools.dynamia.crud.CrudRemoteAction;
import tools.dynamia.crud.CrudState;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies {@link ApplicationMetadataController}'s generic {@code CrudRemoteAction.getApplicableStates()}
 * guard (§7.6 point 2 of {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md}): a request whose inferred
 * {@link CrudState} isn't declared applicable must be rejected before the action ever runs.
 */
class ApplicationMetadataControllerTest {

    private static class FakeCrudRemoteAction extends AbstractAction implements CrudRemoteAction {

        private final CrudState[] applicableStates;

        FakeCrudRemoteAction(CrudState... applicableStates) {
            this.applicableStates = applicableStates;
        }

        @Override
        public CrudState[] getApplicableStates() {
            return applicableStates;
        }

        @Override
        public ApplicableClass[] getApplicableClasses() {
            return new ApplicableClass[0];
        }

        @Override
        public ActionExecutionResponse execute(ActionExecutionRequest request) {
            return new ActionExecutionResponse();
        }
    }

    @Test
    void allowsNonCrudRemoteActionRegardlessOfRequest() {
        var plainAction = new AbstractRemoteAction() {
            @Override
            public ActionExecutionResponse execute(ActionExecutionRequest request) {
                return new ActionExecutionResponse();
            }
        };
        assertTrue(ApplicationMetadataController.isApplicableState(plainAction, new ActionExecutionRequest()));
    }

    @Test
    void allowsCrudRemoteActionWithNoDeclaredStates() {
        var action = new FakeCrudRemoteAction();
        assertTrue(ApplicationMetadataController.isApplicableState(action, new ActionExecutionRequest()));
    }

    @Test
    void allowsCreateOnlyActionWhenRequestHasNoEntityId() {
        var action = new FakeCrudRemoteAction(CrudState.CREATE);
        var request = new ActionExecutionRequest(Map.of("title", "New book"));
        assertTrue(ApplicationMetadataController.isApplicableState(action, request));
    }

    @Test
    void rejectsCreateOnlyActionWhenRequestCarriesAnEntityId() {
        var action = new FakeCrudRemoteAction(CrudState.CREATE);
        var request = new ActionExecutionRequest();
        request.setDataId("42");
        assertFalse(ApplicationMetadataController.isApplicableState(action, request));
    }

    @Test
    void rejectsReadDeleteActionWhenRequestHasNoEntityId() {
        var action = new FakeCrudRemoteAction(CrudState.READ);
        var request = new ActionExecutionRequest(Map.of("title", "New book"));
        assertFalse(ApplicationMetadataController.isApplicableState(action, request));
    }

    @Test
    void allowsDeleteActionForSingleDataId() {
        var action = new FakeCrudRemoteAction(CrudState.READ);
        var request = new ActionExecutionRequest();
        request.setDataId("42");
        assertTrue(ApplicationMetadataController.isApplicableState(action, request));
    }

    @Test
    void allowsDeleteActionForBulkIdsMap() {
        var action = new FakeCrudRemoteAction(CrudState.READ);
        var request = new ActionExecutionRequest(Map.of("ids", List.of("1", "2", "3")));
        assertTrue(ApplicationMetadataController.isApplicableState(action, request));
    }

    @Test
    void rejectsDeleteActionForEmptyBulkIdsMap() {
        var action = new FakeCrudRemoteAction(CrudState.READ);
        var request = new ActionExecutionRequest(Map.of("ids", List.of()));
        assertFalse(ApplicationMetadataController.isApplicableState(action, request));
    }

    @Test
    void allowsDeleteActionForRawListBody() {
        var action = new FakeCrudRemoteAction(CrudState.READ);
        var request = new ActionExecutionRequest(List.of("1", "2"));
        assertTrue(ApplicationMetadataController.isApplicableState(action, request));
    }

    @Test
    void allowsUpdateActionWhenPayloadCarriesAnId() {
        var action = new FakeCrudRemoteAction(CrudState.UPDATE);
        var request = new ActionExecutionRequest(Map.of("id", "42", "title", "Updated"));
        assertTrue(ApplicationMetadataController.isApplicableState(action, request));
    }

    @Test
    void hasEntityIdRecognizesEveryCarrier() {
        var withDataId = new ActionExecutionRequest();
        withDataId.setDataId("1");
        assertTrue(ApplicationMetadataController.hasEntityId(withDataId));

        assertTrue(ApplicationMetadataController.hasEntityId(new ActionExecutionRequest(Map.of("id", "1"))));
        assertTrue(ApplicationMetadataController.hasEntityId(new ActionExecutionRequest(Map.of("ids", List.of("1")))));
        assertTrue(ApplicationMetadataController.hasEntityId(new ActionExecutionRequest(List.of("1"))));

        assertFalse(ApplicationMetadataController.hasEntityId(new ActionExecutionRequest()));
        assertFalse(ApplicationMetadataController.hasEntityId(new ActionExecutionRequest(Map.of("title", "x"))));
        assertFalse(ApplicationMetadataController.hasEntityId(new ActionExecutionRequest(Map.of("ids", List.of()))));
        assertFalse(ApplicationMetadataController.hasEntityId(new ActionExecutionRequest(List.of())));
    }
}
