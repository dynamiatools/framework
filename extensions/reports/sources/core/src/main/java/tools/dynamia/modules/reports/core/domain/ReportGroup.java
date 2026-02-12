package tools.dynamia.modules.reports.core.domain;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tools.dynamia.commons.StringUtils;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.AccountServiceAPI;
import tools.dynamia.modules.saas.jpa.SimpleEntitySaaS;

import java.util.List;

@Entity
@Table(name = "rpt_groups")
@Cacheable
@JsonFilter("ignoreIds")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportGroup extends SimpleEntitySaaS {


    private String name;
    private String module;
    private boolean active = true;
    private String endpointName;
    @Column(name = "is_system")
    private boolean system;

    public ReportGroup() {
    }

    public ReportGroup(String name, String module, boolean active) {
        this.name = name;
        this.module = module;
        this.active = active;
    }

    @Override
    public String toString() {
        return name;
    }

    public static List<ReportGroup> findActives() {
        AccountServiceAPI accountsApi = Containers.get().findObject(AccountServiceAPI.class);

        return DomainUtils.lookupCrudService().find(ReportGroup.class, QueryParameters.with("active", true)
                .add("accountId", accountsApi.getSystemAccountId())
                .orderBy("name"));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public boolean getActive() {
        return active;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        if (endpointName != null) {
            this.endpointName = StringUtils.simplifiedString(endpointName);
        } else {
            this.endpointName = null;
        }
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }
}
