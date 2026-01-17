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
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.security.core.GrantedAuthority;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.jpa.BaseEntity;
import tools.dynamia.modules.saas.api.AccountAware;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Archivo: Perfil.java Fecha de Creacion: 27/06/2009
 *
 * @author Ing. Mario Serrano Leones
 */
@Entity
@Table(name = "sec_profiles")
@BatchSize(size = 20)
@tools.dynamia.domain.OrderBy("name")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Profile extends BaseEntity implements GrantedAuthority, Cloneable, AccountAware {

    @NotNull(message = "Ingrese nombre del perfil")
    private String name;
    private String description;
    private String internalName;
    @JsonIgnore
    private boolean editable = true;
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("value")
    @JsonIgnore
    private List<Permission> permissions = new ArrayList<>();

    private Long accountId;

    public String getDescription() {
        return description;
    }

    public void setDescription(String descripcion) {
        this.description = descripcion;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public void generateInternalName() {
        if (name != null) {
            this.internalName = "ROLE_" + name.toUpperCase().replace(" ", "_");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String nombre) {
        this.name = nombre;
        generateInternalName();
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permisos) {
        this.permissions = permisos;
    }


    @Override
    public String getAuthority() {
        return getInternalName();
    }

    public int compareTo(Object o) {
        if (o instanceof GrantedAuthority) {
            String rhsRole = ((GrantedAuthority) o).getAuthority();

            if (rhsRole == null) {
                return -1;
            }

            return getAuthority().compareTo(rhsRole);
        }
        return -1;
    }

    public void addPermission(Permission p) {
        if (p != null) {
            for (Permission permission : permissions) {
                if (permission.getType().equals(p.getType()) && permission.getValue().equals(p.getValue())) {
                    throw new ValidationError("El permiso seleccionado ya fue asignado");
                }
            }

            p.setAccountId(getAccountId());
            permissions.add(p);
            p.setProfile(this);
        }
    }

    public void removePermission(Permission p) {
        if (p != null) {
            permissions.remove(p);
            p.setProfile(null);
        }
    }

    @Override
    public String toString() {
        return name;
    }


    @Override
    public Profile clone() {
        Profile clon = BeanUtils.clone(this, "id", "accountId", "permisos", "usuarios");
        var now = LocalDateTime.now();
        clon.setCreationDate(now.toLocalDate());
        clon.setCreationTime(now.toLocalTime()  );
        clon.setCreator(null);
        clon.setLastUpdate(null);
        clon.setLastUpdater(null);

        getPermissions().forEach(p -> clon.addPermission(p.clone()));


        return clon;
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
