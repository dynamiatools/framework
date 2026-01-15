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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.BatchSize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.jpa.BaseEntity;
import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.util.BasicEntityJsonDeserializer;
import tools.dynamia.domain.util.BasicEntityJsonSerializer;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.saas.api.AccountAware;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "sec_users", indexes = {@Index(columnList = "username"), @Index(columnList = "email")})
@BatchSize(size = 100)
@Cacheable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User extends BaseEntity implements UserDetails, AccountAware {

    /**
     *
     */
    private static final long serialVersionUID = 7107687991598657957L;
    @Column(updatable = false)
    @Size(min = 5)
    private String username;
    @Size(min = 5)
    @JsonIgnore
    private String password;

    private String uuid = UUID.randomUUID().toString();
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;
    private boolean passwordExpired;
    private String comments;
    @NotNull(message = "User fullname required")
    private String fullname;

    private String email;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<UserProfile> profiles = new ArrayList<>();
    @Column(unique = true)
    @JsonIgnore
    private String userKey;
    @Column(length = 1000)
    private String reference;
    @OneToOne
    @JsonSerialize(using = BasicEntityJsonSerializer.class)
    @JsonDeserialize(using = BasicEntityJsonDeserializer.class)
    private EntityFile photo;

    private String phoneNumber;
    private String mobileNumber;


    private Long accountId;


    @Transient
    private List<GrantedAuthority> grantedAuthorities;

    private void initProfilesCache() {
        if (grantedAuthorities == null && getId() != null) {
            grantedAuthorities = DomainUtils.lookupCrudService()
                    .executeQuery(QueryBuilder.select("profile")
                            .from(UserProfile.class, "up")
                            .where("up.user.id", QueryConditions.eq(getId())));
        }
    }

    public static List<User> findActivosByAccount(Long accountId) {
        return DomainUtils.lookupCrudService().find(User.class, QueryParameters.with("enabled", true)
                .add("accountId", accountId)
                .orderBy("nombreReal"));
    }

    public User() {

    }

    public User(String username) {
        this.username = username;
    }

    public EntityFile getPhoto() {
        return photo;
    }

    public void setPhoto(EntityFile foto) {
        this.photo = foto;
    }

    /**
     * Referencia de usuario, puede ser utilizada para integracion con otros
     * modulos
     */
    public String getReference() {
        return reference;
    }

    /**
     * Utilice este campo para asignar cualquier referencia del usuario, sirve
     * para integracion con otros modulos
     */
    public void setReference(String referencia) {
        this.reference = referencia;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String llave) {
        this.userKey = llave;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String nombreReal) {
        this.fullname = nombreReal;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String observaciones) {
        this.comments = observaciones;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<UserProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<UserProfile> perfiles) {
        this.profiles = perfiles;
    }

    public boolean isPasswordExpired() {
        return passwordExpired;
    }

    public void setPasswordExpired(boolean passwordExpired) {
        this.passwordExpired = passwordExpired;
    }

    public void addProfile(Profile profile) {
        if (profile != null) {
            for (UserProfile p : profiles) {
                if (p.getProfile().equals(profile)) {
                    throw new ValidationError("Profile " + profile.getName() + " is already assigned");
                }
            }
            UserProfile newProfile = new UserProfile(profile, this);
            newProfile.setAccountId(getAccountId());
            profiles.add(newProfile);
        }
    }

    public void removePerfil(UserProfile perfil) {
        if (perfil != null) {
            profiles.remove(perfil);
            perfil.setProfile(null);
            perfil.setUser(this);
        }
    }

    @Override
    public String toString() {
        if (username == null) {
            return super.toString();
        } else {
            return fullname + " (" + username + ")";
        }
    }

    /*--------------------------------------------------*/
    @Override
    @JsonIgnore
    public List<GrantedAuthority> getAuthorities() {
        initProfilesCache();
        return grantedAuthorities;
    }

    public Profile getProfile(String name) {
        if (getAuthorities() != null) {
            for (GrantedAuthority grantedAuthority : getAuthorities()) {
                if (grantedAuthority.getAuthority().equals(name)) {
                    return (Profile) grantedAuthority;
                }
            }
        }
        return null;
    }

    public Profile getProfile(Long perfilId) {
        if (getAuthorities() != null) {
            for (GrantedAuthority grantedAuthority : getAuthorities()) {
                if (grantedAuthority instanceof Profile) {
                    if (((Profile) grantedAuthority).getId().equals(perfilId)) {
                        return (Profile) grantedAuthority;
                    }
                }
            }
        }
        return null;
    }

    @JsonIgnore
    public String getAssignedProfiles() {
        return getProfiles().stream().map(p -> p.getProfile().getName()).collect(Collectors.joining(","));

    }

    @JsonIgnore
    public String getRolesNames() {
        return getProfiles().stream().map(p -> p.getProfile().getInternalName()).collect(Collectors.joining(","));
    }

    public List<String> getRoles() {
        return getProfiles().stream().map(p -> p.getProfile().getInternalName()).toList();
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String telefono) {
        this.phoneNumber = telefono;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String celular) {
        this.mobileNumber = celular;
    }


    public String getUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
