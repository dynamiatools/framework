package tools.dynamia.actions.replay;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.dynamia.actions.ActionExecutionRequest;
import tools.dynamia.actions.ActionExecutionResponse;
import tools.dynamia.actions.ActionFlowStepType;
import tools.dynamia.integration.Containers;
import tools.dynamia.integration.SimpleObjectContainer;
import tools.dynamia.ui.UIFiles;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.ui.files.FlowPrincipal;
import tools.dynamia.ui.files.InMemoryTransferStore;
import tools.dynamia.ui.files.TransferMeta;
import tools.dynamia.ui.files.TransferRef;
import tools.dynamia.ui.files.UploadRejectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An action written against {@link UIFiles} runs unchanged in a headless run: the upload is a step answered with
 * references to files in the transfer store, the download is a URL in the final response. No bytes travel in the flow.
 */
class ReplayFileTransferTest {

    private final List<String> imported = new ArrayList<>();
    private final InMemoryTransferStore store = new InMemoryTransferStore();
    private final AtomicReference<FlowPrincipal> principal = new AtomicReference<>(FlowPrincipal.ANONYMOUS);

    @BeforeEach
    void install() {
        var beans = new SimpleObjectContainer("replay-files-test");
        beans.addObject(store);
        beans.addObject((FlowPrincipal) new FlowPrincipal() {
            @Override
            public String subject() {
                return principal.get().subject();
            }

            @Override
            public String tenant() {
                return principal.get().tenant();
            }
        });
        Containers.get().installObjectContainer(beans);
    }

    @AfterEach
    void uninstall() {
        Containers.get().removeAllContainers();
    }

    private static FlowPrincipal user(String subject, String tenant) {
        return new FlowPrincipal() {
            @Override
            public String subject() {
                return subject;
            }

            @Override
            public String tenant() {
                return tenant;
            }
        };
    }

    private TransferRef sent(String name, String text) {
        return sent(name, text.getBytes(StandardCharsets.UTF_8));
    }

    private TransferRef sent(String name, byte[] content) {
        return store.put(new ByteArrayInputStream(content),
                TransferMeta.of(name, "application/json", TransferMeta.Direction.UPLOAD, FlowPrincipal.current()), Long.MAX_VALUE);
    }

    private ActionExecutionRequest answer(ActionExecutionResponse pending, Object answer) {
        var request = new ActionExecutionRequest();
        request.setResumeToken(pending.getFlow().getResumeToken());
        request.setData(answer);
        return request;
    }

    private Object importLike(ActionExecutionRequest request) {
        UIFiles.uploadOne(".json", file -> {
            try (var in = file.openStream()) {
                imported.add(file.name() + ":" + new String(in.readAllBytes(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
            UIMessages.showMessage("Imported OK");
        });
        return null;
    }

    @Test
    void uploadIsAStepAnsweredWithReferencesAndTheTokenStaysSmall() {
        var pending = ReplayExecutor.execute("import", new ActionExecutionRequest(), this::importLike);

        assertEquals("PENDING", pending.getStatus());
        assertEquals(ActionFlowStepType.UPLOAD, pending.getFlow().getType());
        var data = (Map<?, ?>) pending.getFlow().getData();
        assertEquals(".json", data.get("accept"));
        assertEquals(false, data.get("multiple"));
        assertEquals(100L * 1024 * 1024, data.get("maxFileSize"));
        assertTrue(imported.isEmpty());

        var ref = sent("report.json", "{}");
        var done = ReplayExecutor.execute("import", answer(pending, List.of(Map.of("ref", ref.ref()))), this::importLike);

        assertEquals("SUCCESS", done.getStatus());
        assertEquals(List.of("report.json:{}"), imported);
        assertEquals("Imported OK", done.getFlow().getMessage());
    }

    @Test
    void aBigFileTravelsAsAReferenceNotInsideTheToken() {
        var big = new byte[5 * 1024 * 1024];
        java.util.Arrays.fill(big, (byte) 'x');
        var ref = sent("big.json", big);
        java.util.function.Function<ActionExecutionRequest, Object> body = request -> {
            UIFiles.uploadOne(".json", file -> UIMessages.showQuestion("Import " + file.size() + " bytes?", () ->
                    imported.add(file.name())));
            return null;
        };

        var pending = ReplayExecutor.execute("import", new ActionExecutionRequest(), body);
        var asking = ReplayExecutor.execute("import", answer(pending, List.of(Map.of("ref", ref.ref()))), body);

        assertEquals(ActionFlowStepType.CONFIRM, asking.getFlow().getType());
        assertTrue(asking.getFlow().getResumeToken().length() < 2048, "the token carries the reference only");
        var done = ReplayExecutor.execute("import", answer(asking, true), body);
        assertEquals(List.of("big.json"), imported);
        assertEquals("SUCCESS", done.getStatus());
    }

    @Test
    void theReferencesAreDeletedOnceTheFlowFinishes() {
        var ref = sent("report.json", "{}");
        var pending = ReplayExecutor.execute("import", new ActionExecutionRequest(), this::importLike);

        ReplayExecutor.execute("import", answer(pending, List.of(Map.of("ref", ref.ref()))), this::importLike);

        assertTrue(store.get(ref.ref(), FlowPrincipal.ANONYMOUS).isEmpty());
    }

    @Test
    void aReferenceOfAnotherUserOrTenantIsNotFound() {
        principal.set(user("luis", "1"));
        var luisRef = sent("report.json", "{}");
        principal.set(user("ana", "1"));
        var pending = ReplayExecutor.execute("import", new ActionExecutionRequest(), this::importLike);

        assertThrows(UploadRejectedException.class, () ->
                ReplayExecutor.execute("import", answer(pending, List.of(Map.of("ref", luisRef.ref()))), this::importLike));

        principal.set(user("ana", "2"));
        var anaOtherTenant = sent("report.json", "{}");
        principal.set(user("ana", "1"));
        assertThrows(UploadRejectedException.class, () ->
                ReplayExecutor.execute("import", answer(pending, List.of(Map.of("ref", anaOtherTenant.ref()))), this::importLike));
        assertTrue(imported.isEmpty());
    }

    @Test
    void theServerEnforcesTheAcceptedTypes() {
        var ref = sent("report.exe", "MZ");
        var pending = ReplayExecutor.execute("import", new ActionExecutionRequest(), this::importLike);

        assertThrows(UploadRejectedException.class, () ->
                ReplayExecutor.execute("import", answer(pending, List.of(Map.of("ref", ref.ref()))), this::importLike));
    }

    @Test
    void anUnknownReferenceIsRejected() {
        var pending = ReplayExecutor.execute("import", new ActionExecutionRequest(), this::importLike);

        assertThrows(UploadRejectedException.class, () ->
                ReplayExecutor.execute("import", answer(pending, List.of(Map.of("ref", "nope"))), this::importLike));
    }

    @Test
    void cancellingTheUploadRunsNothing() {
        var pending = ReplayExecutor.execute("import", new ActionExecutionRequest(), this::importLike);
        var done = ReplayExecutor.execute("import", answer(pending, List.of()), this::importLike);

        assertEquals("SUCCESS", done.getStatus());
        assertTrue(imported.isEmpty());
    }

    @Test
    void downloadIsAUrlInTheResponseWithoutBase64() throws IOException {
        var done = ReplayExecutor.execute("export", new ActionExecutionRequest(), request -> {
            UIFiles.download("out.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8));
            return null;
        });

        assertEquals("SUCCESS", done.getStatus());
        var downloads = (List<?>) done.getParams().get(ReplayExecutor.DOWNLOADS_PARAM);
        var file = (Map<?, ?>) downloads.get(0);
        assertEquals("out.txt", file.get("name"));
        assertEquals("text/plain", file.get("contentType"));
        assertEquals(5L, file.get("size"));
        assertFalse(file.containsKey("content"));
        var url = (String) file.get("url");
        assertTrue(url.startsWith(ReplayExecutor.TRANSFERS_PATH + "/"));
        var stored = store.get(url.substring(url.lastIndexOf('/') + 1), FlowPrincipal.ANONYMOUS).orElseThrow();
        try (var in = stored.openStream()) {
            assertEquals("hello", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void aStreamedDownloadIsReadOnlyByThePassThatFinishes() {
        var opened = new int[1];
        java.util.function.Function<ActionExecutionRequest, Object> body = request -> {
            UIFiles.download("out.bin", "application/octet-stream", () -> {
                opened[0]++;
                return new ByteArrayInputStream(new byte[]{1, 2, 3});
            });
            UIMessages.showQuestion("Sure?", () -> {
            });
            return null;
        };

        var pending = ReplayExecutor.execute("export", new ActionExecutionRequest(), body);
        assertNull(pending.getParams(), "the pass that stopped at a question delivers nothing");
        assertEquals(0, opened[0], "and does not read the source");

        var done = ReplayExecutor.execute("export", answer(pending, true), body);
        assertEquals(1, ((List<?>) done.getParams().get(ReplayExecutor.DOWNLOADS_PARAM)).size(), "delivered once");
        assertEquals(1, opened[0], "read once");
    }

    @Test
    void withoutATransferStoreTheErrorSaysSo() {
        Containers.get().removeAllContainers();

        var failure = assertThrows(IllegalStateException.class, () ->
                ReplayExecutor.execute("export", new ActionExecutionRequest(), request -> {
                    UIFiles.download("a.txt", null, new byte[0]);
                    return null;
                }));

        assertTrue(failure.getMessage().contains("TransferStore"));
    }
}
