package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests that long contact details on a list card wrap instead of being cut off.
 */
public class PersonCardContactDetailsTest {

    private static final String LONG_ADDRESS =
            "Blk 123 Ang Mo Kio Avenue 6, #12-345, Singapore Society Building, Singapore 560123";
    private static final String LONG_EMAIL =
            "fundraising.partnerships.and.corporate.giving@internationalcharityfoundation.org.sg";
    private static final double CARD_WIDTH = 320;
    private static final double LAYOUT_TOLERANCE = 1.0;

    @Test
    public void layout_longAddressAndEmail_showsFullText() throws Exception {
        Person person = new PersonBuilder().withAddress(LONG_ADDRESS).withEmail(LONG_EMAIL).build();

        runOnFxThread(() -> {
            Region root = createLaidOutCard(person);
            Label address = (Label) root.lookup("#address");
            Label email = (Label) root.lookup("#email");

            assertFullyDisplayed(address, LONG_ADDRESS);
            assertFullyDisplayed(email, LONG_EMAIL);
            assertTrue(email.localToScene(email.getBoundsInLocal()).getMinY()
                    >= address.localToScene(address.getBoundsInLocal()).getMaxY(),
                    "The email should be below the wrapped address");
            assertTrue(email.localToScene(email.getBoundsInLocal()).getMaxY()
                    <= root.localToScene(root.getBoundsInLocal()).getMaxY() + LAYOUT_TOLERANCE,
                    "The card should grow to fit the wrapped lines");
        });
    }

    @Test
    public void layout_shortAddressAndEmail_remainsOnSingleLine() throws Exception {
        Person person = new PersonBuilder().withAddress("1 Main St").withEmail("amy@example.com").build();

        runOnFxThread(() -> {
            Region root = createLaidOutCard(person);
            Label address = (Label) root.lookup("#address");
            Label email = (Label) root.lookup("#email");

            assertFullyDisplayed(address, "1 Main St");
            assertFullyDisplayed(email, "amy@example.com");
            assertEquals(address.prefHeight(-1), address.getHeight(), LAYOUT_TOLERANCE);
            assertEquals(email.prefHeight(-1), email.getHeight(), LAYOUT_TOLERANCE);
        });
    }

    private static Region createLaidOutCard(Person person) {
        Region root = new PersonCard(person, 1).getRoot();
        Scene scene = new Scene(root, CARD_WIDTH, 500);
        scene.getStylesheets().add(
                PersonCardContactDetailsTest.class.getResource("/view/DarkTheme.css").toExternalForm());
        root.applyCss();
        root.resize(CARD_WIDTH, root.prefHeight(CARD_WIDTH));
        root.layout();
        return root;
    }

    private static void assertFullyDisplayed(Label label, String expectedText) {
        Text renderedText = (Text) label.lookup(".text");
        assertEquals(expectedText, renderedText.getText(), "The rendered text should not contain an ellipsis");
        assertTrue(label.getHeight() + LAYOUT_TOLERANCE >= label.prefHeight(label.getWidth()),
                "The label should have enough height for all wrapped lines");
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
