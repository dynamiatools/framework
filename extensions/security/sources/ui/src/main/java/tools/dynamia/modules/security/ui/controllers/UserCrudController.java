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


package tools.dynamia.modules.security.ui.controllers;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Window;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.crud.CrudController;
import tools.dynamia.zk.util.ZKUtil;

/**
 * @author Mario Serrano Leones
 */


public class UserCrudController extends CrudController<User> {



    public void showSetProfiles() {
        if (getSelected() != null) {

            Window window = ZKUtil.showDialog(getPagePath("setProfiles"), "Set Profiles: " + getSelected().getUsername(), getSelected(), "800px", "500px");
            window.addEventListener(Events.ON_CLOSE, evt -> doQuery());

        } else {
            UIMessages.showMessage("Seleccione el usuario que desea asignar perfiles", MessageType.ERROR);
        }
    }


    private String getPagePath(String name) {
        return "classpath:/zk/security/users/" + name + ".zul";
    }


}
