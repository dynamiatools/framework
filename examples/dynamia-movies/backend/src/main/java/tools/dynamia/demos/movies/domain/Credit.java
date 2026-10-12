package tools.dynamia.demos.movies.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import tools.dynamia.demos.movies.domain.enums.CreditRole;
import tools.dynamia.domain.jpa.BaseEntity;

/** A person taking part in a movie: the link between {@link Movie} and {@link Person}. */
@Entity
@Table(name = "credits")
public class Credit extends BaseEntity {

    @ManyToOne(optional = false)
    @NotNull
    private Movie movie;
    @ManyToOne(optional = false)
    @NotNull
    private Person person;
    @Enumerated(EnumType.STRING)
    private CreditRole role = CreditRole.ACTOR;
    private String characterName;
    /** Order in the cast list, 1 is the lead. */
    private int billing = 1;

    public Credit() {
    }

    public Credit(Person person, CreditRole role, int billing) {
        this.person = person;
        this.role = role;
        this.billing = billing;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public CreditRole getRole() {
        return role;
    }

    public void setRole(CreditRole role) {
        this.role = role;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public int getBilling() {
        return billing;
    }

    public void setBilling(int billing) {
        this.billing = billing;
    }

    @Override
    public String toString() {
        return person + " (" + role + ")";
    }
}
