
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

package tools.dynamia.modules.saas.ui.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.saas.domain.AccountProfile;
import tools.dynamia.modules.saas.domain.AccountProfileRestriction;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleContainer;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.crud.CrudController;
import tools.dynamia.zk.util.ZKUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mario Serrano Leones
 */
@Component("accountProfile")
@Scope("prototype")
public class AccountProfileController extends CrudController<AccountProfile> {

    @Autowired
    private ModuleContainer moduleContainer;
    @Wire
    private Listbox modules;
    @Wire
    private Listbox restrictions;
    private final List<AccountProfileRestriction> toDelete = new ArrayList<>();

    // autowired by zk composer
    @Wire
    private Textbox name;
    @Wire
    private Textbox description;

    @Override
    protected void afterPageLoaded() {
        buildModel();

        if (getEntity() != null) {
            name.setValue(getEntity().getName());
            description.setValue(getEntity().getDescription());
			updateListboxRestrictions();
        }
    }


    @Override
    protected void afterEdit() {
        Window window = ZKUtil.showDialog(getPagePath("new"), "Edit Profile", getSelected(), "90%", "90%");
        window.addEventListener(Events.ON_CLOSE, evt -> doQuery());


    }

    @Override
    protected void afterDelete() {
        query();
    }

    @Override
    protected void beforeSave() {
        getEntity().setName(name.getValue());
        getEntity().setDescription(description.getValue());
    }

    public void nuevo() {
        ZKUtil.showDialog(getPagePath("new"), "New Profile", null, "90%", "90%");
        query();
    }

    @Listen("onClick = #add")
    public void addAccessRestriction() {
        Module value = getSelectedModule();
        if (value == null) {
            UIMessages.showMessage("Select a module");
        } else {
            addPageRestriction(value);
        }

        updateListboxRestrictions();
    }

    private void addPageRestriction(Module mod) {
        try {

            if (mod.isVisible() && mod.isEnable()) {
                for (AccountProfileRestriction restriction : getEntity().getRestrictions()) {
                    if (restriction.getValue().equals(mod.getId())) {
                        UIMessages.showMessage("Restriccion existente", MessageType.WARNING);
                        return;
                    }
                }

                AccountProfileRestriction restriction = new AccountProfileRestriction(mod.getName(), "ACCESS", mod.getId());
                getEntity().getRestrictions().add(restriction);
                restriction.setProfile(getEntity());
            }

        } catch (ValidationError e) {
            ZKUtil.showMessage(e.getMessage(), MessageType.ERROR);
        }
    }

    @Listen("onClick = #remove")
    public void removeAccessRestriction() {
        final AccountProfileRestriction p = getSelectedPermiso();
        if (p != null) {
            UIMessages.showQuestion("Esta seguro que desea eliminar el acceso seleccionado?", () -> {
                getEntity().getRestrictions().remove(p);
                p.setProfile(null);
                toDelete.add(p);
                updateListboxRestrictions();
            });

        } else {
            ZKUtil.showMessage("Seleccione el permiso que desea borrar", MessageType.ERROR);
        }
    }


    private void updateListboxRestrictions() {
        ZKUtil.fillListbox(restrictions, getEntity().getRestrictions(), true);
    }

    private Module getSelectedModule() {
        try {

            return modules.getSelectedItem().getValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private AccountProfileRestriction getSelectedPermiso() {
        if (restrictions != null) {
            return restrictions.getSelectedItem().getValue();
        } else {
            return null;
        }
    }

    private void buildModel() {
        if (modules != null) {
            List<Module> model = new ArrayList<>(moduleContainer.getModules());
            Collections.sort(model);
            ZKUtil.fillListbox(modules, model, true);
        }
    }

    private String getPagePath(String name) {
        return "classpath:/zk/saas/profiles/" + name + ".zul";
    }
}
