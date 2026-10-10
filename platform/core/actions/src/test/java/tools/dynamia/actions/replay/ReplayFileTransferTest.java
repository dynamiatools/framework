package tools.dynamia.actions.replay;

import org.junit.jupiter.api.Test;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.ActionFlowStepType;
import tools.dynamia.ui.UIFiles;
import tools.dynamia.ui.UIMessages;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An action written against {@link UIFiles} runs unchanged in a headless run: the upload becomes a step, the download
 * travels with the response.
 */
class ReplayFileTransferTest {

    private final List<String> imported = new ArrayList<>();

    private static String b64(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private ActionExecutionRequest answer(ActionExecutionResponse pending, Object answer) {
        var request = new ActionExecutionRequest();
        request.setResumeToken(pending.getFlow().getResumeToken());
        request.setData(answer);
        return request;
    }

    private Object importLike(ActionExecutionRequest request) {
        UIFiles.uploadOne(".json", file -> {
            imported.add(file.name() + ":" + new String(file.content(), StandardCharsets.UTF_8));
            UIMessages.showMessage("Imported OK");
        });
        return null;
    }

    @Test
    void uploadIsAStepAndTheFilesReachTheCallback() {
        var pending = ReplayExecutor.execute("import", new ActionExecutionRequest(), this::importLike);

        assertEquals("PENDING", pending.getStatus());
        assertEquals(ActionFlowStepType.UPLOAD, pending.getFlow().getType());
        var data = (Map<?, ?>) pending.getFlow().getData();
        assertEquals(".json", data.get("accept"));
        assertEquals(false, data.get("multiple"));
        assertTrue(imported.isEmpty());

        var file = Map.of("name", "report.json", "contentType", "application/json", "content", b64("{}"));
        var done = ReplayExecutor.execute("import", answer(pending, List.of(file)), this::importLike);

        assertEquals("SUCCESS", done.getStatus());
        assertEquals(List.of("report.json:{}"), imported);
        assertEquals("Imported OK", done.getFlow().getMessage());
    }

    @Test
    void cancellingTheUploadRunsNothing() {
        var pending = ReplayExecutor.execute("import", new ActionExecutionRequest(), this::importLike);
        var done = ReplayExecutor.execute("import", answer(pending, List.of()), this::importLike);

        assertEquals("SUCCESS", done.getStatus());
        assertTrue(imported.isEmpty());
    }

    @Test
    void downloadTravelsWithTheResponse() {
        var done = ReplayExecutor.execute("export", new ActionExecutionRequest(), request -> {
            UIFiles.download("out.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8));
            return null;
        });

        assertEquals("SUCCESS", done.getStatus());
        var downloads = (List<?>) done.getParams().get(ReplayExecutor.DOWNLOADS_PARAM);
        var file = (Map<?, ?>) downloads.get(0);
        assertEquals("out.txt", file.get("name"));
        assertEquals("text/plain", file.get("contentType"));
        assertEquals(b64("hello"), file.get("content"));
    }

    @Test
    void aDownloadBeforeAQuestionIsOnlySentByThePassThatFinishes() {
        java.util.function.Function<ActionExecutionRequest, Object> body = request -> {
            UIFiles.download("out.txt", "text/plain", new byte[]{1});
            UIMessages.showQuestion("Sure?", () -> {
            });
            return null;
        };

        var pending = ReplayExecutor.execute("export", new ActionExecutionRequest(), body);
        assertNull(pending.getParams(), "the pass that stopped at a question delivers nothing");

        var done = ReplayExecutor.execute("export", answer(pending, true), body);
        assertEquals(1, ((List<?>) done.getParams().get(ReplayExecutor.DOWNLOADS_PARAM)).size(), "delivered once");
    }

    @Test
    void bigFilesAreRejected() {
        var tooBig = new byte[ReplayFileTransfer.MAX_DOWNLOAD_BYTES + 1];
        assertThrows(IllegalArgumentException.class, () ->
                new ReplayFileTransfer(new ReplaySession(new ArrayList<>())).download("big.bin", null, tooBig));
    }
}
