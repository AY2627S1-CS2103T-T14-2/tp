package seedu.address.testutil;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/**
 * Provides utilities for running tests on the JavaFX application thread.
 */
public final class FxTestUtil {

    private static final int DEFAULT_TIMEOUT_SECONDS = 15;

    private FxTestUtil() {
    }

    /**
     * Runs the action on the JavaFX application thread, starting the toolkit if necessary.
     * Waits up to 15 seconds for completion and propagates task failures to the calling test.
     * Must be called from outside the JavaFX application thread.
     *
     * @throws Exception If the wait is interrupted, the action fails, or the timeout expires.
     */
    public static void runOnFxThread(Runnable action) throws Exception {
        runOnFxThread(action, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Runs the action on the JavaFX application thread, starting the toolkit if necessary.
     * Waits up to {@code timeoutSeconds} seconds and propagates task failures to the calling test.
     * Must be called from outside the JavaFX application thread.
     *
     * @throws Exception If the wait is interrupted, the action fails, or the timeout expires.
     */
    public static void runOnFxThread(Runnable action, long timeoutSeconds) throws Exception {
        FutureTask<Void> verification = new FutureTask<>(action, null);
        try {
            Platform.startup(verification);
        } catch (IllegalStateException e) {
            // Reuse the toolkit if another UI test has already started it.
            Platform.runLater(verification);
        }
        verification.get(timeoutSeconds, TimeUnit.SECONDS);
    }
}
