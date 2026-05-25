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

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Intercepts login controller rendering to customize model data or flow.
 * Implementations can inject additional attributes, flags, or contextual
 * values before the login view is rendered.
 */
public interface LoginControllerInterceptor {

    /**
     * Called when the login controller is preparing the login page.
     *
     * @param request the current HTTP request
     * @param viewName the view name that will be rendered
     * @param params mutable model parameters for the login view
     */
    void login(HttpServletRequest request, String viewName, Map<String, Object> params);


}
