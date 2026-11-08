package tools.dynamia.zk.ui;

import org.junit.jupiter.api.Test;
import tools.dynamia.ui.files.DownloadSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZKFileTransferPreviewTest {

    @Test
    void aSmallPdfIsShownInline() {
        assertTrue(ZKFileTransfer.canShowInline(new DownloadSource.BytesSource("a.pdf", "application/pdf", new byte[10])));
    }

    @Test
    void activeTypesAreDownloaded() {
        assertFalse(ZKFileTransfer.canShowInline(new DownloadSource.BytesSource("a.html", "text/html", new byte[10])));
        assertFalse(ZKFileTransfer.canShowInline(new DownloadSource.BytesSource("a.svg", "image/svg+xml", new byte[10])));
    }

    @Test
    void aFileOverTheLimitIsDownloaded() {
        var big = new DownloadSource.StreamSource("big.pdf", "application/pdf", ZKFileTransfer.PREVIEW_MAX_BYTES + 1, () -> null);
        assertFalse(ZKFileTransfer.canShowInline(big));
    }
}
