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


package tools.dynamia.modules.security.services.impl;

import jakarta.annotation.PostConstruct;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.ValidatorUtil;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.AbstractService;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.security.TokenRequest;
import tools.dynamia.modules.security.TokenResponse;
import tools.dynamia.modules.security.domain.Profile;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.domain.UserAccessToken;
import tools.dynamia.modules.security.domain.UserProfile;
import tools.dynamia.modules.security.services.ProfileService;
import tools.dynamia.modules.security.services.SecurityService;

import java.util.Date;
import java.util.List;

/**
 * @author Mario Serrano Leones
 */


public class SecurityServiceImpl extends AbstractService implements SecurityService, UserDetailsService {


    private final ProfileService profileService;
    private final CrudService crudService;
    private final PasswordEncoder passwordEncoder;


    public SecurityServiceImpl(ProfileService profileService, CrudService crudService, PasswordEncoder passwordEncoder) {
        this.profileService = profileService;
        this.crudService = crudService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected CrudService crudService() {
        return crudService;
    }


    @Override
    @PostConstruct
    public void checkAccountDefaultsSettings() {
        try {
            profileService.getDefaultProfile();
            profileService.getAdminProfile();
            createDefaultUser();

        } catch (Exception e) {
            log("Error checking account default settings ", e);
        }
    }

    @Override
    public User loadUserByUsername(String username) {
        log("Loading user by username: " + username);
        var user = crudService().findSingle(User.class, "username", QueryConditions.eq(username));

        if (user == null) {
            throw new UsernameNotFoundException("User with username " + username + " not found");
        }
        return user;
    }

    @Override
    @Transactional
    public User createUser(User usuario) {
        if (usuario.getId() == null) {
            String password = usuario.getPassword();

            usuario.setPassword(passwordEncoder.encode(password));
            usuario = crudService().create(usuario);
            UserProfile perfil = new UserProfile(profileService.getDefaultProfile(), usuario);
            perfil.setAccountId(usuario.getAccountId());
            crudService().create(perfil);
        }
        return usuario;
    }


    @Override
    public void validatePassword(String password, String rePassword) {

        ValidatorUtil.validateEmpty(password, "Enter user password");


        if (!password.equalsIgnoreCase(rePassword)) {
            throw new ValidationError("Password not matches");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setNewPassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        User user = loadUserByUsername(username);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ValidationError("Current password is invalid");
        }

        validatePassword(newPassword, confirmPassword);

        if (newPassword.equals(currentPassword)) {
            throw new ValidationError("New password is equal to the current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordExpired(false);
        crudService().update(user);
    }

    @Transactional
    public void createDefaultUser() {

        long count = crudService().count(User.class);
        if (count == 0) {

            User admin = new User();
            admin.setFullname("Administrator");
            admin.setUsername("admin");
            admin.setEmail("admin@admin.com");
            admin.setPassword("adminadmin");
            admin.setCreator("admin");
            createUser(admin);
            UserProfile profile = new UserProfile(profileService.getAdminProfile(), admin);
            crudService().create(profile);
        }
    }


    @Override
    public User getUserByEmail(String email) {
        return crudService().findSingle(User.class, "email", QueryConditions.eq(email));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetPassword(User user, String nuevo, String reNuevo) {
        validatePassword(nuevo, reNuevo);
        user.setPassword(passwordEncoder.encode(nuevo));
        crudService().update(user);
    }


    @Override
    public User getCurrentSessionUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new SecurityException("No current user found");
        }
        return (User) auth.getPrincipal();
    }


    @Override
    public List<Profile> getProfilesByAccountId(Long accountId) {
        return crudService().find(Profile.class, "accountId", accountId);
    }

    @Override
    public UserAccessToken findAccessToken(String token, boolean apiDomain) {
        QueryParameters params = QueryParameters.with("token", QueryConditions.eq(token));

        if (apiDomain) {
            params.add("accountId", QueryConditions.isNotNull());
        }

        return crudService().findSingle(UserAccessToken.class, params);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAccessToken(UserAccessToken userToken) {

        String sql = "update " + UserAccessToken.class.getSimpleName() + " t set t.lastAccess = :acceso, t.hits=t.hits+1 " +
                "where t.id = :id";
        crudService().execute(sql, QueryParameters.with("acceso", new Date())
                .add("id", userToken.getId()));

    }


    @Override
    public TokenResponse requestToken(TokenRequest request, User user) {
        UserAccessToken token = null;

        if (!request.isOtp()) {
            token = crudService().findSingle(UserAccessToken.class, QueryParameters.with("tokenName", QueryConditions.eq(request.getName()))
                    .add("user", user));
        }

        if (token == null) {
            token = new UserAccessToken();
            token.setAutomatic(true);
            token.setTokenName(request.getName());
            token.setUser(user);
            token.setAccountId(user.getAccountId());
            token.setTokenSource(request.getSource());
            token.setOtp(request.isOtp());
        }
        token.generate();
        token.save();

        var response = new TokenResponse(token.getTokenName(), token.getToken(), token.getUser().getUsername(),
                token.getUser().getId(), token.getExpirationDate());
        response.setRoles(user.getRoles());
        if (token.isOtp()) {
            response.setOtp(true);
        }

        AccountServiceAPI accountServiceAPI = Containers.get().findObject(AccountServiceAPI.class);
        if (accountServiceAPI != null) {
            var account = accountServiceAPI.getAccount(token.getAccountId());
            if (account != null) {
                response.setAccountName(account.getName());
                response.setAccountSubdomain(account.getSubdomain());
                response.setAccountId(account.getId());
            }
        }
        return response;
    }
}
