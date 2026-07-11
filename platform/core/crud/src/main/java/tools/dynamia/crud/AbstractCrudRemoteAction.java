package tools.dynamia.crud;

import tools.dynamia.actions.AbstractAction;
import tools.dynamia.commons.ApplicableClass;

public abstract class AbstractCrudRemoteAction extends AbstractAction implements CrudRemoteAction {

    private ApplicableClass[] applicableClasses = ApplicableClass.ALL;
    private CrudState[] applicableStates = CrudState.get(CrudState.READ);

    @Override
    public CrudState[] getApplicableStates() {
        return applicableStates;
    }

    public void setApplicableStates(CrudState[] applicableStates) {
        this.applicableStates = applicableStates;
    }

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

}
