package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Tests that the command box brings back earlier commands with the Up and Down keys, without opening a window.
 */
public class CommandBoxTest {

    private static final String FAILING_COMMAND = "fail now";

    @Test
    public void pressUp_afterCommands_showsNewestFirstWithCaretAtEnd() throws Exception {
        runOnFxThread(() -> {
            TextField textField = createCommandBoxField();
            submit(textField, "list");
            submit(textField, "view 2");

            press(textField, KeyCode.UP);
            assertEquals("view 2", textField.getText());
            assertEquals("view 2".length(), textField.getCaretPosition());

            press(textField, KeyCode.UP);
            assertEquals("list", textField.getText());

            press(textField, KeyCode.UP);
            assertEquals("list", textField.getText());
        });
    }

    @Test
    public void pressDown_pastNewestCommand_leavesBoxEmpty() throws Exception {
        runOnFxThread(() -> {
            TextField textField = createCommandBoxField();
            submit(textField, "list");
            submit(textField, "view 2");
            press(textField, KeyCode.UP);
            press(textField, KeyCode.UP);

            press(textField, KeyCode.DOWN);
            assertEquals("view 2", textField.getText());

            press(textField, KeyCode.DOWN);
            assertEquals("", textField.getText());
        });
    }

    @Test
    public void pressUp_afterFailedCommand_showsFailedCommand() throws Exception {
        runOnFxThread(() -> {
            TextField textField = createCommandBoxField();
            submit(textField, "list");
            submit(textField, FAILING_COMMAND);
            textField.setText("");

            press(textField, KeyCode.UP);

            assertEquals(FAILING_COMMAND, textField.getText());
        });
    }

    @Test
    public void pressUp_noCommandsEntered_keepsTypedText() throws Exception {
        runOnFxThread(() -> {
            TextField textField = createCommandBoxField();
            textField.setText("lis");

            press(textField, KeyCode.UP);

            assertEquals("lis", textField.getText());
        });
    }

    @Test
    public void pressUp_repeatedCommand_storedOnce() throws Exception {
        runOnFxThread(() -> {
            TextField textField = createCommandBoxField();
            submit(textField, "list");
            submit(textField, "view 1");
            submit(textField, "view 1");

            press(textField, KeyCode.UP);
            press(textField, KeyCode.UP);

            assertEquals("list", textField.getText());
        });
    }

    /**
     * Returns the text field of a new {@code CommandBox} whose executor fails only for {@code FAILING_COMMAND}.
     */
    private static TextField createCommandBoxField() {
        CommandBox commandBox = new CommandBox(commandText -> {
            if (commandText.equals(FAILING_COMMAND)) {
                throw new ParseException("Unknown command.");
            }
            return new CommandResult("Done.");
        });
        TextField textField = (TextField) commandBox.getRoot().lookup("#commandTextField");
        assertTrue(textField != null);
        return textField;
    }

    private static void submit(TextField textField, String commandText) {
        textField.setText(commandText);
        textField.fireEvent(new ActionEvent());
    }

    private static void press(TextField textField, KeyCode keyCode) {
        textField.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode, false, false, false, false));
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> verification = new FutureTask<>(() -> {
            action.run();
            return null;
        });
        try {
            Platform.startup(verification);
        } catch (IllegalStateException e) {
            // Reuse the toolkit if another UI test has already started it.
            Platform.runLater(verification);
        }
        verification.get(20, TimeUnit.SECONDS);
    }
}
