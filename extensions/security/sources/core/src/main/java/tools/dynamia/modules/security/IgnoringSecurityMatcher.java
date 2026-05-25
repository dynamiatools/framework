
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

/**
 * Provides request matcher patterns that should bypass security filters.
 * Implementations are typically used to register public paths such as static
 * assets, health endpoints, or public APIs.
 */
public interface IgnoringSecurityMatcher {

    /**
     * Returns the matcher expressions that must be ignored by security.
     *
     * @return an array of path matcher patterns
     */
    String[] matchers();

}
