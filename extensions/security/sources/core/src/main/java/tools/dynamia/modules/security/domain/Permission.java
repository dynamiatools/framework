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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.domain.jpa.SimpleEntity;
import tools.dynamia.modules.saas.api.AccountAware;

/**
 * @author Mario Serrano Leones
 */
@Entity
@Table(name = "sec_permissions")
@BatchSize(size = 100)
public class Permission extends SimpleEntity implements  Cloneable, AccountAware {

    @ManyToOne
    @JsonIgnore
    private Profile profile;
    @Column(name = "permission_type")
    private String type;
    @Column(name = "permission_value")
    private String value;
    private String description;
    private String source;
    private int level;
    private Long accountId;

    public Permission() {
    }

    public Permission(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public Permission(String type, String value, String description) {
        this.type = type;
        this.value = value;
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile perfil) {
        this.profile = perfil;
    }

    public String getType() {
        return type;
    }

    public void setType(String tipo) {
        this.type = tipo;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String valor) {
        this.value = valor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descripcion) {
        this.description = descripcion;
    }

    public Permission clone() {
        return BeanUtils.clone(this, "id", "accountId", "perfil");
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int nivel) {
        this.level = nivel;
    }

    public String getLevelStyle() {
        if (level > 0) {
            return "padding-left: " + (level * 10) + "px";
        } else {
            return "";
        }
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
