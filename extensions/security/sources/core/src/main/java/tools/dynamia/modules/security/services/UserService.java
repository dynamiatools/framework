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


package tools.dynamia.modules.security.services;

import tools.dynamia.modules.security.domain.User;

import java.util.List;

/**
 * @author Mario Serrano Leones
 */
public interface UserService {

    String getEmailUsuario(Long accountId, String username);

    List<String> getEmailUsuarios(Long accountId, String... usernames);

    User getUsuario(Long accountId, String username);

    List<User> getUsuarios(Long accountId, String... usernames);

    List<User> getUsuarios(Long accountId, String usernames);

    List<User> getUsuarios(Long accountId, List<String> usernames);

    User getUsuarioByToken(String token, Long accountId);

    List<User> findUsuarios(Long accountId);

    User getUsuarioById(Long accountId, Long id);
}
