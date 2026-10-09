package tools.dynamia.demos.movies.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import tools.dynamia.domain.OrderBy;
import tools.dynamia.domain.jpa.BaseEntity;

@Entity
@Table(name = "genres")
@OrderBy("name")
public class Genre extends BaseEntity {

    @NotEmpty
    private String name;
    private String description;

    public Genre() {
    }

    public Genre(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }
}
