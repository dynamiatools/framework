package tools.dynamia.modules.functions.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import tools.dynamia.modules.functions.domain.enums.FunctionStatus;
import tools.dynamia.modules.saas.jpa.BaseEntitySaaS;
import tools.dynamia.web.HttpMethod;

import java.util.ArrayList;
import java.util.List;

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
    private String interfaceName; // For dynamic interfaces
    private String methodName; // For dynamic interfaces
    @NotNull
    @Enumerated(EnumType.STRING)
    private FunctionStatus status = FunctionStatus.DRAFT;

}
