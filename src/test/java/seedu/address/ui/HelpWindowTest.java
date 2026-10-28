package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.input.Clipboard;

/**
 * Tests that the help window's copy button copies the user guide URL and shows that it worked.
 */
public class HelpWindowTest {
    private static final long RESTORE_TIMEOUT_SECONDS = 10;

    @Test
    public void copyButton_click_copiesUrlAndShowsCopied() throws Exception {
        runOnFxThread(() -> {
            Button copyButton = getCopyButton(new HelpWindow());
            assertEquals(HelpWindow.COPY_BUTTON_TEXT, copyButton.getText());

            copyButton.fire();

            assertEquals(HelpWindow.COPIED_BUTTON_TEXT, copyButton.getText());
            assertEquals(HelpWindow.USERGUIDE_URL, Clipboard.getSystemClipboard().getString());
        });
    }

    @Test
    public void copyButton_afterClick_restoresButtonText() throws Exception {
        CountDownLatch restored = new CountDownLatch(1);
        runOnFxThread(() -> {
            Button copyButton = getCopyButton(new HelpWindow());
            copyButton.fire();
            copyButton.textProperty().addListener((unused, oldText, newText) -> {
                if (HelpWindow.COPY_BUTTON_TEXT.equals(newText)) {
                    restored.countDown();
                }
            });
        });

        assertTrue(restored.await(RESTORE_TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    private static Button getCopyButton(HelpWindow helpWindow) {
        return (Button) helpWindow.getRoot().getScene().lookup("#copyButton");
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        try {
            Platform.startup(task);
        } catch (IllegalStateException e) {
            // Reuse the toolkit if another UI test has already started it.
            Platform.runLater(task);
        }
        task.get(15, TimeUnit.SECONDS);
    }
}
