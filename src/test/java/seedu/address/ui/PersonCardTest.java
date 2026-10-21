package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests list-card FXML loading and last-contact rendering without opening a window.
 */
public class PersonCardTest {

    @Test
    public void constructor_emptyHistory_showsNoInteractionsMessage() throws Exception {
        Person person = new PersonBuilder().build();

        verifyLastContact(person, "No interactions yet");
    }

    @Test
    public void constructor_outOfOrderDates_showsLatestDate() throws Exception {
        Person person = new PersonBuilder()
                .withInteractions(new Interaction(LocalDate.of(2026, 10, 3), "Called"),
                        new Interaction(LocalDate.of(2026, 9, 30), "Met"))
                .build();

        verifyLastContact(person, "Last contact: 03 Oct 2026");
    }

    private static void verifyLastContact(Person person, String expectedText) throws Exception {
        FutureTask<Void> verification = new FutureTask<>(() -> {
            PersonCard card = new PersonCard(person, 1);
            Scene scene = new Scene(card.getRoot(), 400, 250);
            scene.getStylesheets().add(PersonCardTest.class.getResource("/view/DarkTheme.css").toExternalForm());
            card.getRoot().applyCss();
            card.getRoot().layout();
            Label lastContact = (Label) card.getRoot().lookup("#lastContact");
            Label organisation = (Label) card.getRoot().lookup("#organisation");

            assertEquals(expectedText, lastContact.getText());
            assertTrue(lastContact.isVisible());
            assertTrue(lastContact.isManaged());
            assertTrue(lastContact.getHeight() > 0);
            assertTrue(lastContact.getLayoutY() >= organisation.getLayoutY() + organisation.getHeight());
            return null;
        });
        try {
            Platform.startup(verification);
        } catch (IllegalStateException e) {
            // Reuse the toolkit if another UI test has already started it.
            Platform.runLater(verification);
        }
        verification.get(15, TimeUnit.SECONDS);
    }
}
