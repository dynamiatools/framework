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
 * A {@link RemoteAction} that, instead of always returning a final result in one HTTP round trip, can
 * ask the client to do something first (show a confirm dialog, collect input, render a form...) and be
 * called again with the answer — as many times as needed until it returns a
 * {@link ActionFlowStepType#DONE} step.
 * <p>
 * This is purely additive to {@link RemoteAction}: {@link #execute(ActionExecutionRequest)} has a default
 * implementation that bridges into {@link ActionFlows}, so existing dispatch code
 * ({@code Actions.execute}, {@code ApplicationMetadataController}) needs no changes at all.
 * <p>
 * The flow is entirely stateless server-side: everything an implementation accumulates via
 * {@link ActionFlowContext#put} travels to the client inside a signed {@code resumeToken} and comes back
 * verbatim on the next call — see {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} §4 for why (no Redis,
 * no server-side flow store, by design).
 * <p>
 * Example — confirm before deleting:
 * <pre>{@code
 * @InstallAction
 * public class DeleteBookWithConfirmAction extends AbstractCrudRemoteAction implements FlowRemoteAction {
 *
 *     public DeleteBookWithConfirmAction() {
 *         setApplicableClass(Book.class);
 *     }
 *
 *     @Override
 *     public ActionFlowStep start(ActionFlowContext ctx) {
 *         String id = ctx.get("dataId", String.class);
 *         ctx.put("bookId", id);
 *         return ActionFlowStep.confirm("Delete this book?", "Confirm");
 *     }
 *
 *     @Override
 *     public ActionFlowStep resume(ActionFlowContext ctx, Object answer) {
 *         if (Boolean.TRUE.equals(answer)) {
 *             crudService.delete(Book.class, ctx.get("bookId", String.class));
 *             return ActionFlowStep.done(null, "Deleted.", MessageType.INFO);
 *         }
 *         return ActionFlowStep.done(null);
 *     }
 * }
 * }</pre>
 *
 * @apiNote <b>Experimental.</b> The flow protocol (this interface, {@link ActionFlowStep}, {@link ActionFlows},
 * the {@code resumeToken} wire format) may still change without a deprecation cycle.
 * @author Mario A. Serrano Leones
 */
public interface FlowRemoteAction extends RemoteAction {

    /**
     * Bridges the plain {@link RemoteAction} contract into the flow engine. This is the only method
     * {@link RemoteAction} actually requires — implementations should not override it; implement
     * {@link #start} and {@link #resume} instead.
     */
    @Override
    default ActionExecutionResponse execute(ActionExecutionRequest request) {
        return ActionFlows.dispatch(this, request);
    }

    /**
     * Called once, on the first request of a new flow (no {@code resumeToken} present yet).
     *
     * @param ctx context seeded from the triggering {@link ActionExecutionRequest} — use {@link ActionFlowContext#put}
     *            to carry anything {@link #resume} will need
     * @return the next step to show the client, or a {@link ActionFlowStepType#DONE} step if no
     * interaction is actually needed
     */
    ActionFlowStep start(ActionFlowContext ctx);

    /**
     * Called on every subsequent request, once {@link ActionFlows} has verified the {@code resumeToken}.
     *
     * @param ctx    context reconstructed from the verified token, carrying everything previously {@code put}
     * @param answer the client's answer to the last step returned ({@code request.getData()})
     * @return the next step, or a {@link ActionFlowStepType#DONE} step to end the flow
     */
    ActionFlowStep resume(ActionFlowContext ctx, Object answer);
}
