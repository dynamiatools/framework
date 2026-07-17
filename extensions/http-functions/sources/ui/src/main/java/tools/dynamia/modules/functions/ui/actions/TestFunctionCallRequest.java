package tools.dynamia.modules.functions.ui.actions;

/**
 * Backing bean for the "Test function" dialog: lets the operator pick the version to call and edit the
 * call parameters as raw JSON before invoking {@link tools.dynamia.modules.functions.domain.DynamiaHttpFunction}.
 *
 * @author Mario A. Serrano Leones
 */
public class TestFunctionCallRequest {

    private Integer version;
    private String parametersJson = "{}";

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getParametersJson() {
        return parametersJson;
    }

    public void setParametersJson(String parametersJson) {
        this.parametersJson = parametersJson;
    }
}
