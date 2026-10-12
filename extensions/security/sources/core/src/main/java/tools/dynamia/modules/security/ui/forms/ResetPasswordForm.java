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

package tools.dynamia.modules.security.ui.forms;

import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.services.SecurityService;
import tools.dynamia.actions.ActionCommand;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.integration.Containers;
import tools.dynamia.ui.Form;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;

public class ResetPasswordForm extends Form {

    private final User user;
    private String newPassword;
    private String newPassword2;

    public ResetPasswordForm(User user) {
        this.user = user;
    }

    @ActionCommand(name = "Reset", image = "reset")
    public void reiniciar() {
        try {

            SecurityService service = Containers.get().findObject(SecurityService.class);
            service.resetPassword(user, newPassword, newPassword2);
            submit();
        } catch (ValidationError e) {
            UIMessages.showMessage(e.getMessage(), MessageType.ERROR);
        }
    }

    public User getUser() {
        return user;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword2() {
        return newPassword2;
    }

    public void setNewPassword2(String newPassword2) {
        this.newPassword2 = newPassword2;
    }
}
