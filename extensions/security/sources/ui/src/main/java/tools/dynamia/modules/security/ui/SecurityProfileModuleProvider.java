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

import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleProvider;
import tools.dynamia.navigation.Page;
import tools.dynamia.navigation.PageGroup;

/**
 * Registers the "My Profile" page of the {@code system/security} group. The page is a ZK zul view, so it lives in
 * the ui module; the rest of the security pages are registered by {@code SecurityModuleProvider}. Both providers
 * contribute to the same module and group, which the navigation container merges.
 *
 * @author Mario Serrano Leones
 */
@Provider
public class SecurityProfileModuleProvider implements ModuleProvider {

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
        perfilPage.setPosition(-1); // keep it first in the group, as before the split
        pg.addPage(perfilPage);

        return module;
    }
}
