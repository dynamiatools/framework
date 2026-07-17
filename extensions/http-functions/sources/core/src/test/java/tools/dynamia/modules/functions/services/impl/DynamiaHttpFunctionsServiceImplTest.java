package tools.dynamia.modules.functions.services.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.modules.functions.FunctionExecutionException;
import tools.dynamia.modules.functions.FunctionInactiveException;
import tools.dynamia.modules.functions.FunctionNotFoundException;
import tools.dynamia.modules.functions.FunctionResult;
import tools.dynamia.modules.functions.domain.DynamiaHttpFunction;
import tools.dynamia.modules.functions.domain.DynamiaHttpFunctionParameter;
import tools.dynamia.modules.functions.domain.enums.FunctionStatus;
import tools.dynamia.modules.functions.domain.enums.ParameterDataType;
import tools.dynamia.web.HttpMethod;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Exercises {@link DynamiaHttpFunctionsServiceImpl} end to end: version resolution, parameter
 * validation, template rendering and response parsing. Outbound HTTP calls are mocked with
 * {@link MockRestServiceServer} (no real network access), and {@link CrudService} is mocked with
 * Mockito and installed into {@link Containers} so the inherited {@code crudService()} lookup resolves
 * to it.
 */
class DynamiaHttpFunctionsServiceImplTest {

    private DynamiaHttpFunctionsServiceImpl service;
    private CrudService crudService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        crudService = mock(CrudService.class);
        SimpleObjectContainer container = new SimpleObjectContainer("test-container");
        container.addObject(crudService);
        Containers.get().installObjectContainer(container);

        service = new DynamiaHttpFunctionsServiceImpl();

        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        service.setRestClient(builder.build());
    }

    @AfterEach
    void tearDown() {
        Containers.get().removeAllContainers();
    }

    @Test
    void callRendersTemplatesAndReturnsJsonPayload() {
        DynamiaHttpFunction function = whatsAppFunction(FunctionStatus.ACTIVE, 1);
        function.setHeaders("Authorization: Bearer ${apiKey}");
        whenResolvingLatestVersion(function);

        mockServer.expect(requestTo("https://api.example.com/send"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer secret-key"))
                .andExpect(content().string("{\"to\":\"123456789\",\"text\":\"Hello\"}"))
                .andRespond(withSuccess("{\"messageId\":\"abc\"}", MediaType.APPLICATION_JSON));

        FunctionResult result = service.call("WhatsApp.sendMessage",
                Map.of("number", "123456789", "message", "Hello", "apiKey", "secret-key"));

        mockServer.verify();
        assertTrue(result.isSuccess());
        assertTrue(result.isJson());
        assertEquals("abc", result.toJson().get("messageId"));
    }

    @Test
    void callUsesParameterDefaultValueWhenNotProvided() {
        DynamiaHttpFunction function = whatsAppFunction(FunctionStatus.ACTIVE, 1);
        function.getParameter("message").setDefaultValue("Default greeting");
        function.getParameter("message").setRequired(false);
        whenResolvingLatestVersion(function);

        mockServer.expect(requestTo("https://api.example.com/send"))
                .andExpect(content().string("{\"to\":\"123456789\",\"text\":\"Default greeting\"}"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        service.call("WhatsApp.sendMessage", Map.of("number", "123456789"));

        mockServer.verify();
    }

    @Test
    void callReturnsBinaryResultWhenResponseIsNotJson() {
        DynamiaHttpFunction function = whatsAppFunction(FunctionStatus.ACTIVE, 1);
        whenResolvingLatestVersion(function);

        byte[] pdfBytes = {0x25, 0x50, 0x44, 0x46};
        mockServer.expect(requestTo("https://api.example.com/send"))
                .andRespond(withSuccess(pdfBytes, MediaType.APPLICATION_PDF));

        FunctionResult result = service.call("WhatsApp.sendMessage", Map.of("number", "123", "message", "Hi"));

        assertTrue(result.isBinary());
        assertFalse(result.isJson());
        assertEquals(MediaType.APPLICATION_PDF_VALUE, result.getContentType());
        assertEquals(4, result.getBinaryData().length);
    }

    @Test
    void callThrowsFunctionNotFoundExceptionWhenFunctionDoesNotExist() {
        when(crudService.find(eq(DynamiaHttpFunction.class), any(QueryParameters.class))).thenReturn(List.of());

        assertThrows(FunctionNotFoundException.class,
                () -> service.call("Unknown.function", Map.of()));

        verify(crudService, never()).create(any());
    }

    @Test
    void callThrowsFunctionInactiveExceptionWhenFunctionIsDraft() {
        DynamiaHttpFunction function = whatsAppFunction(FunctionStatus.DRAFT, 1);
        whenResolvingLatestVersion(function);

        assertThrows(FunctionInactiveException.class,
                () -> service.call("WhatsApp.sendMessage", Map.of("number", "123", "message", "Hi")));
    }

    @Test
    void callThrowsValidationErrorWhenRequiredParameterIsMissing() {
        DynamiaHttpFunction function = whatsAppFunction(FunctionStatus.ACTIVE, 1);
        whenResolvingLatestVersion(function);

        assertThrows(ValidationError.class,
                () -> service.call("WhatsApp.sendMessage", Map.of("number", "123456789")));
    }

    @Test
    void callWithAutoCreateRegistersDraftAndThrowsInactiveInsteadOfNotFound() {
        when(crudService.find(eq(DynamiaHttpFunction.class), any(QueryParameters.class))).thenReturn(List.of());
        when(crudService.create(any(DynamiaHttpFunction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(FunctionInactiveException.class,
                () -> service.call("New.integration", Map.of(), true));

        verify(crudService).create(any(DynamiaHttpFunction.class));
    }

    @Test
    void callWithAutoCreateFalseThrowsNotFoundInsteadOfCreating() {
        when(crudService.find(eq(DynamiaHttpFunction.class), any(QueryParameters.class))).thenReturn(List.of());

        assertThrows(FunctionNotFoundException.class,
                () -> service.call("New.integration", Map.of(), false));

        verify(crudService, never()).create(any());
    }

    @Test
    void callWrapsHttpFailureAsFunctionExecutionException() {
        DynamiaHttpFunction function = whatsAppFunction(FunctionStatus.ACTIVE, 1);
        whenResolvingLatestVersion(function);

        mockServer.expect(requestTo("https://api.example.com/send"))
                .andRespond(withServerError());

        assertThrows(FunctionExecutionException.class,
                () -> service.call("WhatsApp.sendMessage", Map.of("number", "123", "message", "Hi")));
    }

    @Test
    void callResolvesExplicitVersion() {
        DynamiaHttpFunction v2 = whatsAppFunction(FunctionStatus.ACTIVE, 2);
        when(crudService.findSingle(eq(DynamiaHttpFunction.class), any(QueryParameters.class))).thenReturn(v2);

        mockServer.expect(requestTo("https://api.example.com/send"))
                .andRespond(withStatus(HttpStatus.OK).body("{}").contentType(MediaType.APPLICATION_JSON));

        FunctionResult result = service.call("WhatsApp.sendMessage", 2, Map.of("number", "123", "message", "Hi"));

        assertTrue(result.isSuccess());
        verify(crudService).findSingle(eq(DynamiaHttpFunction.class), any(QueryParameters.class));
    }

    private void whenResolvingLatestVersion(DynamiaHttpFunction function) {
        when(crudService.find(eq(DynamiaHttpFunction.class), any(QueryParameters.class)))
                .thenReturn(List.of(function));
    }

    private DynamiaHttpFunction whatsAppFunction(FunctionStatus status, int version) {
        DynamiaHttpFunction function = new DynamiaHttpFunction();
        function.setName("WhatsApp.sendMessage");
        function.setFunctionVersion(version);
        function.setStatus(status);
        function.setMethod(HttpMethod.POST);
        function.setUrl("https://api.example.com/send");
        function.setContentType("application/json");
        function.setBodyTemplate("{\"to\":\"${number}\",\"text\":\"${message}\"}");

        function.addParameter(parameter("number", ParameterDataType.STRING, true));
        function.addParameter(parameter("message", ParameterDataType.STRING, true));
        return function;
    }

    private DynamiaHttpFunctionParameter parameter(String name, ParameterDataType type, boolean required) {
        DynamiaHttpFunctionParameter parameter = new DynamiaHttpFunctionParameter();
        parameter.setName(name);
        parameter.setType(type);
        parameter.setRequired(required);
        return parameter;
    }
}
