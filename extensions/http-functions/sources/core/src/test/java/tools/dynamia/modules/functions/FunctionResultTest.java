package tools.dynamia.modules.functions;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FunctionResultTest {

    @Test
    void successWithMapIsJsonAndNotBinary() {
        FunctionResult result = FunctionResult.success(Map.of("messageId", "abc"));

        assertTrue(result.isSuccess());
        assertFalse(result.isBinary());
        assertTrue(result.isJson());
        assertEquals("abc", result.toJson().get("messageId"));
    }

    @Test
    void successWithListIsJson() {
        FunctionResult result = FunctionResult.success(List.of("a", "b"));

        assertTrue(result.isJson());
        assertFalse(result.isBinary());
    }

    @Test
    void successWithPlainStringIsNotJsonButToJsonStillParsesIt() {
        FunctionResult result = FunctionResult.success("{\"messageId\":\"abc\"}");

        assertFalse(result.isJson());
        assertEquals("abc", result.toJson().get("messageId"));
    }

    @Test
    void toJsonWithNoDataReturnsEmptyMap() {
        FunctionResult result = FunctionResult.success(null);

        assertTrue(result.toJson().isEmpty());
    }

    @Test
    void toJsonWithClassConvertsMapToDto() {
        FunctionResult result = FunctionResult.success(Map.of("messageId", "abc", "delivered", true));

        SendMessageResponse response = result.toJson(SendMessageResponse.class);

        assertEquals("abc", response.messageId());
        assertTrue(response.delivered());
    }

    @Test
    void toJsonWithClassParsesRawJsonString() {
        FunctionResult result = FunctionResult.success("{\"messageId\":\"abc\",\"delivered\":false}");

        SendMessageResponse response = result.toJson(SendMessageResponse.class);

        assertEquals("abc", response.messageId());
        assertFalse(response.delivered());
    }

    @Test
    void toJsonWithClassReturnsNullWhenNoData() {
        FunctionResult result = FunctionResult.success(null);

        assertNull(result.toJson(SendMessageResponse.class));
    }

    @Test
    void binaryResultIsNotJsonAndCarriesRawBytes() {
        byte[] bytes = {1, 2, 3};
        FunctionResult result = FunctionResult.binary(bytes, "image/png");

        assertTrue(result.isSuccess());
        assertTrue(result.isBinary());
        assertFalse(result.isJson());
        assertEquals("image/png", result.getContentType());
        assertEquals(bytes, result.getBinaryData());
        assertNull(result.getData());
    }

    @Test
    void errorResultIsNotSuccessfulAndCarriesMessage() {
        FunctionResult result = FunctionResult.error("boom");

        assertFalse(result.isSuccess());
        assertFalse(result.isBinary());
        assertFalse(result.isJson());
        assertEquals("boom", result.getErrorMessage());
        assertNull(result.getData());
    }

    private record SendMessageResponse(String messageId, boolean delivered) {
    }
}
