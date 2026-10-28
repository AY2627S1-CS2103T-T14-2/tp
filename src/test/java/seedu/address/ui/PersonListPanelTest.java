package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests that supporter cards in the list cannot be selected.
 */
public class PersonListPanelTest {

    @Test
    public void click_supporterCard_doesNotSelectCard() throws Exception {
        runOnFxThread(() -> {
            Person first = new PersonBuilder().withName("Amy Tan").build();
            Person second = new PersonBuilder().withName("Bob Lim").build();
            Region root = new PersonListPanel(FXCollections.observableArrayList(first, second)).getRoot();
            new Scene(root, 340, 400);
            root.applyCss();
            root.layout();
            ListView<?> listView = (ListView<?>) root.lookup("#personListView");

            for (Node node : root.lookupAll(".list-cell")) {
                Event.fireEvent(node, createPrimaryPress());
            }

            assertTrue(listView.getSelectionModel().isEmpty());
            assertFalse(listView.isFocusTraversable());
        });
    }

    private static MouseEvent createPrimaryPress() {
        return new MouseEvent(MouseEvent.MOUSE_PRESSED, 10, 10, 10, 10, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, true, null);
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
