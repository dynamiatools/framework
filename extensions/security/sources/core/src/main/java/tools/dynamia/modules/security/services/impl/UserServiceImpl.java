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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.jpa.JpaQuery;
import tools.dynamia.domain.query.Inlist;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.modules.security.domain.User;
import tools.dynamia.modules.security.services.UserService;

import java.util.List;

/**
 * @author Mario Serrano Leones
 */

public class UserServiceImpl implements UserService {

    private LoggingService logger = new SLF4JLoggingService(UserService.class);

    private final CrudService crudService;
    @PersistenceContext
    private EntityManager em;


    public UserServiceImpl(CrudService crudService) {
        this.crudService = crudService;
    }

    @Override
    public User getUsuario(Long accountId, String username) {
        return crudService.findSingle(User.class, QueryParameters.with("accountId", accountId)
                .add("username", QueryConditions.eq(username)));
    }

    @Override
    public List<User> getUsuarios(Long accountId, String... usernames) {
        QueryParameters qp = QueryParameters.with("username", new Inlist<>(usernames))
                .add("accountId", accountId);
        return crudService.find(User.class, qp);


    }

    @Override
    public List<User> getUsuarios(Long accountId, String usernames) {
        String[] users = usernames.split(",");
        return getUsuarios(accountId, users);
    }

    @Override
    public List<User> getUsuarios(Long accountId, List<String> usernames) {
        String[] users = usernames.toArray(new String[0]);
        return getUsuarios(accountId, users);
    }

    @Override
    public String getEmailUsuario(Long accountId, String username) {
        User user = getUsuario(accountId, username);
        if (user != null) {
            return user.getEmail();
        } else {
            return null;
        }
    }

    @Override
    public List<String> getEmailUsuarios(Long accountId, String... usernames) {
        QueryParameters qp = QueryParameters.with("usernames", new Inlist<>(usernames))
                .add("accountId", accountId);

        String queryText = QueryBuilder.select(User.class, "u", "u.email")
                .where("u.username in (:usernames)")
                .and("u.accountId = :accountId").toString();
        Query query = em.createQuery(queryText);
        qp.applyTo(new JpaQuery(query));

        return query.getResultList();
    }

    @Override
    public User getUsuarioByToken(String token, Long accountId) {
        return crudService.findSingle(User.class, QueryParameters.with("accountId", accountId)
                .add("token", QueryConditions.eq(token)));

    }


    @Override
    public List<User> findUsuarios(Long accountId) {
        return crudService.find(User.class, QueryParameters.with("enabled", true)
                .add("accountId", accountId)
                .orderBy("username"));
    }

    @Override
    public User getUsuarioById(Long accountId, Long id) {
        return crudService.findSingle(User.class, QueryParameters.with("accountId", accountId)
                .add("id", id));
    }
}
