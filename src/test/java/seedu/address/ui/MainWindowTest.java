package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests detail-panel updates through the main window's command box without opening a window.
 */
public class MainWindowTest {

    @TempDir
    private Path temporaryFolder;

    @Test
    public void executeCommand_selectionLifecycle_updatesDetailPanel() throws Exception {
        runOnFxThread(() -> verifySelectionLifecycle());
    }

    @Test
    public void constructor_savedSizeLargerThanScreenAndPositionOffScreen_fitsOnScreen() throws Exception {
        runOnFxThread(() -> {
            Stage stage = createStage(new GuiSettings(100_000, 100_000, -100_000, -100_000));
            try {
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                assertTrue(stage.getWidth() <= screenBounds.getWidth());
                assertTrue(stage.getHeight() <= screenBounds.getHeight());
                assertNotEquals(-100_000, stage.getX());
                assertNotEquals(-100_000, stage.getY());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void constructor_savedPositionOnScreen_restoresPosition() throws Exception {
        runOnFxThread(() -> {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            int x = (int) screenBounds.getMinX() + 10;
            int y = (int) screenBounds.getMinY() + 10;
            Stage stage = createStage(new GuiSettings(700, 450, x, y));
            try {
                assertEquals(x, stage.getX());
                assertEquals(y, stage.getY());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void isOnAnyScreen() throws Exception {
        runOnFxThread(() -> {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Point centre = new Point((int) (screenBounds.getMinX() + screenBounds.getWidth() / 2),
                    (int) (screenBounds.getMinY() + screenBounds.getHeight() / 2));
            assertTrue(MainWindow.isOnAnyScreen(centre));
            assertFalse(MainWindow.isOnAnyScreen(new Point(-100_000, -100_000)));
        });
    }

    @Test
    public void executeCommand_saveFailure_keepsPanelConsistentWithModel() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder().build());
            JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(temporaryFolder.resolve("book.json")) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    throw new IOException("Test save failure");
                }
            };
            MainWindow window = createWindow(model, failingStorage);
            try {
                submit(window, "log 1 nt/Called despite save failure");

                assertEquals("Amy Bee", label(window, "supporterName").getText());
                assertEquals("Interactions (1) - newest first", label(window, "historyHeading").getText());
                TextArea feedback = (TextArea) window.getPrimaryStage().getScene().lookup("#resultDisplay");
                assertTrue(feedback.getText().contains("Test save failure"));
                assertEquals(1, model.getViewedPerson().orElseThrow().getInteractionCount());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    private void verifySelectionLifecycle() {
        Model model = new ModelManager();
        List<Interaction> interactions = new ArrayList<>();
        String note = "Discussion ".repeat(40).trim();
        for (int i = 0; i < 40; i++) {
            interactions.add(new Interaction(LocalDate.of(2026, 9, 16), note));
        }
        Person amy = new PersonBuilder().withInteractions(interactions.toArray(Interaction[]::new)).build();
        Person bob = new PersonBuilder().withName("Bob Lee").build();
        model.addPerson(amy);
        model.addPerson(bob);
        MainWindow window = createWindow(model, new JsonAddressBookStorage(temporaryFolder.resolve("book.json")));
        try {
            assertTrue(label(window, "startupMessage").isVisible());
            assertEquals(SupporterDetailData.MESSAGE_STARTUP, label(window, "startupMessage").getText());

            submit(window, "view 1");
            assertEquals("Amy Bee", label(window, "supporterName").getText());
            verifyHistoryLayout(window);

            ScrollPane history = (ScrollPane) window.getPrimaryStage().getScene().lookup("#historyScrollPane");
            history.setVvalue(0.8);
            submit(window, "find Bob");
            assertEquals(List.of(bob), model.getFilteredPersonList());
            assertEquals("Amy Bee", label(window, "supporterName").getText());
            assertEquals(0.8, history.getVvalue());

            submit(window, "view 1");
            assertEquals("Bob Lee", label(window, "supporterName").getText());
            assertEquals(List.of(bob), model.getFilteredPersonList());
            assertEquals(SupporterDetailData.MESSAGE_EMPTY_HISTORY, historyPlaceholder(history).getText());

            submit(window, "view 2");
            assertEquals("Bob Lee", label(window, "supporterName").getText());
            submit(window, "view abc");
            assertEquals("Bob Lee", label(window, "supporterName").getText());
            submit(window, "list");
            assertEquals("Bob Lee", label(window, "supporterName").getText());
            submit(window, "add n/Cara Tan p/91234567");
            assertEquals("Bob Lee", label(window, "supporterName").getText());

            submit(window, "log 1 nt/Follow-up call");
            assertEquals("Amy Bee", label(window, "supporterName").getText());
            assertEquals("Interactions (41) - newest first", label(window, "historyHeading").getText());
            assertEquals(0.0, history.getVvalue());
            submit(window, "stage 2 giving");
            assertEquals("Bob Lee", label(window, "supporterName").getText());
            assertEquals("Giving", label(window, "supporterStage").getText());
            submit(window, "view 1");
            submit(window, "stage 2 giving");
            assertEquals("Bob Lee", label(window, "supporterName").getText());

            submit(window, "delete 1");
            assertEquals("Bob Lee", label(window, "supporterName").getText());
            submit(window, "delete 1");
            assertCleared(window);
            submit(window, "view 1");
            assertEquals("Cara Tan", label(window, "supporterName").getText());
            submit(window, "clear");
            assertCleared(window);
        } finally {
            window.getPrimaryStage().close();
        }
    }

    private Stage createStage(GuiSettings savedGuiSettings) {
        Model model = new ModelManager();
        model.setGuiSettings(savedGuiSettings);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("book.json"));
        return createWindow(model, storage).getPrimaryStage();
    }

    private MainWindow createWindow(Model model, JsonAddressBookStorage addressBookStorage) {
        StorageManager storage = new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage),
                storage.getAddressBookFilePath());
        window.fillInnerParts();
        Region root = (Region) window.getPrimaryStage().getScene().getRoot();
        root.resize(1000, 560);
        root.applyCss();
        root.layout();
        return window;
    }

    private static void verifyHistoryLayout(MainWindow window) {
        Region root = (Region) window.getPrimaryStage().getScene().getRoot();
        ScrollPane history = (ScrollPane) root.lookup("#historyScrollPane");
        VBox entries = (VBox) history.getContent().lookup("#interactionEntries");
        for (double width : new double[] {740, 1000}) {
            root.resize(width, 560);
            root.applyCss();
            root.layout();
            HBox firstRow = (HBox) entries.getChildren().get(0);
            Label firstNote = (Label) firstRow.getChildren().get(2);
            assertEquals("#40", ((Label) firstRow.getChildren().get(0)).getText());
            assertTrue(firstNote.getHeight() > firstNote.getFont().getSize() * 2);
            assertTrue(history.getContent().getBoundsInLocal().getHeight() > history.getViewportBounds().getHeight());
        }
    }

    private static void submit(MainWindow window, String command) {
        TextField commandField = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
        commandField.setText(command);
        commandField.fireEvent(new ActionEvent());
    }

    private static Label label(MainWindow window, String id) {
        return (Label) window.getPrimaryStage().getScene().lookup("#" + id);
    }

    private static Label historyPlaceholder(ScrollPane history) {
        return (Label) history.getContent().lookup("#historyPlaceholder");
    }

    private static void assertCleared(MainWindow window) {
        assertTrue(label(window, "startupMessage").isVisible());
        assertEquals(SupporterDetailData.MESSAGE_STARTUP, label(window, "startupMessage").getText());
        VBox details = (VBox) window.getPrimaryStage().getScene().lookup("#detailsContainer");
        assertFalse(details.isVisible());
        assertFalse(details.isManaged());
        assertEquals("-", label(window, "supporterName").getText());
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
