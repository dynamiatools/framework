package tools.dynamia.crud.actions;

import tools.dynamia.actions.Action;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionFilter;
import tools.dynamia.commons.BeanMap;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.sterotypes.Listener;

import java.io.Serializable;

@Listener
public class AutoLoadBeanMapEntityActionFilter implements ActionFilter {


    @Override
    public void beforeActionPerformed(Action action, ActionEvent evt) {
        if (evt.getData() instanceof BeanMap beanMap && beanMap.isLoadable()) {
            var entity = DomainUtils.lookupCrudService().find(beanMap.getBeanClass(), (Serializable) beanMap.getId());
            evt.setData(entity);
        }
    }
}
