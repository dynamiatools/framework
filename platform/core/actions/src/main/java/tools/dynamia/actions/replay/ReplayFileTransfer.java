package tools.dynamia.actions.replay;

import tools.dynamia.actions.ActionFlowStep;
import tools.dynamia.integration.Containers;
import tools.dynamia.ui.FileTransfer;
import tools.dynamia.ui.files.DownloadSource;
import tools.dynamia.ui.files.FlowPrincipal;
import tools.dynamia.ui.files.StoredTransfer;
import tools.dynamia.ui.files.TransferStore;
import tools.dynamia.ui.files.UploadOptions;
import tools.dynamia.ui.files.UploadPolicy;
import tools.dynamia.ui.files.UploadRejectedException;
import tools.dynamia.ui.files.UploadedFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * {@link FileTransfer} of a headless run. Nothing travels inside the flow: an upload is an {@code UPLOAD} step the client
 * answers with references to files it already sent to {@code /api/app/transfers}, which are resolved in the
 * {@link TransferStore} (owner, expiry, accepted types and sizes) and handed to the action as streaming handles. A
 * download is recorded in the {@link ReplaySession} as a {@link DownloadSource} and only written to the store by the pass
 * that ends, so passes that are later discarded never read it.
 */
public final class ReplayFileTransfer implements FileTransfer {

    private final ReplaySession session;

    /**
     * @param session the session of the pass
     */
    public ReplayFileTransfer(ReplaySession session) {
        this.session = session;
    }

    @Override
    public void download(DownloadSource source) {
        session.download(source);
    }

    @Override
    public void upload(UploadOptions options, Consumer<List<UploadedFile>> onFiles) {
        session.interact(ActionFlowStep.upload(options.title(), options.accept(), options.maxFiles(),
                options.maxFileSize(), options.maxTotalSize()), answer -> {
            var files = resolve(options, answer);
            if (!files.isEmpty()) {
                onFiles.accept(files);
            }
        });
    }

    private List<UploadedFile> resolve(UploadOptions options, Object answer) {
        var items = new ArrayList<Object>();
        if (answer instanceof List<?> list) {
            items.addAll(list);
        } else if (answer != null) {
            items.add(answer);
        }
        if (items.isEmpty()) {
            return List.of();
        }
        TransferStore store = Containers.get().findObject(TransferStore.class);
        if (store == null) {
            throw new IllegalStateException("No TransferStore is registered: uploads from remote clients need one");
        }
        FlowPrincipal owner = FlowPrincipal.current();
        var stored = new ArrayList<StoredTransfer>();
        for (Object item : items) {
            String ref = item instanceof Map<?, ?> map ? String.valueOf(map.get("ref")) : String.valueOf(item);
            stored.add(store.get(ref, owner).orElseThrow(() ->
                    new UploadRejectedException("The uploaded file " + ref + " does not exist or has expired")));
        }
        var files = stored.stream().map(StoredTransfer::asUploadedFile).toList();
        UploadPolicy.check(options, files);
        stored.forEach(s -> session.consume(s.ref().ref()));
        return files;
    }
}
