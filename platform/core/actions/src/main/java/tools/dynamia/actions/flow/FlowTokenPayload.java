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
package tools.dynamia.actions.flow;

import java.util.HashMap;
import java.util.Map;

/**
 * Decoded, verified contents of a {@code resumeToken} — the entire server-side state of one in-flight
 * flow, round-tripped through the client on every request/response (see
 * {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} §4). Never held in memory across requests.
 *
 * @author Mario A. Serrano Leones
 */
public final class FlowTokenPayload {

    private final String flowId;
    private final String actionId;
    private final String step;
    private final Map<String, Object> data;
    private final long expiresAt;

    public FlowTokenPayload(String flowId, String actionId, String step, Map<String, Object> data, long expiresAt) {
        this.flowId = flowId;
        this.actionId = actionId;
        this.step = step;
        this.data = data != null ? data : new HashMap<>();
        this.expiresAt = expiresAt;
    }

    public String flowId() {
        return flowId;
    }

    public String actionId() {
        return actionId;
    }

    public String step() {
        return step;
    }

    public Map<String, Object> data() {
        return data;
    }

    public long expiresAt() {
        return expiresAt;
    }

    /** Serializes this payload to a plain map, ready for {@code StringPojoParser.convertMapToJson}. */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("flowId", flowId);
        map.put("actionId", actionId);
        map.put("step", step);
        map.put("data", data);
        map.put("exp", expiresAt);
        return map;
    }

    /** Reconstructs a payload from a map previously produced by {@link #toMap()}. */
    @SuppressWarnings("unchecked")
    public static FlowTokenPayload fromMap(Map<String, Object> map) {
        Object exp = map.get("exp");
        long expiresAt = exp instanceof Number number ? number.longValue() : 0L;
        Object data = map.get("data");
        return new FlowTokenPayload(
                (String) map.get("flowId"),
                (String) map.get("actionId"),
                (String) map.get("step"),
                data instanceof Map ? (Map<String, Object>) data : new HashMap<>(),
                expiresAt
        );
    }
}
