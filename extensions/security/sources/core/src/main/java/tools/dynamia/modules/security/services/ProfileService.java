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


package tools.dynamia.modules.security.services;

import tools.dynamia.modules.security.domain.Permission;
import tools.dynamia.modules.security.domain.Profile;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.domain.UserProfile;

import java.util.List;

/**
 * @author Mario Serrano Leones
 */
public interface ProfileService {


    String ALL_ACTIONS_PERMISSION = "AllActions";
    String ACCESS_PERMISSION = "Access";
    String ACTION_PERMISSION = "Action";

    void deletePermissions(List<Permission> permissions);

    void saveProfiles(User entity);

    void deleteUserProfiles(List<UserProfile> perfiles);

    List<Permission> getPermissions(Long accountId, String username, String permissionType);

    List<Permission> getPermissions(Long accountId, User user, String permissionType);


    List<Permission> getPermissions(Long accountId, User user);

    Profile getProfileByName(String name, boolean autocreate);


    Profile getDefaultProfile();

    Profile getAdminProfile();

    Profile getProfileByInternalName(String internalName, Long accountId);

    List<User> getUsersByProfile(Profile profile);


    List<Profile> getProfiles(User user);
}
