package tools.dynamia.modules.functions.ui.actions;

import org.zkoss.zul.Messagebox;
import tools.jackson.databind.ObjectMapper;
import tools.dynamia.actions.FastAction;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.commons.ApplicableClass;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.crud.CrudState;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.functions.FunctionExecutionException;
import tools.dynamia.modules.functions.FunctionInactiveException;
import tools.dynamia.modules.functions.FunctionNotFoundException;
import tools.dynamia.modules.functions.FunctionResult;
import tools.dynamia.modules.functions.domain.DynamiaHttpFunction;
import tools.dynamia.modules.functions.domain.DynamiaHttpFunctionParameter;
import tools.dynamia.modules.functions.services.DynamiaHttpFunctionsService;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.viewers.ViewDescriptor;
import tools.dynamia.zk.util.ZKUtil;
import tools.dynamia.zk.viewers.ui.Viewer;

import java.util.LinkedHashMap;
import java.util.Map;

import static tools.dynamia.viewers.ViewDescriptorBuilder.field;
import static tools.dynamia.viewers.ViewDescriptorBuilder.viewDescriptor;

/**
 * Crud table/toolbar action that lets an operator try a {@link DynamiaHttpFunction} on demand, editing
 * the call parameters as JSON and immediately seeing the {@link FunctionResult}, without leaving the
 * back office or writing any code.
 *
 * @author Mario A. Serrano Leones
 */
@InstallAction
public class TestHttpFunctionAction extends AbstractCrudAction {

    private final DynamiaHttpFunctionsService functionsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TestHttpFunctionAction(DynamiaHttpFunctionsService functionsService) {
        setName("Test");
        setDescription("Call this function with custom parameters and see the result");
        setImage("bolt");
        setMenuSupported(true);
        setApplicableStates(CrudState.get(CrudState.READ, CrudState.CREATE, CrudState.UPDATE));
        this.functionsService = functionsService;
    }

    @Override
    public CrudState[] getApplicableStates() {
        return CrudState.get(CrudState.READ);
    }

    @Override
    public ApplicableClass[] getApplicableClasses() {
        return ApplicableClass.get(DynamiaHttpFunction.class);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        DynamiaHttpFunction function = (DynamiaHttpFunction) evt.getData();
        if (function != null) {
            Viewer viewer = createView(function);
            ZKUtil.showDialog("Test Function: " + function.getName() + " v" + function.getFunctionVersion(), viewer, "60%", null);
        } else {
            UIMessages.showMessage("Select a function to test", MessageType.WARNING);
        }
    }

    private Viewer createView(DynamiaHttpFunction function) {
        ViewDescriptor descriptor = viewDescriptor("form", TestFunctionCallRequest.class, false)
                .id("testHttpFunctionForm")
                .fields(field("version"),
                        field("parametersJson")
                                .label("Parameters (JSON)")
                                .params("multiline", true, "height", "220px"))
                .layout("columns", 1)
                .build();

        Viewer viewer = new Viewer(descriptor);

        TestFunctionCallRequest request = new TestFunctionCallRequest();
        request.setVersion(function.getFunctionVersion());
        request.setParametersJson(sampleParametersJson(function));
        viewer.setValue(request);

        viewer.addAction(new FastAction("Run", evt -> runFunction(function, request)));
        viewer.setVflex(null);
        viewer.setContentVflex(null);
        return viewer;
    }

    private void runFunction(DynamiaHttpFunction function, TestFunctionCallRequest request) {
        try {
            Map<String, Object> params = parseParams(request.getParametersJson());
            FunctionResult result = functionsService.call(function.getName(), request.getVersion(), params);
            Messagebox.show(formatResult(result), "Function Result", Messagebox.OK,
                    result.isSuccess() ? Messagebox.INFORMATION : Messagebox.EXCLAMATION);
        } catch (FunctionNotFoundException | FunctionInactiveException | ValidationError e) {
            UIMessages.showMessage(e.getMessage(), MessageType.ERROR);
        } catch (FunctionExecutionException e) {
            UIMessages.showMessage(e.getMessage(), MessageType.ERROR);
        } catch (Exception e) {
            UIMessages.showMessage("Invalid parameters JSON: " + e.getMessage(), MessageType.ERROR);
        }
    }

    private Map<String, Object> parseParams(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        return objectMapper.readValue(json, Map.class);
    }

    private String formatResult(FunctionResult result) {
        if (!result.isSuccess()) {
            return "Error: " + result.getErrorMessage();
        }
        if (result.isBinary()) {
            return "Binary response (" + result.getContentType() + "), " + result.getBinaryData().length + " bytes";
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result.getData());
        } catch (Exception e) {
            return String.valueOf(result.getData());
        }
    }

    private String sampleParametersJson(DynamiaHttpFunction function) {
        Map<String, Object> sample = new LinkedHashMap<>();
        for (DynamiaHttpFunctionParameter parameter : function.getParameters()) {
            sample.put(parameter.getName(), parameter.getDefaultValue() != null ? parameter.getDefaultValue() : "");
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sample);
        } catch (Exception e) {
            return "{}";
        }
    }
}
