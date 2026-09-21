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
package tools.dynamia.crud.actions.remote;

import tools.dynamia.actions.ActionFlowContext;
import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.actions.FlowRemoteAction;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudRemoteAction;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.ui.MessageType;

import java.io.Serializable;
import java.util.List;

/**
 * REST/Vue counterpart of ZK's {@code DeleteAction}: confirms with the client before deleting — the flow
 * equivalent of the design doc's {@code BulkDeleteAction} example, generalized to handle both a single
 * entity ({@code dataId}) and a batch ({@code data: {"ids": [...]}}, or a raw list body).
 * <p>
 * Registered under the conventional {@code "delete"} id so the existing {@code isDeleteCrudAction} matcher
 * on the Vue side picks it up for the default CRUD toolbar (see
 * {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} §7.5) — an entity with no bean of this type registered
 * behaves exactly as it does today (plain REST delete).
 *
 * @author Mario A. Serrano Leones
 */
@InstallAction
public class DeleteFlowRemoteAction extends AbstractCrudRemoteAction implements FlowRemoteAction {

    public DeleteFlowRemoteAction() {
        setId("delete");
        setName("Delete");
        setImage("delete");
    }

    @Override
    public CrudState[] getApplicableStates() {
        return CrudState.get(CrudState.READ);
    }

    @Override
    public ActionFlowStep start(ActionFlowContext ctx) {
        List<Serializable> ids = resolveIds(ctx);
        // Re-put under a stable "ids" key so resume() finds it the same way regardless of whether the
        // triggering request carried a single dataId or an {"ids": [...]}/list body.
        ctx.put("ids", ids);

        String message = ids.size() == 1
                ? "Delete this record?"
                : "Delete " + ids.size() + " records?";
        return ActionFlowStep.confirm(message, "Confirm");
    }

    @Override
    public ActionFlowStep resume(ActionFlowContext ctx, Object answer) {
        if (!Boolean.TRUE.equals(answer)) {
            return ActionFlowStep.done(null);
        }

        Class<?> entityClass = SaveSupport.resolveEntityClass(ctx.get("dataType", String.class));
        List<Serializable> ids = ctx.get("ids", List.class);
        var deleted = DeleteSupport.delete(DomainUtils.lookupCrudService(), entityClass, ids);
        return ActionFlowStep.done(deleted, "Deleted successfully", MessageType.INFO);
    }

    /**
     * A single {@code dataId} spreads into the context under its own key (seeded separately from
     * {@code data} — see {@code ActionFlows.seedFromRequest}); a {@code data: {"ids": [...]}} body spreads
     * its {@code ids} key flat into the context the same way; a raw list body lands under {@code "data"}.
     */
    private List<Serializable> resolveIds(ActionFlowContext ctx) {
        String dataId = ctx.get("dataId", String.class);
        if (dataId != null && !dataId.isBlank()) {
            return List.of(dataId);
        }
        List<Serializable> ids = ctx.get("ids", List.class);
        if (ids != null) {
            return ids;
        }
        ids = ctx.get("data", List.class);
        if (ids != null) {
            return ids;
        }
        throw new IllegalArgumentException("Delete action requires \"dataId\" or a list of ids");
    }
}
