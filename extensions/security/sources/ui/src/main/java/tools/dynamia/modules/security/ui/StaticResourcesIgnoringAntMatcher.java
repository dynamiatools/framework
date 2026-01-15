
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

import org.springframework.stereotype.Component;
import tools.dynamia.modules.security.IgnoringSecurityMatcher;

/**
 * @author Mario Serrano Leones
 */
@Component
public class StaticResourcesIgnoringAntMatcher implements IgnoringSecurityMatcher {


    @Override
    public String[] matchers() {
        return new String[]{
                "/zkau/**", "/zkau", "/static/**", "/storage/**",
                "/styles/**", "/css/**", "/images/**", "/fonts/**", "/img/**", "/js/**", "/assets/**",
                "/*.ico", "/favicon.ico", "/ws-commands/**", "/ws-commands", "*.png", "*.svg", "/root/**",
                "/*.manifest", "/*.webmanifest", "*.css", "*.js", "/static/*.ico", "*.jpg", "*.jpeg", "*.ttf", "*.woff", "*.woff2"
        };
    }


}
