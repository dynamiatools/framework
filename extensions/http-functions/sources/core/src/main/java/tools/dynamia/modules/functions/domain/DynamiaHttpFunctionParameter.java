package tools.dynamia.modules.functions.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import tools.dynamia.modules.functions.domain.enums.ParameterDataType;
import tools.dynamia.modules.saas.jpa.SimpleEntitySaaS;

@Entity
@Table(name = "fx_functions_parameters")
public class DynamiaHttpFunctionParameter extends SimpleEntitySaaS {

    @ManyToOne
    private DynamiaHttpFunction function;
    @NotNull
    private String name;
    private boolean required;
    private String description;
    private String defaultValue;
    @NotNull
    @Enumerated(jakarta.persistence.EnumType.STRING)
    private ParameterDataType type = ParameterDataType.STRING;
    private int position;

    public DynamiaHttpFunction getFunction() {
        return function;
    }

    public void setFunction(DynamiaHttpFunction function) {
        this.function = function;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ParameterDataType getType() {
        return type;
    }

    public void setType(ParameterDataType type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return name + (required ? " (required)" : "") + " - " + type;
    }
}