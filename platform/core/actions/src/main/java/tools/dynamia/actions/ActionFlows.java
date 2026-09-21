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

import tools.dynamia.actions.flow.FlowTokenException;
import tools.dynamia.actions.flow.FlowTokenPayload;
import tools.dynamia.actions.flow.FlowTokenSigner;
import tools.dynamia.integration.Containers;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Dispatches {@link FlowRemoteAction} execution: decides whether an incoming
 * {@link ActionExecutionRequest} starts a new flow or resumes one from a verified {@code resumeToken},
 * calls the action, and stamps the returned {@link ActionFlowStep} with a fresh signed token before
 * wrapping it into an {@link ActionExecutionResponse}.
 * <p>
 * This is the only place a {@code resumeToken} is created or verified — action authors never touch
 * signing directly. See {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} for the full protocol.
 *
 * @author Mario A. Serrano Leones
 */
public final class ActionFlows {

    private static final FlowTokenSigner DEFAULT_SIGNER = new FlowTokenSigner(null);

    private ActionFlows() {
    }

    /**
     * Executes one step of {@code action}'s flow for {@code request}: {@link FlowRemoteAction#start} when
     * {@code request} carries no {@code resumeToken}, {@link FlowRemoteAction#resume} otherwise.
     * <p>
     * A tampered, expired, or mismatched {@code resumeToken} never reaches the action — it's turned into a
     * plain {@code ERROR} response (HTTP 401 semantics via {@link ActionExecutionResponse#getStatusCode()})
     * instead of propagating as an exception, keeping the existing controller dispatch untouched.
     */
    public static ActionExecutionResponse dispatch(FlowRemoteAction action, ActionExecutionRequest request) {
        try {
            boolean resuming = request.getResumeToken() != null && !request.getResumeToken().isBlank();
            ActionFlowContext ctx;
            String flowId;
            ActionFlowStep step;

            if (!resuming) {
                flowId = request.getFlowId() != null ? request.getFlowId() : UUID.randomUUID().toString();
                ctx = new ActionFlowContext(flowId, action.getId(), null, seedFromRequest(request));
                step = action.start(ctx);
            } else {
                FlowTokenPayload payload = signer().verify(request.getResumeToken(), action.getId());
                flowId = payload.flowId();
                ctx = new ActionFlowContext(flowId, action.getId(), payload.step(), new HashMap<>(payload.data()));
                step = action.resume(ctx, request.getData());
            }

            return toResponse(flowId, ctx, step);
        } catch (FlowTokenException e) {
            return new ActionExecutionResponse(e.getMessage(), "ERROR", 401);
        }
    }

    /**
     * Seeds a fresh flow's context data from the triggering request, so {@link FlowRemoteAction#start} can
     * read the initial payload uniformly via {@link ActionFlowContext#get} regardless of whether the
     * caller sent a map body (e.g. {@code {"ids": [1,2,3]}}) or a single entity reference
     * ({@code dataId}/{@code dataType}/{@code dataName}).
     */
    private static Map<String, Object> seedFromRequest(ActionExecutionRequest request) {
        Map<String, Object> seed = new HashMap<>();
        if (request.getData() instanceof Map<?, ?> map) {
            map.forEach((key, value) -> seed.put(String.valueOf(key), value));
        } else if (request.getData() != null) {
            seed.put("data", request.getData());
        }
        if (request.getDataId() != null) {
            seed.putIfAbsent("dataId", request.getDataId());
        }
        if (request.getDataType() != null) {
            seed.putIfAbsent("dataType", request.getDataType());
        }
        if (request.getDataName() != null) {
            seed.putIfAbsent("dataName", request.getDataName());
        }
        if (request.getParams() != null) {
            request.getParams().forEach(seed::putIfAbsent);
        }
        return seed;
    }

    private static ActionExecutionResponse toResponse(String flowId, ActionFlowContext ctx, ActionFlowStep step) {
        step.setFlowId(flowId);

        if (step.getType() != ActionFlowStepType.DONE) {
            long expiresAt = Instant.now().plus(signer().ttl()).toEpochMilli();
            var payload = new FlowTokenPayload(flowId, ctx.actionId(), step.getType().name(), ctx.data(), expiresAt);
            step.setResumeToken(signer().sign(payload));
        }

        var response = new ActionExecutionResponse();
        response.setFlow(step);
        response.setData(step.getData());
        response.setStatus(step.getType() == ActionFlowStepType.DONE ? "SUCCESS" : "PENDING");
        response.setStatusCode(200);
        return response;
    }

    private static FlowTokenSigner signer() {
        FlowTokenSigner signer = Containers.get().findObject(FlowTokenSigner.class);
        return signer != null ? signer : DEFAULT_SIGNER;
    }
}
