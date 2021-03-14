/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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
package tools.dynamia.zk.reports.actions;

import tools.dynamia.actions.ActionGroup;
import tools.dynamia.actions.ReadableOnly;
import tools.dynamia.commons.ApplicableClass;
import tools.dynamia.commons.Messages;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.reports.ReportOutputType;
import tools.dynamia.reports.SimpleReportDescriptor;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.viewers.util.Viewers;

public abstract class AbstractExportAction extends AbstractCrudAction implements ReadableOnly {

    public AbstractExportAction() {
        setName(Messages.get(getClass(), "export"));
        setImage("export-" + getOuputType().getExtension());
        setGroup(ActionGroup.get("EXPORT"));
    }

 
    private boolean isBaseClass(Class type) {
        String name = type.getName();
        return name.startsWith("java.lang") || name.startsWith("java.math") || name.startsWith("java.sql") || name.startsWith("java.util");
    }

    @Override
    public CrudState[] getApplicableStates() {
        return CrudState.get(CrudState.READ);
    }

    @Override
    public ApplicableClass[] getApplicableClasses() {
        return ApplicableClass.ALL;
    }

    public abstract ReportOutputType getOuputType();

    protected void customizeReportDescriptor(SimpleReportDescriptor descriptor) {

    }

    protected ViewDescriptor getViewDescriptor(CrudActionEvent evt) {

        Class entityClass = evt.getController().getEntityClass();
        ViewDescriptor viewDescriptor = Viewers.findViewDescriptor(entityClass, "export");
        if (viewDescriptor == null) {
            viewDescriptor = Viewers.findViewDescriptor(entityClass, "tree");
        }

        if (viewDescriptor == null) {
            viewDescriptor = Viewers.getViewDescriptor(entityClass, "table");
        }

        return viewDescriptor;

    }

}
