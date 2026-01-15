/*
 * Copyright (c) 2009 - 2021 Dynamia Soluciones IT SAS  All Rights Reserved
 *
 * Todos los Derechos Reservados  2009 - 2021
 *
 * Este archivo es propiedad de Dynamia Soluciones IT NIT 900302344-1 en Colombia / Sur America,
 * esta estrictamente prohibida su copia o distribución sin previa autorización del propietario.
 * Puede contactarnos a info@dynamiasoluciones.com o visitar nuestro sitio web
 * https://www.dynamiasoluciones.com
 *
 * Autor: Ing. Mario Serrano Leones <mario@dynamiasoluciones.com>
 */


package tools.dynamia.modules.security.ui.vm;

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
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.security.domain.Profile;
import tools.dynamia.modules.security.domain.Permission;
import tools.dynamia.modules.security.services.ProfileService;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleContainer;
import tools.dynamia.navigation.NavigationElement;
import tools.dynamia.navigation.NavigationRestrictions;
import tools.dynamia.navigation.Page;
import tools.dynamia.navigation.PageAction;
import tools.dynamia.navigation.PageGroup;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.crud.CrudView;
import tools.dynamia.zk.crud.FormCrudViewModel;
import tools.dynamia.zk.util.ZKBindingUtil;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.form.FormView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mario Serrano Leones
 */

public class ProfileCrudVM extends AbstractService implements FormCrudViewModel<Profile> {


    private final ProfileService service = Containers.get().findObject(ProfileService.class);
    private final AccountServiceAPI accountService = Containers.get().findObject(AccountServiceAPI.class);
    private final ModuleContainer moduleContainer = Containers.get().findObject(ModuleContainer.class);


    private final List<Permission> toDelete = new ArrayList<>();
    private Profile model;
    private Object selectedItem;
    private Permission selectedPermission;
    private DefaultTreeModel treeModel;

    private CrudView<Profile> crudView;
    private List<Permission> permissions;


    @Override
    public void initForm(CrudView<Profile> crudView, FormView<Profile> formView) {
        this.crudView = crudView;
        formView.addEventListener(FormView.ON_VALUE_CHANGED, e -> {
            this.model = (Profile) e.getData();
            this.model.getPermissions().size();
            sortPermisos();
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

            getModel().getPermissions()
                    .stream()
                    .filter(p -> p.getAccountId() == null)
                    .forEach(p -> p.setAccountId(accountService.getCurrentAccountId()));
            crudService().save(model);

            UIMessages.showMessage("Perfil guardado correctamente");
            crudView.setState(CrudState.READ);
        });
    }

    @Command
    public void close() {
        UIMessages.showQuestion("Esta seguro que desea cerrar esta ventana? Los cambios no guardados se perderan", () -> crudView.setState(CrudState.READ));
    }


    @Command
    @NotifyChange("*")
    public void addAccessPermission() {
        Object value = getSelectedItem();
        if (value instanceof DefaultTreeNode) {
            value = ((DefaultTreeNode) value).getData();
        }
        if (value == null) {
            UIMessages.showMessage("Seleccione el permiso que desea agregar");
        } else if (value instanceof Page) {
            addPermisoPagina((Page) value);
        } else if (value instanceof PageGroup pg) {
            for (Page p : pg.getPages()) {
                addPermisoPagina(p);
            }
        } else if (value instanceof Module) {
            for (PageGroup pg : ((Module) value).getPageGroups()) {
                for (Page p : pg.getPages()) {
                    addPermisoPagina(p);
                }
            }

        } else if (value instanceof PageAction) {
            ddActionPermission((PageAction) value);
        }
        sortPermisos();
    }

    private void sortPermisos() {
        permissions = model.getPermissions().stream().sorted(Comparator.comparing(Permission::getValue)).collect(Collectors.toList());
    }

    private void addPermisoPagina(Page pg) {
        try {

            if (pg.getId() != null && pg.isVisible() && pg.isEnable()) {
                Permission permiso = new Permission(ProfileService.ACCESS_PERMISSION, pg.getVirtualPath(), pg.getFullName());
                getModel().addPermission(permiso);
            }

        } catch (ValidationError e) {
            ZKUtil.showMessage(e.getMessage(), MessageType.ERROR);
        }
    }

    private void ddActionPermission(PageAction action) {
        Page page = action.getPage();
        Permission permiso = null;
        if (action.getId() != null) {
            permiso = new Permission(ProfileService.ACTION_PERMISSION, page.getVirtualPath() + ":" + action.getId(), page.getName() + " - "
                    + action.getName());
        } else {
            permiso = new Permission(ProfileService.ALL_ACTIONS_PERMISSION, page.getVirtualPath() + ":Todas", "Todas las acciones");
        }
        permiso.setLevel(2);
        getModel().addPermission(permiso);
    }



    @Command
    public void removeAccessPermission(@BindingParam("permission") Permission p) {

        if (p != null) {
            UIMessages.showQuestion("Esta seguro que desea eliminar el permiso " + p.getDescription() + "?", () -> {
                getModel().removePermission(p);
                toDelete.add(p);
                sortPermisos();
                update();
            });

        } else {
            UIMessages.showMessage("Seleccione el permiso que desea borrar", MessageType.ERROR);
        }
    }


    private void initTreeModel() {
        DefaultTreeNode rootNode = new DefaultTreeNode(new Page(null, "permisos", "Permisos"), new ArrayList());
        List<Module> modules = new ArrayList<>(moduleContainer.getModules());
        Collections.sort(modules);
        for (Module module : modules) {
            if (NavigationRestrictions.allowAccess(module) && !module.isEmpty()) {
                DefaultTreeNode modNode = new DefaultTreeNode(module, new ArrayList());
                rootNode.add(modNode);
                renderPageGroupNode(Collections.singletonList(module.getDefaultPageGroup()), modNode);
                renderPageGroupNode(module.getPageGroups(), modNode);
            }
        }

        treeModel = new DefaultTreeModel(rootNode);
    }

    @SuppressWarnings("rawtypes")
    private void renderPageGroupNode(List<PageGroup> groups, DefaultTreeNode<NavigationElement> parentNode) {
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

                if (pg.getActions() != null && !pg.getActions().isEmpty()) {
                    for (PageAction pga : pg.getActions()) {
                        DefaultTreeNode actionNode = new DefaultTreeNode(pga);
                        pageNode.add(actionNode);
                    }
                }
            }
        }
    }

    private void renderSubpageGroups(PageGroup pageGroup, DefaultTreeNode<NavigationElement> pageGroupNode) {
        if (pageGroup.getPageGroups() != null && !pageGroup.getPageGroups().isEmpty()) {
            renderPageGroupNode(pageGroup.getPageGroups(), pageGroupNode);
        }
    }




    public Profile getModel() {
        return model;
    }

    public void setModel(Profile model) {
        this.model = model;
    }

    public Object getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(Object selectedItem) {
        this.selectedItem = selectedItem;
    }


    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    public Permission getSelectedPermission() {
        return selectedPermission;
    }

    public void setSelectedPermission(Permission selectedPermission) {
        this.selectedPermission = selectedPermission;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }
}
