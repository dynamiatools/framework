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

import java.util.Map;

/**
 * REST/Vue counterpart of ZK's {@code SaveAction}, giving entities that need
 * {@code isConfirmBeforeSave()}-style behavior — a ZK-only concept today ({@code CrudControllerAPI}) — a
 * real server-side equivalent: confirms with the client before persisting, instead of a client-side
 * {@code confirm()} that skips server-side re-validation.
 * <p>
 * Registered under the conventional {@code "save"} id so the existing {@code isSaveCrudAction} matcher on
 * the Vue side picks it up for the default CRUD toolbar (see
 * {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} §7.5) — an entity with no bean of this type registered
 * behaves exactly as it does today (plain REST save).
 * <p>
 * Shares persistence logic with {@link SaveRemoteAction} via the {@link SaveSupport} helper rather than
 * extending it — see §7.4 for why {@code extends} is a footgun for a {@link FlowRemoteAction}.
 *
 * @author Mario A. Serrano Leones
 */
@InstallAction
public class SaveFlowRemoteAction extends AbstractCrudRemoteAction implements FlowRemoteAction {

    /** Set to {@code true} (e.g. per-instance configuration/subclassing) to confirm before every save. */
    private boolean confirmBeforeSave = false;

    public SaveFlowRemoteAction() {
        setId("save");
        setName("Save");
        setImage("save");
    }

    @Override
    public CrudState[] getApplicableStates() {
        return CrudState.get(CrudState.CREATE, CrudState.UPDATE);
    }

    public boolean isConfirmBeforeSave() {
        return confirmBeforeSave;
    }

    public void setConfirmBeforeSave(boolean confirmBeforeSave) {
        this.confirmBeforeSave = confirmBeforeSave;
    }

    @Override
    public ActionFlowStep start(ActionFlowContext ctx) {
        if (confirmBeforeSave) {
            return ActionFlowStep.confirm("Save changes?", "Confirm");
        }
        return doSave(ctx);
    }

    @Override
    public ActionFlowStep resume(ActionFlowContext ctx, Object answer) {
        if (!Boolean.TRUE.equals(answer)) {
            return ActionFlowStep.done(null);
        }
        return doSave(ctx);
    }

    /**
     * The triggering request's whole entity payload (its {@code data} map) is spread flat into the flow
     * context by {@link tools.dynamia.actions.ActionFlows#dispatch} — {@link ActionFlowContext#asMap()}
     * recovers it as a single map, ready for {@link SaveSupport#persist}.
     */
    private ActionFlowStep doSave(ActionFlowContext ctx) {
        Class<?> entityClass = SaveSupport.resolveEntityClass(ctx.get("dataType", String.class));
        Map<String, Object> data = ctx.asMap();
        Object saved = SaveSupport.persist(DomainUtils.lookupCrudService(), entityClass, data, getApplicableStates());
        return ActionFlowStep.done(saved, "Saved successfully", MessageType.INFO);
    }
}
