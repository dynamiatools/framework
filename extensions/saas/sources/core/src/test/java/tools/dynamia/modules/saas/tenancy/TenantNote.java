package tools.dynamia.modules.saas.tenancy;

import jakarta.persistence.Entity;
import tools.dynamia.modules.saas.jpa.SimpleEntitySaaS;

@Entity
public class TenantNote extends SimpleEntitySaaS {

    private String text;

    public TenantNote() {
    }

    public TenantNote(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
