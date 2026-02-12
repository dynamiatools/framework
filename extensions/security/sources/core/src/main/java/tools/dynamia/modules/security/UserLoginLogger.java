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

package tools.dynamia.modules.security;

import org.springframework.stereotype.Component;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.listeners.LoginListener;

@Component
public class UserLoginLogger implements LoginListener {

    private final LoggingService logger = new SLF4JLoggingService(UserLoginLogger.class);

    @Override
    public void onLoginSuccess(User user) {
        logger.info("User [" + user.getUsername() + "] logged in to the system ");
    }


}
