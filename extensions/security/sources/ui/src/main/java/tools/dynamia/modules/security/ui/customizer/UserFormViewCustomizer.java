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


package tools.dynamia.modules.security.ui.customizer;

import tools.dynamia.modules.security.domain.User;
import org.zkoss.zul.Textbox;
import tools.dynamia.viewers.ViewCustomizer;
import tools.dynamia.zk.viewers.form.FormView;

/**
 * @author Mario Serrano Leones
 */
public class UserFormViewCustomizer implements ViewCustomizer<FormView> {

    @Override
    public void customize(final FormView view) {

        view.addEventListener(FormView.ON_VALUE_CHANGED, event -> {
            if (view.getValue() instanceof User user) {
                if (user.getId() != null) {
                    disableComponents(view);
                }
            }
        });
    }

    private void disableComponents(FormView view) {
        Textbox username = (Textbox) view.getFieldComponent("username").getInputComponent();
        username.setDisabled(true);

        Textbox password = (Textbox) view.getFieldComponent("password").getInputComponent();
        password.setDisabled(true);

    }
}
