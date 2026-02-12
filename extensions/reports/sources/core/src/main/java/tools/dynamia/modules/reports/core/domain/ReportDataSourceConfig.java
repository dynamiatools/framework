package tools.dynamia.modules.reports.core.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import tools.dynamia.modules.saas.jpa.SimpleEntitySaaS;

@Entity
@Table(name = "rpt_datasources")
public class ReportDataSourceConfig extends SimpleEntitySaaS {

    @NotNull
    private String name;

    @NotEmpty
    private String url;
    private String username;
    private String password;
    @NotEmpty
    private String driverClassName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    @Override
    public String toString() {
        return name;
    }
}
