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
package tools.dynamia.crud;

import tools.dynamia.actions.AbstractClassAction;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.commons.ApplicableClass;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;

/**
 * Helper class to create CrudActions. Use the class constructor to setup the action, like name,
 * applicable classes, applicable states, image, action group and position
 */
public abstract class AbstractCrudAction extends AbstractClassAction implements CrudAction {

    private boolean menuSupported;
    private ApplicableClass[] applicableClasses = ApplicableClass.ALL;
    private CrudState[] applicableStates = CrudState.get(CrudState.READ);
    private CrudService crudService;

    @Override
    public void actionPerformed(ActionEvent evt) {
        CrudActionEvent crudEvt;
        if (evt instanceof CrudActionEvent) {
            crudEvt = (CrudActionEvent) evt;
        } else {
            crudEvt = new CrudActionEvent(evt.getData(),
                    evt.getSource(),
                    (GenericCrudView) evt.getParam("crudview"),
                    (CrudControllerAPI) evt.getParam("controllers"));

            crudEvt.getParams().putAll(evt.getParams());
        }

        actionPerformed(crudEvt);

    }

    public abstract void actionPerformed(CrudActionEvent evt);

    /**
     * By default is false
     *
     * @return menuSupported
     */
    @Override
    public boolean isMenuSupported() {
        return menuSupported;
    }

    public void setMenuSupported(boolean menuSupported) {
        this.menuSupported = menuSupported;
    }

    /**
     * By default CrudState.READ is returned
     *
     * @return CrudState array
     */
    @Override
    public CrudState[] getApplicableStates() {
        return applicableStates;
    }

    public void setApplicableStates(CrudState[] applicableStates) {
        this.applicableStates = applicableStates;
    }

    /**
     * By default all classes are applicable
     *
     * @return ApplicableClass array
     */
    @Override
    public ApplicableClass[] getApplicableClasses() {
        return applicableClasses;
    }

    public void setApplicableClasses(ApplicableClass[] applicableClasses) {
        this.applicableClasses = applicableClasses;
    }

    public void setApplicableClass(Class clazz) {
        setApplicableClasses(ApplicableClass.get(clazz));
    }

    public Class getApplicableClass() {
        if (applicableClasses.length > 0) {
            return applicableClasses[0].getTargetClass();
        } else {
            return null;
        }
    }

    protected CrudService crudService() {
        if (crudService == null) {
            crudService = DomainUtils.lookupCrudService();
        }
        if (crudService == null) {
            throw new NullPointerException("Cannot lookup instance of " + CrudService.class);
        }
        return crudService;
    }
}


