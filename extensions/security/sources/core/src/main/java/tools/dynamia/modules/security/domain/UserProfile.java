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


package tools.dynamia.modules.security.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import tools.dynamia.domain.jpa.BaseEntity;
import tools.dynamia.modules.saas.api.AccountAware;


/**
 * @author Mario Serrano Leones
 */
@Entity
@Table(name = "sec_users_profiles")
@BatchSize(size = 20)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfile extends BaseEntity implements AccountAware {

    @OneToOne
    @JoinColumn(updatable = false)
    private Profile profile;
    @ManyToOne
    @JoinColumn(updatable = false)
    @JsonIgnore
    private User user;
    private Long accountId;

    public UserProfile() {
    }

    public UserProfile(Profile profile, User user) {
        this.profile = profile;
        this.user = user;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile perfil) {
        this.profile = perfil;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User usuario) {
        this.user = usuario;
    }

    @Override
    public Long getAccountId() {
        return accountId;
    }

    @Override
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
