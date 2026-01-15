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


package tools.dynamia.modules.security.listeners;


import tools.dynamia.modules.security.domain.User;

import java.util.Map;

/**
 * @author Mario Serrano Leones
 */
public interface LoginListener {

    default void onLoginSuccess(User user) {
    }

    default void onLoginPage(Map<String, Object> params) {
        //ignore
    }


    default int getPriority() {
        return 100;
    }

    default void onTokenLogin(User user){}
}
