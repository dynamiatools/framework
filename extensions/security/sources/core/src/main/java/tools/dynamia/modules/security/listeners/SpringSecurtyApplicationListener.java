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

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.security.CurrentUser;
import tools.dynamia.modules.security.domain.User;

import java.util.Comparator;
import java.util.List;

@Component
public class SpringSecurtyApplicationListener implements ApplicationListener<AuthenticationSuccessEvent> {

    public static void fireOnUserTokenLoginListeners(User user) {
        CurrentUser.get().init(user);


        List<LoginListener> listeners = Containers.get().findObjects(LoginListener.class).stream()
                .sorted(Comparator.comparingInt(LoginListener::getPriority)).toList();

        listeners.forEach(listener -> listener.onTokenLogin(user));
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        fireOnUserLogin(event);
    }

    private void fireOnUserLogin(AuthenticationSuccessEvent evt) {
        User usuario = (User) evt.getAuthentication().getPrincipal();
        fireOnUserLoginListeners(usuario);

    }

    public static void fireOnUserLoginListeners(User usuario) {

        CurrentUser.get().init(usuario);


        List<LoginListener> listeners = Containers.get().findObjects(LoginListener.class).stream()
                .sorted(Comparator.comparingInt(LoginListener::getPriority)).toList();

        listeners.forEach(listener -> listener.onLoginSuccess(usuario));
    }

}
