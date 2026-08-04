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

/**
 * Thrown by {@link FlowTokenSigner#verify} when a {@code resumeToken} is malformed, tampered with,
 * expired, or does not belong to the action that received it.
 * <p>
 * Caught internally by {@code ActionFlows.dispatch} and turned into a clean
 * {@code ActionExecutionResponse} — never propagates to callers.
 *
 * @author Mario A. Serrano Leones
 */
public class FlowTokenException extends RuntimeException {

    public FlowTokenException(String message) {
        super(message);
    }
}
