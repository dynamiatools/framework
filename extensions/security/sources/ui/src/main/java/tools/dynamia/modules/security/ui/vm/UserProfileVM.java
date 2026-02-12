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

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.security.CurrentUser;
import tools.dynamia.modules.security.domain.Profile;
import tools.dynamia.modules.security.domain.UserProfile;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.services.SecurityService;
import tools.dynamia.modules.security.services.UserService;
import tools.dynamia.navigation.NavigationManager;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.AbstractViewModel;

import java.util.List;
import java.util.stream.Collectors;

public class UserProfileVM extends AbstractViewModel<User> {

    private CrudService crudService;
    private SecurityService service;
    private UserService userService;

    private User user;

    private List<Profile> profiles;

    private boolean resetPassword;
    private String currentPassword;
    private String newPassword;
    private String newPassword2;
    private NavigationManager navManager;

    @Init
    public void init() {
        super.initDefaults();
        this.user = getModel();
        crudService = Containers.get().findObject(CrudService.class);
        service = Containers.get().findObject(SecurityService.class);
        userService = Containers.get().findObject(UserService.class);
        navManager = NavigationManager.getCurrent();
        getUser();
        loadProfiles();

    }

    private void loadProfiles() {
        profiles = user.getProfiles()
                .stream()
                .map(UserProfile::getProfile)
                .collect(Collectors.toList());

    }


    @Command
    @NotifyChange("*")
    public void save() {
        try {
            crudService.update(user);
            if (isResetPassword()) {
                service.setNewPassword(user.getUsername(), currentPassword, newPassword, newPassword2);
                reset();
            }

            UIMessages.showMessage("Cambios guardados exitosamente");

        } catch (ValidationError e) {
            UIMessages.showMessage(e.getMessage(), MessageType.ERROR);
        } catch (Exception e) {
            UIMessages.showException("Error al guardar cambios", e);
        }
    }

    @Command
    @NotifyChange("*")
    public void resetPassword() {
        try {

            service.setNewPassword(user.getUsername(), currentPassword, newPassword, newPassword2);
            reset();

            UIMessages.showMessage("Password actualizado exitosamente");

            closeWindow();
        } catch (ValidationError e) {
            UIMessages.showMessage(e.getMessage(), MessageType.ERROR);
        } catch (Exception e) {
            UIMessages.showException("Error al guardar cambios", e);
        }
    }

    private void reset() {
        currentPassword = null;
        newPassword = null;
        newPassword2 = null;
        resetPassword = false;
        CurrentUser.get().reload();

    }

    public User getUser() {
        if (user == null) {
            user = getCurrentUser();
        }
        return user;
    }

    public List<Profile> getProfiles() {
        return profiles;
    }



    public boolean isResetPassword() {
        return resetPassword;
    }

    public void setResetPassword(boolean resetPassword) {
        this.resetPassword = resetPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String nuevoPassword) {
        this.newPassword = nuevoPassword;
    }

    public String getNewPassword2() {
        return newPassword2;
    }

    public void setNewPassword2(String newPassword2) {
        this.newPassword2 = newPassword2;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    private User getCurrentUser() {
        User currentUser = CurrentUser.get().getUser();
        if (currentUser != null) {
            currentUser = crudService.reload(currentUser);
        }
        return currentUser;
    }

}
