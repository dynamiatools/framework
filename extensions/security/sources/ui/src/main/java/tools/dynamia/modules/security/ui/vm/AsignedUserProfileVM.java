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

package tools.dynamia.modules.security.ui.vm;

import tools.dynamia.modules.security.domain.Profile;
import tools.dynamia.modules.security.domain.UserProfile;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.services.SecurityService;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Window;
import tools.dynamia.domain.services.AbstractService;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.util.ZKUtil;

import java.util.ArrayList;
import java.util.List;

public class AsignedUserProfileVM extends AbstractService {

    private final SecurityService service = Containers.get().findObject(SecurityService.class);
    private final AccountServiceAPI accountServiceAPI = Containers.get().findObject(AccountServiceAPI.class);
    private User model;
    private List<Profile> profiles;
    private Window parentWindow;


    @Init
    public void init() {
        User entity = (User) ZKUtil.getExecutionEntity();
        if (entity != null) {
            this.model = entity;
            this.model = crudService().reload(model);
        }
        this.parentWindow = ZKUtil.getExecutionParentWindow();
        profiles = new ArrayList<>(service.getProfilesByAccountId(accountServiceAPI.getCurrentAccountId()));
        List<Profile> asigned = this.model.getProfiles().stream().map(UserProfile::getProfile).toList();
        profiles.removeAll(asigned);
    }

    @Command
    @NotifyChange("*")
    public void addProfile(@BindingParam("profile") Profile profile) {
        if (profile != null) {
            model.addProfile(profile);
            profiles.remove(profile);
        }
    }

    @Command
    @NotifyChange("*")
    public void removeProfile(@BindingParam("profile") UserProfile userProfile) {
        if (userProfile != null) {
            Profile perfil = userProfile.getProfile();
            model.removePerfil(userProfile);
            profiles.add(perfil);
        }
    }

    @Command
    public void save() {
        UIMessages.showLocalizedQuestion("Esta seguro que desea guardar perfiles de usuario %s ?", List.of(model), () -> {
            crudService().executeWithinTransaction(() -> crudService().save(model));

            UIMessages.showLocalizedMessage("Perfiles de %s guardados correctamente", model);
            if (parentWindow != null) {
                Events.postEvent(new Event(Events.ON_CLOSE, parentWindow));
            }
        });

    }

    public User getModel() {
        return model;
    }

    public List<Profile> getProfiles() {
        return profiles;
    }


}
