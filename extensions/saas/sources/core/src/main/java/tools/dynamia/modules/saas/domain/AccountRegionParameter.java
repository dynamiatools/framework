package tools.dynamia.modules.saas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import tools.dynamia.domain.Descriptor;
import tools.dynamia.domain.jpa.SimpleEntity;

@Entity
@Table(name = "saas_regions_parameters")
@Descriptor(fields = {"name", "value"}, viewParams = "columns: 1")
public class AccountRegionParameter extends SimpleEntity {

    @ManyToOne
    private AccountRegion region;
    @NotNull
    private String name;
    @Column(length = 2000)
    @NotNull
    @Descriptor(params = {"multiline: true", "height: 100px"})
    private String value;

    public AccountRegion getRegion() {
        return region;
    }

    public void setRegion(AccountRegion region) {
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
