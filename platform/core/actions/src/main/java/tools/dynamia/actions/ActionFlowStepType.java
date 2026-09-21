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

/**
 * The kind of interaction a {@link ActionFlowStep} asks the client to perform, as part of a
 * {@link FlowRemoteAction} flow.
 * <p>
 * See {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} in the repository for the full protocol design.
 *
 * @author Mario A. Serrano Leones
 */
public enum ActionFlowStepType {

    /** Yes/no question. The client answers with a {@code boolean}. */
    CONFIRM,

    /** Single value prompt. The client answers with a {@code String}/{@code Number}. */
    INPUT,

    /**
     * Renders {@link ActionFlowStep#getViewDescriptor()} as a form (e.g. {@code DynamiaForm} on the Vue
     * side) and collects the whole submitted form as a {@code Map<String, Object>} answer.
     */
    DIALOG,

    /**
     * Fire-and-forget notification (e.g. a toast). The client shows it and immediately continues the
     * flow without waiting for user input.
     */
    NOTIFY,

    /** The client navigates to a URL carried in {@link ActionFlowStep#getData()}. */
    REDIRECT,

    /** The client invokes another action first and feeds its response back as the answer. */
    CALL,

    /** Terminal step — identical semantics to a plain, non-flow {@link ActionExecutionResponse}. */
    DONE,

    /**
     * Escape hatch for anything not covered above: the client looks up a registered step renderer by a
     * component name carried in {@link ActionFlowStep#getData()}.
     */
    CUSTOM
}
