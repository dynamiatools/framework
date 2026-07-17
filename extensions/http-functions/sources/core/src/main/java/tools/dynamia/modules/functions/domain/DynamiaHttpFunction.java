package tools.dynamia.modules.functions.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import tools.dynamia.modules.functions.domain.enums.FunctionStatus;
import tools.dynamia.modules.saas.jpa.BaseEntitySaaS;
import tools.dynamia.web.HttpMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a versioned HTTP function definition. A function describes how to reach and invoke an
 * external service (or internal endpoint) as if it was a normal, reusable, versioned capability that
 * can be called from code through {@link tools.dynamia.modules.functions.DynamiaFunctions}.
 * <p>
 * A function is uniquely identified by the pair {@code (name, functionVersion)} within an account.
 * When multiple versions of the same function exist, callers can request an explicit version or let
 * the runtime resolve the highest active version automatically.
 *
 * Example:
 * <pre>{@code
 * DynamiaHttpFunction function = new DynamiaHttpFunction();
 * function.setName("WhatsApp.sendMessage");
 * function.setFunctionVersion(1);
 * function.setMethod(HttpMethod.POST);
 * function.setUrl("https://api.whatsapp.example.com/send");
 * function.setBodyTemplate("{\"to\":\"${number}\",\"text\":\"${message}\"}");
 * function.setStatus(FunctionStatus.ACTIVE);
 * }</pre>
 *
 * @author Mario A. Serrano Leones
 */
@Entity
@Table(name = "fx_functions", indexes = {
        @Index(name = "idx_fx_function_account", columnList = "accountId,name,functionVersion", unique = true)
})
public class DynamiaHttpFunction extends BaseEntitySaaS {

    @NotNull
    @NotEmpty
    @Column(length = 1000)
    private String name;
    private String description;
    @Min(1)
    private int functionVersion = 1;
    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    private List<DynamiaHttpFunctionParameter> parameters = new ArrayList<>();
    @Enumerated(EnumType.STRING)
    @NotNull
    private HttpMethod method = HttpMethod.POST;
    @Column(length = 1000)
    private String url;
    private String contentType = "application/json";
    @Lob
    private String bodyTemplate;
    /**
     * Optional raw HTTP headers, one per line, using the {@code Header-Name: value} syntax. Values
     * support {@code ${paramName}} placeholders that are resolved with the call parameters, so
     * integrations that require API keys, tokens or other custom headers can be configured declaratively.
     */
    @Lob
    private String headers;
    /**
     * Optional free-form JSON metadata associated with the function definition (e.g. provider name,
     * documentation links, rate limits). Not used by the execution engine, purely informative/extensible.
     */
    @Lob
    private String metadata;
    private String interfaceName; // For dynamic interfaces
    private String methodName; // For dynamic interfaces
    @NotNull
    @Enumerated(EnumType.STRING)
    private FunctionStatus status = FunctionStatus.DRAFT;

    /**
     * Finds a parameter definition by name.
     *
     * @param name the parameter name
     * @return the matching parameter definition, or {@code null} if not found
     */
    public DynamiaHttpFunctionParameter getParameter(String name) {
        if (name == null) {
            return null;
        }
        return parameters.stream()
                .filter(p -> name.equals(p.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Adds a parameter definition to this function, wiring the back-reference automatically.
     *
     * @param parameter the parameter definition to add
     */
    public void addParameter(DynamiaHttpFunctionParameter parameter) {
        parameter.setFunction(this);
        parameters.add(parameter);
    }

    public boolean isActive() {
        return status == FunctionStatus.ACTIVE;
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

    public int getFunctionVersion() {
        return functionVersion;
    }

    public void setFunctionVersion(int functionVersion) {
        this.functionVersion = functionVersion;
    }

    public List<DynamiaHttpFunctionParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<DynamiaHttpFunctionParameter> parameters) {
        this.parameters = parameters;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public FunctionStatus getStatus() {
        return status;
    }

    public void setStatus(FunctionStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return name + " v" + functionVersion;
    }
}