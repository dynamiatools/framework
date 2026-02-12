
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

package tools.dynamia.modules.security.ui.vm;

import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.services.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.csrf.CsrfToken;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zhtml.Form;
import org.zkoss.zhtml.Input;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.ValidatorUtil;
import tools.dynamia.integration.Containers;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.util.ZKBindingUtil;
import tools.dynamia.zk.util.ZKUtil;

/**
 * @author Mario Serrano Leones
 */
public class LoginVM {

    public static final String HIDDEN = "hidden";
    public static final String TYPE = "type";
    public static final String NAME = "name";
    private final LoggingService logger = new SLF4JLoggingService(LoginVM.class);

    private SecurityService service = Containers.get().findObject(SecurityService.class);

    @WireVariable
    private Session session;

    @WireVariable
    private Page page;

    private String username;
    private String password;
    private String message;
    private boolean rememberMe;

    private CsrfToken csrfToken;
    private Component view;

    @Init
    public void init(@ExecutionParam("CSRF") Object csrfToken, @ExecutionParam("username") String username) {
        this.username = username;
        if (csrfToken instanceof CsrfToken) {
            System.out.println("CSR TOKEN: " + csrfToken);
            this.csrfToken = (CsrfToken) csrfToken;
        }
        Exception ex = (Exception) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

        if (ex != null) {
            message = ex.getLocalizedMessage();
        }

    }

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        this.view = view;
        if (username != null && !username.isBlank()) {
            var passwordField = (Textbox) view.query(".password");
            if (passwordField != null) {
                passwordField.focus();
            }
        }
    }

    @Command
    public void login() {

        try {
            ValidatorUtil.validateEmpty(username, UIMessages.getLocalizedMessage("Ingrese nombre de usuario o email"));
            ValidatorUtil.validateEmpty(password, UIMessages.getLocalizedMessage("Ingrese password"));

            var userService = Containers.get().findObject(UserDetailsService.class);
            if (userService != null) {
                var request = (HttpServletRequest) Executions.getCurrent().getNativeRequest();
                var response = (HttpServletResponse) Executions.getCurrent().getNativeResponse();

                var user = userService.loadUserByUsername(username);
                ValidatorUtil.validateTrue(user.isEnabled(), "Usuario no habilitado");

                var passwordEncoder = Containers.get().findObject(PasswordEncoder.class);
                var passwordTest = passwordEncoder.matches(password, user.getPassword());
                ValidatorUtil.validateTrue(passwordTest, "Password invalido");


                if (user instanceof User usuario && usuario.isPasswordExpired()) {
                    var win = ZKUtil.showDialog("classpath:zk/seguridad/usuarios/passwordExpirado.zul", usuario.getFullname(), usuario,
                            "40%", "420px");
                    win.addEventListener(Events.ON_CLOSE, event -> {
                        UIMessages.showMessage("Ingrese password");
                        password = null;
                        ZKBindingUtil.postNotifyChange(this);
                    });
                } else {
                    Clients.showBusy("Hola " + user.getUsername());
                    doLogin();
                }
            } else {
                doLogin();
            }
        } catch (ValidationError | UsernameNotFoundException e) {
            UIMessages.showMessage(e.getMessage(), MessageType.WARNING);
        } catch (Exception e) {
            UIMessages.showException("Error Login " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Command
    public void rememberPassword() {
        UIMessages.showInput(UIMessages.getLocalizedMessage("Ingrese email de usuario"), String.class, email -> {
            User usuario = service.getUserByEmail(email);
            if (usuario != null) {
                UIMessages.showQuestion("Se generara un nuevo password para " + usuario.getFullname() + " y se enviara al correo. Desea Continuar?", () -> {
                    String newpassword = StringUtils.randomString().substring(0, 8);
                    service.resetPassword(usuario, newpassword, newpassword);
                    Messagebox.show("Password reiniciado exitosamente, verifique su email para conocer el nuevo password");
                });
            } else {
                Messagebox.show("No existe usuario registrado con email: " + email);
            }
        });
    }

    private void doLogin() {
        try {

            Form form = new Form();
            form.setDynamicProperty("action", "login");
            form.setDynamicProperty("method", "post");
            form.setPage(page);

            // Username
            createFormInput(form, "username", username);
            createFormInput(form, "password", password);
            createFormInput(form, "remember-me", rememberMe ? "1" : "0");

            // CSRF
            if (csrfToken != null) {
                createFormInput(form, csrfToken.getParameterName(), csrfToken.getToken());
            }

            Clients.submitForm(form);

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private void createFormInput(Form form, String name, String value) {
        Input input = new Input();
        input.setParent(form);
        input.setDynamicProperty(TYPE, HIDDEN);
        input.setDynamicProperty(NAME, name);
        input.setValue(value);
    }

    @Command
    public void logout() {
        try {
            Executions.getCurrent().sendRedirect("logout");
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

}
