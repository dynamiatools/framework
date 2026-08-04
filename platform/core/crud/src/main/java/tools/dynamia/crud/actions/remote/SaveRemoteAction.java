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

import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudRemoteAction;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.util.DomainUtils;

/**
 * One-shot REST/Vue counterpart of ZK's {@code SaveAction}: creates or updates an entity (create when the
 * request payload has no {@code id}, update otherwise) with no confirmation step.
 * <p>
 * For entities that need "are you sure you want to save this?", use {@link SaveFlowRemoteAction} instead —
 * this class is for programmatic or already-confirmed contexts. See
 * {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} §7.
 * <p>
 * Not registered under the conventional {@code "save"} id — {@link SaveFlowRemoteAction} is the one meant
 * to be picked up by the default CRUD toolbar (see {@code isSaveCrudAction} on the Vue side).
 *
 * @author Mario A. Serrano Leones
 */
@InstallAction
public class SaveRemoteAction extends AbstractCrudRemoteAction {

    public SaveRemoteAction() {
        setId("saveDirect");
        setName("Save");
        setImage("save");
    }

    @Override
    public CrudState[] getApplicableStates() {
        return CrudState.get(CrudState.CREATE, CrudState.UPDATE);
    }

    @Override
    public ActionExecutionResponse execute(ActionExecutionRequest request) {
        Class<?> entityClass = SaveSupport.resolveEntityClass(request.getDataType());
        Object saved = SaveSupport.persist(DomainUtils.lookupCrudService(), entityClass, request.getData(), getApplicableStates());
        return new ActionExecutionResponse(saved);
    }
}
