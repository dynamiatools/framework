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


package tools.dynamia.modules.security.ui;

import org.springframework.stereotype.Component;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.saas.api.dto.AccountDTO;
import tools.dynamia.modules.security.CurrentUser;
import tools.dynamia.navigation.NavigationElement;

import java.io.Serializable;


/**
 * User restrition, default order: 100
 */
@Component
public class UserNavigationRestriction implements tools.dynamia.navigation.NavigationRestriction, Serializable {


    private final AccountServiceAPI accountServiceAPI;
    private final CurrentUser usuarioActual;

    public UserNavigationRestriction(AccountServiceAPI accountServiceAPI, CurrentUser usuarioActual) {
        this.accountServiceAPI = accountServiceAPI;
        this.usuarioActual = usuarioActual;
    }


    @Override
    public Boolean allowAccess(NavigationElement element) {
        String username = usuarioActual.getUsername();
        AccountDTO accountDTO = accountServiceAPI.getCurrentAccount();

        if (username == null || accountDTO == null) {
            return null;
        }

        if (element.getVirtualPath().startsWith("saas") && !accountDTO.getTypeName().equals("admin")) {
            return false;
        }

        if (accountDTO.getMaxUsers() == 1 && (element.getVirtualPath().equals("system/seguridad/user") || element.getVirtualPath().equals("system/seguridad/perfiles"))) {
            return false;
        }

        UserInterfaceController uiController = Containers.get().findObject(UserInterfaceController.class);
        if (uiController != null) {
            return uiController.hasAccess(element.getVirtualPath());
        } else {
            return null;
        }
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
