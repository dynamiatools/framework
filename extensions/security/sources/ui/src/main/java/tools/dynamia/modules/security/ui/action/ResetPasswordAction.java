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


package tools.dynamia.modules.security.ui.action;

import org.zkoss.zul.Window;
import tools.dynamia.actions.ActionGroup;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.ui.forms.ResetPasswordForm;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.viewers.ui.Viewer;

/**
 * @author Mario Serrano Leones
 */
@InstallAction
public class ResetPasswordAction extends AbstractCrudAction {

    public ResetPasswordAction() {
        setName("Reiniciar password");
        setImage("unlock");
        setGroup(ActionGroup.get("USERS"));
        setMenuSupported(true);
        setApplicableClass(User.class);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        if (evt.getData() != null) {
            User user = crudService().reload((User) evt.getData());
            ResetPasswordForm form = new ResetPasswordForm(user);
            Window window = Viewer.showDialog("Reset Password", "form", form);

            form.onSubmit(() -> {
                UIMessages.showMessage("Password rested");
                window.detach();
            });

            form.onCancel(window::detach);

        } else {
            UIMessages.showMessage("Select user", MessageType.ERROR);
        }
    }

}
