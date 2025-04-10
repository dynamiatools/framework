package tools.dynamia.crud;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import tools.dynamia.domain.jpa.SimpleEntity;

import java.time.LocalDate;

@Entity
public class TestEntity extends SimpleEntity {

    private String name;
    private LocalDate date;
    private String description;
    private String notes;

    @ManyToOne
    private TestSubentity subentity;

    @ManyToOne
    private TestEntity parent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public TestSubentity getSubentity() {
        return subentity;
    }

    public void setSubentity(TestSubentity subentity) {
        this.subentity = subentity;
    }

    public TestEntity getParent() {
        return parent;
    }

    public void setParent(TestEntity parent) {
        this.parent = parent;
    }
}
