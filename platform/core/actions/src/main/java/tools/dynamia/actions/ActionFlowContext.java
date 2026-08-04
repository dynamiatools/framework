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

import tools.dynamia.commons.StringPojoParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Accumulated state for one in-flight {@link FlowRemoteAction} flow, reconstructed by {@link ActionFlows}
 * from the signed {@code resumeToken} on every call after the first.
 * <p>
 * Nothing here is kept server-side between requests — see {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md}
 * §4: everything an action {@link #put}s is serialized into the next signed token and only exists in
 * memory for the duration of a single {@link FlowRemoteAction#start}/{@link FlowRemoteAction#resume} call.
 *
 * @author Mario A. Serrano Leones
 */
public final class ActionFlowContext {

    private final String flowId;
    private final String actionId;
    private final String currentStep;
    private final Map<String, Object> data;

    ActionFlowContext(String flowId, String actionId, String currentStep, Map<String, Object> data) {
        this.flowId = flowId;
        this.actionId = actionId;
        this.currentStep = currentStep;
        this.data = data != null ? data : new HashMap<>();
    }

    /** Correlation id for this flow instance, stable across every step. */
    public String flowId() {
        return flowId;
    }

    /**
     * The {@link ActionFlowStepType} name of the step this context is resuming from, or {@code null} on
     * the first call ({@link FlowRemoteAction#start}). Useful when {@link FlowRemoteAction#resume} needs
     * to tell apart which of several possible prior steps it's answering.
     */
    public String currentStep() {
        return currentStep;
    }

    /**
     * Typed lookup into the accumulated flow data (seeded, on the first call, from the triggering
     * {@link ActionExecutionRequest}'s {@code data}/{@code dataId}/{@code dataType}/{@code dataName}/
     * {@code params}; afterwards, from whatever previous steps {@link #put} there).
     * <p>
     * Values that already are (or were deserialized from JSON as) an instance of {@code type} — including
     * {@code String}, {@code Number}, {@code Boolean}, {@code List}, {@code Map} — are returned directly.
     * A {@code Map}-shaped value requested as some other POJO type is converted via
     * {@link StringPojoParser#parseJsonToPojo(Map, Class)}.
     *
     * @return the value as {@code type}, or {@code null} if absent
     * @throws IllegalArgumentException if the stored value can't be converted to {@code type}
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object raw = data.get(key);
        if (raw == null) {
            return null;
        }
        if (type.isInstance(raw)) {
            return type.cast(raw);
        }
        if (raw instanceof Map) {
            return StringPojoParser.parseJsonToPojo((Map) raw, type);
        }
        throw new IllegalArgumentException("Flow context value for '" + key + "' (" + raw.getClass()
                + ") cannot be converted to " + type);
    }

    /** Accumulates {@code value} under {@code key} into the next signed {@code resumeToken}. */
    public void put(String key, Object value) {
        data.put(key, value);
    }

    /** @internal used by {@link ActionFlows} to build the next signed token. */
    Map<String, Object> data() {
        return data;
    }

    /** @internal used by {@link ActionFlows} to bind a resumed token to the action that issued it. */
    String actionId() {
        return actionId;
    }
}
