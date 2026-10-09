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
}
