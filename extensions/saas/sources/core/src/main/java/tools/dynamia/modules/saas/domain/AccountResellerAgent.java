package tools.dynamia.modules.saas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import tools.dynamia.domain.contraints.Email;
import tools.dynamia.domain.contraints.NotEmpty;
import tools.dynamia.domain.jpa.SimpleEntity;

import java.io.Serializable;

/**
 * Entity representing an agent or seller for a reseller (AccountReseller).
 * Contains basic contact information and a many-to-one relationship with AccountReseller.
 * <p>
 * Fields:
 * <ul>
 *   <li>name: Agent's full name (required)</li>
 *   <li>email: Contact email</li>
 *   <li>phone: Contact phone number</li>
 *   <li>address: Contact address</li>
 *   <li>identification: Identification number or code</li>
 *   <li>reseller: Associated AccountReseller</li>
 * </ul>
 */
@Entity
public class AccountResellerAgent extends SimpleEntity implements Serializable {


    @NotEmpty(message = "Agent name is required")
    private String name;

    @Email
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 200)
    private String address;

    @Column(length = 50)
    private String identification;

    @ManyToOne(optional = false)
    private AccountReseller reseller;


    // Getters y setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public AccountReseller getReseller() {
        return reseller;
    }

    public void setReseller(AccountReseller reseller) {
        this.reseller = reseller;
    }

    @Override
    public String toString() {
        return name;
    }
}
