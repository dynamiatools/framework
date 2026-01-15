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


package tools.dynamia.modules.security.services.impl;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.modules.security.domain.Permission;
import tools.dynamia.modules.security.domain.Profile;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.domain.UserProfile;
import tools.dynamia.modules.security.services.ProfileService;

import java.util.Collections;
import java.util.List;


/**
 * @author Mario Serrano Leones
 */
public class ProfileServiceImpl implements ProfileService {

    private LoggingService logger = new SLF4JLoggingService(ProfileService.class);

    private final CrudService crudService;


    public ProfileServiceImpl(CrudService crudService) {
        this.crudService = crudService;
    }

    @Override
    @Transactional
    public void deletePermissions(List<Permission> permissions) {
        for (Permission permiso : permissions) {
            if (permiso.getId() != null) {
                crudService.delete(Permission.class, permiso.getId());
            }
        }
        permissions.clear();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProfiles(User entity) {
        for (UserProfile perfilUsuario : entity.getProfiles()) {
            if (perfilUsuario.getId() == null) {
                crudService.create(perfilUsuario);
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteUserProfiles(List<UserProfile> perfiles) {
        if (perfiles != null) {
            for (UserProfile p : perfiles) {
                if (p.getId() != null) {
                    crudService.delete(UserProfile.class, p.getId());
                }
            }
            perfiles.clear();
        }
    }

    @Override

    public List<Permission> getPermissions(Long accountId, String username, String permissionType) {
        User usuario = crudService.findSingle(User.class, QueryParameters.with("username", QueryConditions.eq(username))
                .add("accountId", accountId));
        if (usuario == null) {
            return Collections.emptyList();
        }
        return getPermissions(accountId, usuario, permissionType);
    }

    @Override

    public List<Permission> getPermissions(Long accountId, User user, String permissionType) {
        QueryBuilder query = QueryBuilder.select().from(Permission.class, "p")
                .where("p.profile.id in (select up.profile.id from UserProfile up where up.user = :user)")
                .and("p.accountId = :accountId ");

        if (permissionType != null) {
            query.and("p.type = :type");
        }

        QueryParameters params = QueryParameters.with("user", user)
                .add("accountId", accountId);
        if (permissionType != null) {
            params.add("type", permissionType);
        }

        return crudService.executeQuery(query, params);
    }

    @Override

    public List<Permission> getPermissions(Long accountId, User user) {
        logger.info("Loading permissions for user: "+user);
        return getPermissions(accountId, user, null);
    }

    @Override
    public Profile getProfileByName(String name, boolean autocreate) {
        String internalName = "ROLE_" + name.toUpperCase().replace(" ", "_");
        Profile p = crudService.findSingle(Profile.class, "internalName", internalName);

        if (p == null && autocreate) {
            p = new Profile();
            p.setName(name);
            p.setCreator("auto");
            crudService.create(p);
        }

        return p;
    }


    @Override
    public Profile getDefaultProfile() {
        return getProfileByName("User", true);
    }


    @Override
    public Profile getAdminProfile() {
        return getProfileByName("Admin", true);
    }

    @Override

    public Profile getProfileByInternalName(String internalName, Long accountId) {
        return crudService.findSingle(Profile.class, QueryParameters.with("accountId", accountId)
                .add("internalName", QueryConditions.eq(internalName)));
    }

    @Override

    public List<User> getUsersByProfile(Profile profile) {
        var query = QueryBuilder.select("pu.user").from(UserProfile.class, "pu")
                .where("pu.profile", QueryConditions.eq(profile))
                .and("pu.accountId", QueryConditions.eq(profile.getAccountId()));

        return crudService.executeQuery(query);
    }

    @Override

    public List<Profile> getProfiles(User user) {
        var query = QueryBuilder.select("pu.profile").from(UserProfile.class, "pu")
                .where("pu.user", QueryConditions.eq(user))
                .and("pu.accountId", QueryConditions.eq(user.getAccountId()));

        return crudService.executeQuery(query);
    }

}
