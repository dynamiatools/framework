package tools.dynamia.app.metadata;

import org.junit.jupiter.api.Test;
import tools.dynamia.actions.AbstractRemoteAction;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.FlowRemoteAction;
import tools.dynamia.actions.ActionFlowContext;
import tools.dynamia.actions.ActionFlowStep;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionMetadataRuntimeTest {

    static class Plain extends AbstractRemoteAction {
        @Override
        public ActionExecutionResponse execute(ActionExecutionRequest request) {
            return new ActionExecutionResponse();
        }
    }

    static class Flow extends AbstractRemoteAction implements FlowRemoteAction {
        @Override
        public ActionFlowStep start(ActionFlowContext ctx) {
            return ActionFlowStep.done(null);
        }

        @Override
        public ActionFlowStep resume(ActionFlowContext ctx, Object answer) {
            return ActionFlowStep.done(null);
        }
    }

    @Test
    void remoteActionsPublishTheirRuntime() {
        assertEquals("REMOTE", new ActionMetadata(new Plain()).getRuntime());
        assertEquals("FLOW", new ActionMetadata(new Flow()).getRuntime());
    }

    @tools.dynamia.actions.RunsOn(tools.dynamia.actions.ActionRuntime.FRONTEND)
    static class SearchBox extends tools.dynamia.actions.AbstractLocalAction {
        @Override
        public void actionPerformed(tools.dynamia.actions.ActionEvent evt) {
        }
    }

    @tools.dynamia.actions.RunsOn(tools.dynamia.actions.ActionRuntime.HEADLESS)
    static class Declared extends tools.dynamia.actions.AbstractLocalAction {
        @Override
        public void actionPerformed(tools.dynamia.actions.ActionEvent evt) {
        }
    }

    static class SubOfDeclared extends Declared {
    }

    @Test
    void frontendActionsArePublishedWithoutEndpointAndAreNotExecutable() {
        var metadata = new ActionMetadata(new SearchBox());

        assertEquals("FRONTEND", metadata.getRuntime());
        org.junit.jupiter.api.Assertions.assertNull(metadata.getEndpoint());
        org.junit.jupiter.api.Assertions.assertFalse(metadata.isExecutable());
    }

    @Test
    void aSubclassOfAHeadlessActionIsUndeclared() {
        assertEquals("HEADLESS", new ActionMetadata(new Declared()).getRuntime());
        assertEquals("UNDECLARED", new ActionMetadata(new SubOfDeclared()).getRuntime());
    }

    @Test
    void remoteActionsAreExecutable() {
        org.junit.jupiter.api.Assertions.assertTrue(new ActionMetadata(new Plain()).isExecutable());
    }
}
