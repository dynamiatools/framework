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


package tools.dynamia.modules.security.ui.action;

import tools.dynamia.actions.ActionGroup;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.ui.controllers.UserCrudController;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;

/**
 * @author Mario Serrano Leones
 */
@InstallAction
public class SetUserProfilesAction extends AbstractCrudAction {

    public SetUserProfilesAction() {
        setName("Set Profiles");
        setImage("security");
        setGroup(ActionGroup.get("USERS"));
        setMenuSupported(true);
        setApplicableClass(User.class);
    }

    @Override
    public void actionPerformed(CrudActionEvent event) {
        if (event.getData() instanceof User user) {
            UserCrudController controler = (UserCrudController) event.getController();
            controler.setSelected(user);
            controler.showSetProfiles();
        } else {
            UIMessages.showLocalizedMessage("Select User", MessageType.ERROR);
        }
    }

}
