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

import tools.dynamia.crud.CrudPage;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.modules.security.domain.Profile;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.domain.UserAccessToken;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleProvider;
import tools.dynamia.navigation.Page;
import tools.dynamia.navigation.PageGroup;

/**
 * @author Mario Serrano Leones
 */
@Provider
public class SecurityModuleProvider implements ModuleProvider {

    @Override
    public Module getModule() {

        Module module = new Module("system", "System");
        module.setIcon("settings");

        PageGroup pg = new PageGroup("security", "Security");
        module.addPageGroup(pg);

        Page perfilPage = new Page("myProfile", "My Profile", "classpath:zk/security/users/userProfile.zul");
        perfilPage.setAlwaysAllowed(true);
        perfilPage.setIcon("user-badge");
        perfilPage.setFeatured(true);

        pg.addPage(perfilPage);
        pg.addPage(new CrudPage("users", "Users", User.class)
                .icon("users")
                .featured());
        pg.addPage(new CrudPage("profiles", "Profiles", Profile.class));
        pg.addPage(new CrudPage("tokens", "Access Tokens", UserAccessToken.class));


        return module;

    }
}
