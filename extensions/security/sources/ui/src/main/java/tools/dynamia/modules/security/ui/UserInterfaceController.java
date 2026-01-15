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
import org.springframework.web.context.annotation.SessionScope;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.modules.security.CurrentUser;
import tools.dynamia.modules.security.domain.Permission;
import tools.dynamia.modules.security.services.ProfileService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * @author Mario Serrano Leones
 */
@Component("uiController")
@SessionScope
public class UserInterfaceController extends HashMap<String, Boolean> implements Serializable {


    private final LoggingService logger = new SLF4JLoggingService(UserInterfaceController.class);
    private List<Permission> accessPermissions;
    private boolean adminUser;


    public void init() {
        try {
            if (accessPermissions == null || accessPermissions.isEmpty()) {
                if (CurrentUser.get().isLogged()) {
                    adminUser = CurrentUser.get().hasProfile("ROLE_ADMIN");
                    accessPermissions = CurrentUser.get().getPermissions()
                            .stream().filter(p -> ProfileService.ACCESS_PERMISSION.equals(p.getType()))
                            .toList();
                }
            }
        } catch (Exception e) {
            logger.error("Error UI Access controller", e);
        }
    }

    @Override
    public Boolean get(Object key) {
        if (!containsKey(key)) {
            put(key.toString(), hasAccess(key.toString()));
        }

        return super.get(key);
    }

    public boolean hasAccess(String key) {
        init();

        if (adminUser) {
            return true;
        }
        if (key == null || key.isBlank()) {
            return false;
        }

        key = key.toLowerCase();
        for (Permission p : accessPermissions) {
            if (p.getValue() != null) {
                var valor = p.getValue().toLowerCase();
                if (valor.equals(key) || valor.startsWith(key)) {
                    return true;
                }
            }
        }
        return false;
    }
}
