package tools.dynamia.actions;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionExecutionRequest {

    @JsonAlias({"data", "value", "payload", "content"})
    private Object data;
    @JsonAlias({"params", "parameters"})
    private Map<String, Object> params;
    private String source;

    @JsonAlias({"dataType", "type", "data-type", "className", "class"})
    private String dataType;

    @JsonAlias({"dataId", "id", "data-id"})
    private String dataId;

    @JsonAlias({"dataName", "name", "data-name"})
    private String dataName;

    public ActionExecutionRequest() {
    }

    public ActionExecutionRequest(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }
}
