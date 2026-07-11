package tools.dynamia.modules.functions.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.modules.functions.FunctionExecutionException;
import tools.dynamia.modules.functions.FunctionInactiveException;
import tools.dynamia.modules.functions.FunctionNotFoundException;
import tools.dynamia.modules.functions.FunctionResult;
import tools.dynamia.modules.functions.services.DynamiaHttpFunctionsService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies the HTTP status/body mapping performed by {@link DynamiaHttpFunctionsController}. The
 * underlying {@link DynamiaHttpFunctionsService} is mocked so no real function execution/HTTP call
 * happens here; only the controller's request/response translation is under test.
 */
@ExtendWith(MockitoExtension.class)
class DynamiaHttpFunctionsControllerTest {

    @Mock
    private DynamiaHttpFunctionsService functionsService;

    private DynamiaHttpFunctionsController controller;

    @BeforeEach
    void setUp() {
        controller = new DynamiaHttpFunctionsController(functionsService);
    }

    @Test
    void returnsOkWithJsonPayloadOnSuccess() {
        when(functionsService.call(eq("WhatsApp.sendMessage"), isNull(), any()))
                .thenReturn(FunctionResult.success(Map.of("messageId", "abc")));

        ResponseEntity<?> response = controller.call("WhatsApp.sendMessage", null, null, requestWith(Map.of("number", "123")));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("success", true, "data", Map.of("messageId", "abc")), response.getBody());
    }

    @Test
    void returnsOkWithBinaryBodyWhenResultIsBinary() {
        byte[] bytes = {1, 2, 3};
        when(functionsService.call(eq("Reports.export"), isNull(), any()))
                .thenReturn(FunctionResult.binary(bytes, "application/pdf"));

        ResponseEntity<?> response = controller.call("Reports.export", null, null, requestWith(Map.of()));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertArrayEquals(bytes, (byte[]) response.getBody());
    }

    @Test
    void returnsNotFoundWhenFunctionDoesNotExist() {
        when(functionsService.call(eq("Missing.function"), isNull(), any()))
                .thenThrow(new FunctionNotFoundException("Missing.function", null));

        ResponseEntity<?> response = controller.call("Missing.function", null, null, requestWith(Map.of()));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse((Boolean) ((Map<?, ?>) response.getBody()).get("success"));
    }

    @Test
    void returnsNotFoundWhenFunctionIsInactive() {
        when(functionsService.call(eq("Draft.function"), isNull(), any()))
                .thenThrow(new FunctionInactiveException("Draft.function", 1));

        ResponseEntity<?> response = controller.call("Draft.function", null, null, requestWith(Map.of()));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void returnsBadRequestOnValidationError() {
        when(functionsService.call(eq("WhatsApp.sendMessage"), isNull(), any()))
                .thenThrow(new ValidationError("Parameter [number] is required"));

        ResponseEntity<?> response = controller.call("WhatsApp.sendMessage", null, null, requestWith(Map.of()));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void returnsInternalServerErrorOnExecutionFailure() {
        when(functionsService.call(eq("WhatsApp.sendMessage"), isNull(), any()))
                .thenThrow(new FunctionExecutionException("WhatsApp.sendMessage", 1, new RuntimeException("boom")));

        ResponseEntity<?> response = controller.call("WhatsApp.sendMessage", null, null, requestWith(Map.of()));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void returnsInternalServerErrorOnUnexpectedException() {
        when(functionsService.call(eq("WhatsApp.sendMessage"), isNull(), any()))
                .thenThrow(new RuntimeException("unexpected"));

        ResponseEntity<?> response = controller.call("WhatsApp.sendMessage", null, null, requestWith(Map.of()));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void versionHeaderTakesPrecedenceOverQueryParam() {
        when(functionsService.call(eq("WhatsApp.sendMessage"), eq(2), any()))
                .thenReturn(FunctionResult.success(Map.of()));

        ResponseEntity<?> response = controller.call("WhatsApp.sendMessage", 2, 5, requestWith(Map.of()));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(functionsService).call(eq("WhatsApp.sendMessage"), eq(2), any());
    }

    @Test
    void queryParamIsUsedWhenVersionHeaderIsAbsent() {
        when(functionsService.call(eq("WhatsApp.sendMessage"), eq(5), any()))
                .thenReturn(FunctionResult.success(Map.of()));

        ResponseEntity<?> response = controller.call("WhatsApp.sendMessage", null, 5, requestWith(Map.of()));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(functionsService).call(eq("WhatsApp.sendMessage"), eq(5), any());
    }

    @Test
    void nullRequestBodyIsTreatedAsEmptyParams() {
        when(functionsService.call(eq("WhatsApp.sendMessage"), isNull(), eq(Map.of())))
                .thenReturn(FunctionResult.success(Map.of()));

        ResponseEntity<?> response = controller.call("WhatsApp.sendMessage", null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private DynamiaHttpFunctionsController.FunctionCallRequest requestWith(Map<String, Object> params) {
        DynamiaHttpFunctionsController.FunctionCallRequest request = new DynamiaHttpFunctionsController.FunctionCallRequest();
        request.setParams(params);
        return request;
    }
}
