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


package tools.dynamia.modules.security;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.security.domain.Permission;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.services.ProfileService;
import tools.dynamia.web.util.HttpUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mario Serrano Leones
 */
@Component("currentUser")
@SessionScope
public class CurrentUser implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3517635662986656812L;


    private final LoggingService logger = new SLF4JLoggingService(CurrentUser.class);
    protected User user;

    private String ip;
    private String browser;
    private Date timestamp;
    private Map<String, Long> permissionsCache;
    private List<Permission> permissions;

    public static CurrentUser get() {
        try {
            CurrentUser currentUser = Containers.get().findObject(CurrentUser.class);
            return currentUser;
        } catch (Exception e) {
            var mock = new CurrentUser();
            mock.init(new User("anonymous"));
            return mock;
        }
    }


    public String getUsername() {
        if (isLogged()) {
            return user.getUsername();
        } else {
            return "anonimo";
        }
    }

    public EntityFile getPhoto() {
        if (isLogged()) {
            return user.getPhoto();
        } else {
            return null;
        }
    }

    public String getFullname() {
        if (isLogged()) {
            return user.getFullname();
        } else {
            return null;
        }
    }

    public boolean hasProfile(String name) {
        if (isLogged()) {
            return user.getProfile(name) != null;
        } else {
            return false;
        }
    }

    public boolean hasProfile(Long profileId) {
        if (isLogged()) {
            return user.getProfile(profileId) != null;
        } else {
            return false;
        }
    }

    public boolean hasPermission(String permiso) {
        if (isLogged()) {
            return permissionsCache != null && permissionsCache.containsKey(permiso);
        } else {
            return false;
        }
    }

    public User getUser() {
        return user;
    }

    public boolean isLogged() {
        return user != null && user.getId() != null;
    }

    public String getIp() {
        return ip;
    }

    public String getBrowser() {
        return browser;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void init(User usuario) {
        if (usuario != null) {
            this.user = usuario;
            logger.info("Configurando usuario actual: " + usuario);
            try {
                this.browser = HttpUtils.getBrowser();
                this.ip = HttpUtils.getClientIp();
                this.timestamp = new Date();
                loadPermissions();
            } catch (Exception e) {
                logger.error("Error cargando datos de usuario actual", e);
            }
        }
    }

    public void update(User user) {
        if (user != null) {
            this.user = user;
            logger.info("Usuario actual updated");
        }
    }

    public void reload() {
        if (isLogged()) {
            CrudService crudService = Containers.get().findObject(CrudService.class);
            user = crudService.reload(user);
            loadPermissions();
        }

    }

    private void loadPermissions() {
        if (user != null && user.getId() != null) {
            logger.info("Cargando permisos de " + user);
            var profileService = Containers.get().findObject(ProfileService.class);
            permissionsCache = new HashMap<>();
            this.permissions = profileService.getPermissions(user.getAccountId(), user);
            if (permissions != null) {
                permissions.forEach(permiso -> this.permissionsCache.put(permiso.getValue(), permiso.getId()));
            }
            logger.info("Permisos especiales de " + user + " OK");
        }
    }

    public void clear() {
        user = null;
        ip = null;
        permissionsCache = null;
        browser = null;
        timestamp = null;
    }

    public boolean isAdmin() {
        return hasProfile("ROLE_ADMIN");
    }

    public boolean isNotAdmin() {
        return !isAdmin();
    }

    public List<Permission> getPermissions() {
        return permissions;
    }
}
