package tools.dynamia.demos.movies.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import tools.dynamia.domain.OrderBy;
import tools.dynamia.domain.jpa.BaseEntity;

@Entity
@Table(name = "studios")
@OrderBy("name")
public class Studio extends BaseEntity {

    @NotEmpty
    private String name;
    private String country;
    private Integer foundedYear;

    public Studio() {
    }

    public Studio(String name, String country) {
        this.name = name;
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getFoundedYear() {
        return foundedYear;
    }

    public void setFoundedYear(Integer foundedYear) {
        this.foundedYear = foundedYear;
    }

    @Override
    public String toString() {
        return name;
    }
}
