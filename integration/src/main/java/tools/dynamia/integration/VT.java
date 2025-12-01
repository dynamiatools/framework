package tools.dynamia.integration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class for managing a reusable ExecutorService that uses virtual threads.  This register a shutdown
 * hook to properly shutdown the executor when the application terminates.
 */
public final class VT {

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(VT::shutdown));
    }

    // reutilizable, eficiente, moderno
    private static final ExecutorService EXEC =
            Executors.newVirtualThreadPerTaskExecutor();

    public static ExecutorService executor() {
        return EXEC;
    }

    public static void shutdown() {
        EXEC.shutdown();
    }

    private VT() {
    }
}
