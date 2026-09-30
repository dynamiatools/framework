package mybookstore.handoff;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counts how many {@code NavigationManager.runLater} callbacks queued by the hand-off demo endpoints
 * actually ran, so the flow can be verified from the outside (see {@link HandOffDemoController}).
 */
final class HandOffDemoState {

    static final AtomicInteger CALLBACKS_RUN = new AtomicInteger();

    private HandOffDemoState() {
    }
}
