package tools.dynamia.demos.movies.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import tools.dynamia.domain.OrderBy;
import tools.dynamia.domain.jpa.BaseEntity;

/** Actors, directors, writers and composers. */
@Entity
@Table(name = "people")
@OrderBy("name")
public class Person extends BaseEntity {

    @NotEmpty
    private String name;
    private Integer birthYear;
    private String nationality;
    @Column(length = 2000)
    private String biography;
    private String photoUrl;

    public Person() {
    }

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Override
    public String toString() {
        return name;
    }
}
