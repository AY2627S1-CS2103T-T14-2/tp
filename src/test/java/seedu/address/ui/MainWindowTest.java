package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.awt.Point;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

    private static final int FX_TIMEOUT_SECONDS = 20;

    @TempDir
    private Path temporaryFolder;

    @Test
    public void executeCommand_selectionLifecycle_updatesDetailPanel() throws Exception {
        runOnFxThread(this::verifySelectionLifecycle, FX_TIMEOUT_SECONDS);
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
        }, FX_TIMEOUT_SECONDS);
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
        }, FX_TIMEOUT_SECONDS);
    }

    @Test
    public void isOnAnyScreen() throws Exception {
        runOnFxThread(() -> {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Point centre = new Point((int) (screenBounds.getMinX() + screenBounds.getWidth() / 2),
                    (int) (screenBounds.getMinY() + screenBounds.getHeight() / 2));
            assertTrue(MainWindow.isOnAnyScreen(centre));
            assertFalse(MainWindow.isOnAnyScreen(new Point(-100_000, -100_000)));
        }, FX_TIMEOUT_SECONDS);
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

                assertEquals("Amy Bee", getLabel(window, "supporterName").getText());
                assertEquals("Interactions (1) - newest first", getLabel(window, "historyHeading").getText());
                TextArea feedback = getResultDisplay(window);
                assertTrue(feedback.getText().contains("Test save failure"));
                assertEquals(1, model.getViewedPerson().orElseThrow().getInteractionCount());
            } finally {
                window.getPrimaryStage().close();
            }
        }, FX_TIMEOUT_SECONDS);
    }

    private void verifySelectionLifecycle() {
        Model model = new ModelManager();
        Person bob = new PersonBuilder().withName("Bob Lee").build();
        model.addPerson(createPersonWithHistory());
        model.addPerson(bob);
        MainWindow window = createWindow(model, new JsonAddressBookStorage(temporaryFolder.resolve("book.json")));
        try {
            assertStartupState(window);
            verifyViewingSupporterWithHistory(window);
            verifyFilteringPreservesSelection(window, model, bob);
            verifyViewingFilteredSupporter(window, model, bob);
            verifyInvalidViewsPreserveSelection(window);
            verifyListAndAddPreserveSelection(window);
            verifyLoggingSelectsSupporter(window);
            verifyStageChangesSelectSupporter(window);
            verifyDeletionAndClearing(window);
        } finally {
            window.getPrimaryStage().close();
        }
    }

    private static Person createPersonWithHistory() {
        List<Interaction> interactions = new ArrayList<>();
        String note = "Discussion ".repeat(40).trim();
        for (int i = 0; i < 40; i++) {
            interactions.add(new Interaction(LocalDate.of(2026, 9, 16), note));
        }
        return new PersonBuilder().withInteractions(interactions.toArray(Interaction[]::new)).build();
    }

    private static void verifyViewingSupporterWithHistory(MainWindow window) {
        submitAndAssertSupporter(window, "view 1", "Amy Bee");
        verifyHistoryLayout(window);
    }

    private static void verifyFilteringPreservesSelection(MainWindow window, Model model, Person bob) {
        ScrollPane history = getHistoryScrollPane(window);
        history.setVvalue(0.8);
        submit(window, "find Bob");
        assertEquals(List.of(bob), model.getFilteredPersonList());
        assertEquals("Amy Bee", getLabel(window, "supporterName").getText());
        assertEquals(0.8, history.getVvalue());
    }

    private static void verifyViewingFilteredSupporter(MainWindow window, Model model, Person bob) {
        submitAndAssertSupporter(window, "view 1", "Bob Lee");
        assertEquals(List.of(bob), model.getFilteredPersonList());
        assertEquals(SupporterDetailData.MESSAGE_EMPTY_HISTORY,
                getHistoryPlaceholder(getHistoryScrollPane(window)).getText());
    }

    private static void verifyInvalidViewsPreserveSelection(MainWindow window) {
        submitAndAssertSupporter(window, "view 2", "Bob Lee");
        submitAndAssertSupporter(window, "view abc", "Bob Lee");
    }

    private static void verifyListAndAddPreserveSelection(MainWindow window) {
        submitAndAssertSupporter(window, "list", "Bob Lee");
        submitAndAssertSupporter(window, "add n/Cara Tan p/91234567", "Bob Lee");
    }

    private static void verifyLoggingSelectsSupporter(MainWindow window) {
        submitAndAssertSupporter(window, "log 1 nt/Follow-up call", "Amy Bee");
        assertEquals("Interactions (41) - newest first", getLabel(window, "historyHeading").getText());
        assertEquals(0.0, getHistoryScrollPane(window).getVvalue());
    }

    private static void verifyStageChangesSelectSupporter(MainWindow window) {
        submitAndAssertSupporter(window, "stage 2 giving", "Bob Lee");
        assertEquals("Giving", getLabel(window, "supporterStage").getText());
        submit(window, "view 1");
        submitAndAssertSupporter(window, "stage 2 giving", "Bob Lee");
    }

    private static void verifyDeletionAndClearing(MainWindow window) {
        submitAndAssertSupporter(window, "delete 1", "Bob Lee");
        submit(window, "delete 1");
        assertCleared(window);
        submitAndAssertSupporter(window, "view 1", "Cara Tan");
        submit(window, "clear");
        assertCleared(window);
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
        Region root = getRoot(window);
        root.resize(1000, 560);
        root.applyCss();
        root.layout();
        return window;
    }

    private static void verifyHistoryLayout(MainWindow window) {
        Region root = getRoot(window);
        ScrollPane history = getHistoryScrollPane(window);
        VBox entries = getInteractionEntries(history);
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
        TextField commandField = getCommandField(window);
        commandField.setText(command);
        commandField.fireEvent(new ActionEvent());
    }

    private static void submitAndAssertSupporter(MainWindow window, String command, String expectedName) {
        submit(window, command);
        assertEquals(expectedName, getLabel(window, "supporterName").getText(),
                "Selected supporter after command: " + command);
    }

    private static Region getRoot(MainWindow window) {
        return (Region) window.getPrimaryStage().getScene().getRoot();
    }

    private static Label getLabel(MainWindow window, String id) {
        return (Label) window.getPrimaryStage().getScene().lookup("#" + id);
    }

    private static Label getHistoryPlaceholder(ScrollPane history) {
        return (Label) history.getContent().lookup("#historyPlaceholder");
    }

    private static ScrollPane getHistoryScrollPane(MainWindow window) {
        return (ScrollPane) window.getPrimaryStage().getScene().lookup("#historyScrollPane");
    }

    private static VBox getInteractionEntries(ScrollPane history) {
        return (VBox) history.getContent().lookup("#interactionEntries");
    }

    private static TextField getCommandField(MainWindow window) {
        return (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
    }

    private static TextArea getResultDisplay(MainWindow window) {
        return (TextArea) window.getPrimaryStage().getScene().lookup("#resultDisplay");
    }

    private static VBox getDetailsContainer(MainWindow window) {
        return (VBox) window.getPrimaryStage().getScene().lookup("#detailsContainer");
    }

    private static void assertStartupState(MainWindow window) {
        assertTrue(getLabel(window, "startupMessage").isVisible());
        assertEquals(SupporterDetailData.MESSAGE_STARTUP, getLabel(window, "startupMessage").getText());
    }

    private static void assertCleared(MainWindow window) {
        assertStartupState(window);
        VBox details = getDetailsContainer(window);
        assertFalse(details.isVisible());
        assertFalse(details.isManaged());
        assertEquals("-", getLabel(window, "supporterName").getText());
    }
}
