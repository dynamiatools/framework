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
 * One-shot REST/Vue counterpart of ZK's {@code DeleteAction}: deletes one entity ({@code dataId}) or a
 * batch ({@code data: {"ids": [...]}} / a raw list body) with no confirmation step.
 * <p>
 * Delete-without-confirm is the classic footgun — {@link DeleteFlowRemoteAction} (confirms first) is the
 * one meant to be wired into the default CRUD toolbar. This class stays available for programmatic or
 * already-confirmed contexts. See {@code docs/design/SERVER_DRIVEN_ACTION_FLOWS.md} §7.
 *
 * @author Mario A. Serrano Leones
 */
@InstallAction
public class DeleteRemoteAction extends AbstractCrudRemoteAction {

    public DeleteRemoteAction() {
        setId("deleteDirect");
        setName("Delete");
        setImage("delete");
    }

    @Override
    public CrudState[] getApplicableStates() {
        return CrudState.get(CrudState.READ);
    }

    @Override
    public ActionExecutionResponse execute(ActionExecutionRequest request) {
        Class<?> entityClass = SaveSupport.resolveEntityClass(request.getDataType());
        var ids = DeleteSupport.resolveIds(request.getDataId(), request.getData());
        var deleted = DeleteSupport.delete(DomainUtils.lookupCrudService(), entityClass, ids);
        return new ActionExecutionResponse(deleted);
    }
}
