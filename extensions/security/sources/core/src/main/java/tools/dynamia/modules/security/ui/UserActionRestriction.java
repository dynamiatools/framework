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

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import tools.dynamia.actions.Action;
import tools.dynamia.actions.ReadableOnly;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.security.CurrentUser;
import tools.dynamia.modules.security.domain.Permission;
import tools.dynamia.modules.security.services.ProfileService;
import tools.dynamia.navigation.NavigationManager;
import tools.dynamia.navigation.Page;

import java.io.Serializable;
import java.util.List;

/**
 * @author Mario Serrano Leones
 */
@Component
@Scope("zk-desktop")
public class UserActionRestriction implements tools.dynamia.actions.ActionRestriction, Serializable {

    private final LoggingService logger = new SLF4JLoggingService(UserActionRestriction.class);
    private List<Permission> permisosAccion;
    private List<Permission> permisosTodasAcciones;
    private boolean admin;
    private static final List<String> DEFAULT_ACTIONS = List.of("SaveAction", "SaveAndEditAction", "SaveAndNewAction", "CancelAction", "FindAction");


    @PostConstruct
    private void init() {
        try {
            var service = Containers.get().findObject(ProfileService.class);
            var user = CurrentUser.get().getUser();
            admin = CurrentUser.get().isAdmin();
            permisosAccion = service.getPermissions(user.getAccountId(), user.getUsername(), ProfileService.ACTION_PERMISSION);
            permisosTodasAcciones = service.getPermissions(user.getAccountId(), user.getUsername(), ProfileService.ALL_ACTIONS_PERMISSION);

        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public Boolean actionAllowed(Action action) {
        if (admin) {
            return true;
        }

        if (DEFAULT_ACTIONS.contains(action.getId()) || action instanceof ReadableOnly) {
            return true;
        }

        Page currentPage = NavigationManager.getCurrent().getCurrentPage();
        if (currentPage != null) {
            return currentPage.isAlwaysAllowed() || isTodasAcciones(currentPage) || tienePermiso(currentPage, action);
        }

        return false;
    }

    private boolean isTodasAcciones(Page currentPage) {
        if (permisosTodasAcciones != null) {
            for (Permission permiso : permisosTodasAcciones) {
                if (permiso.getValue().equals(currentPage.getVirtualPath() + ":All")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tienePermiso(Page currentPage, Action action) {
        String valor = currentPage.getVirtualPath() + ":" + action.getId();
        if (permisosAccion != null) {
            for (Permission permiso : permisosAccion) {
                if (permiso.getValue().equals(valor)) {
                    return true;
                }
            }
        }
        return false;
    }
}
