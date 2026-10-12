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
 * Where and how an action runs, as published to front ends in the action metadata.
 *
 * @see RunsOn
 * @see ActionRuntimes
 */
public enum ActionRuntime {

    /** A local action written against the UI facades; the server runs it, a REST client drives it by replay. */
    HEADLESS,

    /** A {@link FlowRemoteAction}: an explicit state machine on the server. */
    FLOW,

    /** A plain {@link RemoteAction}: one request, one answer. */
    REMOTE,

    /**
     * Behaviour specific to each front end: the search box, the filters panel, the export of what the grid shows, ZK
     * screens. It is in the action catalog (id, name, icon, restrictions) but has no endpoint and the server never runs
     * it. Each front end provides its own implementation under the same id (ZK in Java, Vue or POS as a TypeScript
     * {@code ClientAction}) or does not show the action.
     */
    FRONTEND,

    /** Nobody declared it and it cannot be derived: it is not published. */
    UNDECLARED
}
