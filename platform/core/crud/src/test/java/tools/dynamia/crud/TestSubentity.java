package tools.dynamia.crud;

import jakarta.persistence.Entity;
import tools.dynamia.commons.InstanceName;
import tools.dynamia.domain.jpa.SimpleEntity;

@Entity
public class TestSubentity extends SimpleEntity {

    @InstanceName
    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
