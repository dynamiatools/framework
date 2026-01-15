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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.dynamia.modules.saas.ui.vm;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.services.AbstractService;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.domain.AccountProfile;
import tools.dynamia.modules.saas.domain.AccountProfileRestriction;
import tools.dynamia.modules.saas.domain.enums.AccessControl;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.*;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.crud.CrudView;
import tools.dynamia.zk.crud.FormCrudViewModel;
import tools.dynamia.zk.util.ZKBindingUtil;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.form.FormView;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mario Serrano Leones
 */

public class AccountProfileCrudVM extends AbstractService implements FormCrudViewModel<AccountProfile> {


    private final ModuleContainer moduleContainer = Containers.get().findObject(ModuleContainer.class);


    private final List<AccountProfileRestriction> toDelete = new ArrayList<>();
    private AccountProfile model;
    private Object selectedItem;
    private AccountProfileRestriction selectedRestriction;
    private DefaultTreeModel treeModel;

    private FormView<AccountProfile> formView;
    private CrudView<AccountProfile> crudView;
    private List<AccountProfileRestriction> restrictions;


    @Override
    public void initForm(CrudView<AccountProfile> crudView, FormView<AccountProfile> formView) {
        this.crudView = crudView;
        this.formView = formView;
        formView.addEventListener(FormView.ON_VALUE_CHANGED, e -> {
            this.model = (AccountProfile) e.getData();
            this.model.getRestrictions().size();
            sortRestrictions();
            update();
        });
    }

    public void update() {
        ZKBindingUtil.postNotifyChange(this);
    }

    @Init
    public void init() {
        initTreeModel();
    }

    @Command
    public void save() {
        UIMessages.showQuestion("Esta seguro que desea guardar cambios?", () -> {
            crudService().save(model);
            UIMessages.showMessage("AccountProfile guardado correctamente");
            crudView.setState(CrudState.READ);
        });
    }

    @Command
    public void close() {
        UIMessages.showQuestion("Esta seguro que desea cerrar esta ventana? Los cambios no guardados se perderan", () -> crudView.setState(CrudState.READ));
    }


    @Command
    @NotifyChange("*")
    public void addRestriction() {
        Object value = getSelectedItem();
        if (value instanceof DefaultTreeNode) {
            value = ((DefaultTreeNode) value).getData();
        }
        if (value == null) {
            UIMessages.showMessage("Select Restriction", MessageType.WARNING);
        } else if (value instanceof NavigationElement) {
            addRestriction((NavigationElement) value);
        }
        sortRestrictions();
    }

    private void sortRestrictions() {
        restrictions = model.getRestrictions().stream().sorted(Comparator.comparing(AccountProfileRestriction::getValue)).collect(Collectors.toList());
    }

    private void addRestriction(NavigationElement ele) {
        try {

            if (ele.getId() != null && ele.isVisible() && ele.isEnable()) {
                var restriction = new AccountProfileRestriction(ele instanceof Page ? ((Page) ele).getFullName() : ele.getName(), "ACCESS", ele.getVirtualPath());
                getModel().getRestrictions().add(restriction);
                restriction.setProfile(getModel());
            }

        } catch (ValidationError e) {
            ZKUtil.showMessage(e.getMessage(), MessageType.ERROR);
        }
    }

    @Command
    public void removeRestriction(@BindingParam("r") AccountProfileRestriction rst) {

        if (rst != null) {
            UIMessages.showQuestion("Esta seguro que desea eliminar el permiso " + rst.getName() + "?", () -> {
                getModel().getRestrictions().remove(rst);
                toDelete.add(rst);
                sortRestrictions();
                update();
            });

        } else {
            UIMessages.showMessage("Seleccione el permiso que desea borrar", MessageType.ERROR);
        }
    }


    private void initTreeModel() {
        DefaultTreeNode rootNode = new DefaultTreeNode(new Page(null, "restriction", "Rescritions"), new ArrayList());
        List<Module> modules = new ArrayList<>(moduleContainer.getModules());
        Collections.sort(modules);
        for (Module module : modules) {
            if (!module.isEmpty()) {
                DefaultTreeNode modNode = new DefaultTreeNode(module, new ArrayList());
                rootNode.add(modNode);
                renderPageGroupNode(Collections.singletonList(module.getDefaultPageGroup()), modNode);
                renderPageGroupNode(module.getPageGroups(), modNode);
            }
        }
        treeModel = new DefaultTreeModel(rootNode);
    }

    @SuppressWarnings("rawtypes")
    private void renderPageGroupNode(Collection<PageGroup> groups, DefaultTreeNode<NavigationElement> parentNode) {
        for (PageGroup pageGroup : groups) {
            DefaultTreeNode<NavigationElement> pageGroupNode = new DefaultTreeNode<>(pageGroup, new ArrayList<>());
            if (pageGroup.getParentModule() != null && pageGroup == pageGroup.getParentModule().getDefaultPageGroup()) {
                pageGroupNode = parentNode;
            } else {
                parentNode.add(pageGroupNode);
            }

            renderSubpageGroups(pageGroup, pageGroupNode);
            renderPages(pageGroup, pageGroupNode);
        }
    }

    private void renderPages(PageGroup pageGroup, DefaultTreeNode<NavigationElement> pageGroupNode) {
        for (Page pg : pageGroup.getPages()) {
            if (pg.isVisible()) {
                DefaultTreeNode<NavigationElement> pageNode = new DefaultTreeNode(pg, new ArrayList());
                pageGroupNode.add(pageNode);
            }
        }
    }

    private void renderSubpageGroups(PageGroup pageGroup, DefaultTreeNode<NavigationElement> pageGroupNode) {
        if (pageGroup.getPageGroups() != null && !pageGroup.getPageGroups().isEmpty()) {
            renderPageGroupNode(pageGroup.getPageGroups(), pageGroupNode);
        }
    }


    public AccountProfile getModel() {
        return model;
    }

    public void setModel(AccountProfile model) {
        this.model = model;
    }

    public Object getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(Object selectedItem) {
        this.selectedItem = selectedItem;
    }

    public void setSelectedRestriction(AccountProfileRestriction selectedRestriction) {
        this.selectedRestriction = selectedRestriction;
    }

    public AccountProfileRestriction getSelectedRestriction() {
        return selectedRestriction;
    }

    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    public List<AccountProfileRestriction> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(List<AccountProfileRestriction> restrictions) {
        this.restrictions = restrictions;
    }

    public List<AccessControl> getAccessControlValues() {
        return Stream.of(AccessControl.values()).collect(Collectors.toList());
    }
}
