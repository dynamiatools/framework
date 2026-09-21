/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.actions;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ActionFlowsTest {

    /** Confirms, then echoes back whatever it was told to remember at start(). */
    static class ConfirmThenEchoAction extends AbstractRemoteAction implements FlowRemoteAction {
        @Override
        public ActionFlowStep start(ActionFlowContext ctx) {
            ctx.put("echo", ctx.get("value", String.class));
            return ActionFlowStep.confirm("Are you sure?", "Confirm");
        }

        @Override
        public ActionFlowStep resume(ActionFlowContext ctx, Object answer) {
            if (Boolean.TRUE.equals(answer)) {
                return ActionFlowStep.done(ctx.get("echo", String.class));
            }
            return ActionFlowStep.done(null);
        }
    }

    /** A second, differently-identified action — used to prove tokens don't cross actions. */
    static class OtherAction extends AbstractRemoteAction implements FlowRemoteAction {
        @Override
        public ActionFlowStep start(ActionFlowContext ctx) {
            return ActionFlowStep.done("n/a");
        }

        @Override
        public ActionFlowStep resume(ActionFlowContext ctx, Object answer) {
            return ActionFlowStep.done("n/a");
        }
    }

    /** CALLs another action, then reports the nested response it was resumed with plus what it remembered. */
    static class CallThenReportAction extends AbstractRemoteAction implements FlowRemoteAction {
        @Override
        public ActionFlowStep start(ActionFlowContext ctx) {
            ctx.put("remembered", "before-call");
            return ActionFlowStep.call("lookup", Map.of("q", "abc"));
        }

        @Override
        public ActionFlowStep resume(ActionFlowContext ctx, Object answer) {
            Map<?, ?> nested = (Map<?, ?>) answer;
            return ActionFlowStep.done(ctx.get("remembered", String.class) + ":" + nested.get("data"));
        }
    }

    @Test
    public void callStepCarriesTheTargetActionAndPayloadAndSurvivesTheRoundTrip() {
        var action = new CallThenReportAction();
        var first = ActionFlows.dispatch(action, new ActionExecutionRequest());

        assertEquals(ActionFlowStepType.CALL, first.getFlow().getType());
        assertEquals(Map.of("action", "lookup", "q", "abc"), first.getFlow().getData());
        assertNotNull(first.getFlow().getResumeToken());

        var resumeRequest = new ActionExecutionRequest(Map.of("status", "SUCCESS", "data", "nested-result"));
        resumeRequest.setResumeToken(first.getFlow().getResumeToken());
        ActionExecutionResponse second = ActionFlows.dispatch(action, resumeRequest);

        assertEquals("before-call:nested-result", second.getData());
    }

    @Test
    public void redirectStepCarriesUrlAndAwaitReturnFlag() {
        assertEquals(Map.of("url", "/books/1", "awaitReturn", false), ActionFlowStep.redirect("/books/1").getData());
        assertEquals(Map.of("url", "/x", "awaitReturn", true), ActionFlowStep.redirect("/x", true).getData());
    }

    @Test
    public void firstCallReturnsAPendingConfirmStepWithAResumeToken() {
        var action = new ConfirmThenEchoAction();
        var request = new ActionExecutionRequest(Map.of("value", "hello"));

        ActionExecutionResponse response = ActionFlows.dispatch(action, request);

        assertNotNull(response.getFlow());
        assertEquals(ActionFlowStepType.CONFIRM, response.getFlow().getType());
        assertNotNull(response.getFlow().getResumeToken());
        assertNotNull(response.getFlow().getFlowId());
        assertEquals("PENDING", response.getStatus());
    }

    @Test
    public void resumingWithTrueReturnsTheEchoedValueAsDone() {
        var action = new ConfirmThenEchoAction();
        var first = ActionFlows.dispatch(action, new ActionExecutionRequest(Map.of("value", "hello")));

        var resumeRequest = new ActionExecutionRequest(true);
        resumeRequest.setFlowId(first.getFlow().getFlowId());
        resumeRequest.setResumeToken(first.getFlow().getResumeToken());

        ActionExecutionResponse second = ActionFlows.dispatch(action, resumeRequest);

        assertEquals(ActionFlowStepType.DONE, second.getFlow().getType());
        assertEquals("hello", second.getData());
        assertEquals("SUCCESS", second.getStatus());
        assertEquals(first.getFlow().getFlowId(), second.getFlow().getFlowId());
    }

    @Test
    public void resumingWithFalseCancelsWithoutTheEchoedValue() {
        var action = new ConfirmThenEchoAction();
        var first = ActionFlows.dispatch(action, new ActionExecutionRequest(Map.of("value", "hello")));

        var resumeRequest = new ActionExecutionRequest(false);
        resumeRequest.setResumeToken(first.getFlow().getResumeToken());

        ActionExecutionResponse second = ActionFlows.dispatch(action, resumeRequest);

        assertEquals(ActionFlowStepType.DONE, second.getFlow().getType());
        assertNull(second.getData());
    }

    @Test
    public void aTamperedResumeTokenYieldsACleanErrorResponseInsteadOfThrowing() {
        var action = new ConfirmThenEchoAction();
        var first = ActionFlows.dispatch(action, new ActionExecutionRequest(Map.of("value", "hello")));

        var resumeRequest = new ActionExecutionRequest(true);
        resumeRequest.setResumeToken(first.getFlow().getResumeToken() + "tampered");

        ActionExecutionResponse response = ActionFlows.dispatch(action, resumeRequest);

        assertEquals("ERROR", response.getStatus());
        assertEquals(401, response.getStatusCode());
        assertNull(response.getFlow());
    }

    @Test
    public void aTokenIssuedForAnotherActionIsRejected() {
        var action = new ConfirmThenEchoAction();
        var other = new OtherAction();
        var first = ActionFlows.dispatch(action, new ActionExecutionRequest(Map.of("value", "hello")));

        var resumeRequest = new ActionExecutionRequest(true);
        resumeRequest.setResumeToken(first.getFlow().getResumeToken());

        ActionExecutionResponse response = ActionFlows.dispatch(other, resumeRequest);

        assertEquals("ERROR", response.getStatus());
        assertEquals(401, response.getStatusCode());
    }

    /** Reads the entity id off the seeded context — proves start() sees dataId even for a non-map request. */
    static class EchoDataIdAction extends AbstractRemoteAction implements FlowRemoteAction {
        @Override
        public ActionFlowStep start(ActionFlowContext ctx) {
            return ActionFlowStep.done(ctx.get("dataId", String.class));
        }

        @Override
        public ActionFlowStep resume(ActionFlowContext ctx, Object answer) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void seedsContextFromDataIdWhenRequestHasNoMapDataPayload() {
        var request = new ActionExecutionRequest();
        request.setDataId("42");

        ActionExecutionResponse response = ActionFlows.dispatch(new EchoDataIdAction(), request);

        assertEquals("42", response.getData());
    }
}
