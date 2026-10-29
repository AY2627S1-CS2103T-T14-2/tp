package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests list-card FXML loading, text wrapping, and last-contact rendering without opening a window.
 */
public class PersonCardTest {

    private static final String LONG_NAME = "Muhammad Abdul Rahman bin Mohamed Iskandar Shah";
    private static final String LONG_ORGANISATION =
            "Singapore Society for the Prevention of Cruelty to Animals";
    private static final double LAYOUT_TOLERANCE = 1.0;

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

    @Test
    public void layout_longNameAndOrganisation_wrapsWithoutOverlappingContent() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withName(LONG_NAME).withOrganisation(LONG_ORGANISATION).build();
            PersonCard card = createLaidOutCard(person, 250);
            Label name = (Label) card.getRoot().lookup("#name");
            Label organisation = (Label) card.getRoot().lookup("#organisation");
            Label index = (Label) card.getRoot().lookup("#id");
            Label stage = (Label) card.getRoot().lookup("#stage");
            Label lastContact = (Label) card.getRoot().lookup("#lastContact");

            assertFullyDisplayed(name, LONG_NAME);
            assertFullyDisplayed(organisation, LONG_ORGANISATION);
            assertTrue(name.getHeight() > name.prefHeight(-1), "The name should occupy multiple lines");
            assertTrue(organisation.getHeight() > organisation.prefHeight(-1),
                    "The organisation should occupy multiple lines");
            assertEquals(getBaselineInScene(name), getBaselineInScene(index), LAYOUT_TOLERANCE);
            assertEquals(getBaselineInScene(name), getBaselineInScene(stage), LAYOUT_TOLERANCE);
            assertFullyDisplayed(index, "1. ");
            assertFullyDisplayed(stage, person.getStage().toString());
            assertTrue(index.localToScene(index.getBoundsInLocal()).getMaxX()
                    <= name.localToScene(name.getBoundsInLocal()).getMinX());
            assertTrue(name.localToScene(name.getBoundsInLocal()).getMaxX()
                    <= stage.localToScene(stage.getBoundsInLocal()).getMinX());
            assertBelow(organisation, name);
            assertBelow(organisation, stage);
            assertBelow(lastContact, organisation);
        });
    }

    @Test
    public void layout_cardResized_reflowsTextAndFitsContent() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withName(LONG_NAME).withOrganisation(LONG_ORGANISATION).build();
            PersonCard card = createLaidOutCard(person, 650);
            Region root = card.getRoot();
            Label name = (Label) root.lookup("#name");
            Label organisation = (Label) root.lookup("#organisation");
            Region content = (Region) name.getParent().getParent();
            double wideContentHeight = content.getHeight();
            double wideNameHeight = name.getHeight();
            double wideOrganisationHeight = organisation.getHeight();

            root.resize(250, root.prefHeight(250));
            root.layout();

            assertTrue(content.getHeight() > wideContentHeight, "The content should grow when narrowed");
            assertTrue(name.getHeight() > wideNameHeight, "The name should wrap onto more lines");
            assertTrue(organisation.getHeight() > wideOrganisationHeight,
                    "The organisation should wrap onto more lines");
            assertFullyDisplayed(name, LONG_NAME);
            assertFullyDisplayed(organisation, LONG_ORGANISATION);
            assertTrue(root.lookup("#email").localToScene(root.lookup("#email").getBoundsInLocal()).getMaxY()
                    <= root.localToScene(root.getBoundsInLocal()).getMaxY());

            root.resize(650, root.prefHeight(650));
            root.layout();

            assertEquals(wideContentHeight, content.getHeight(), LAYOUT_TOLERANCE);
            assertEquals(wideNameHeight, name.getHeight(), LAYOUT_TOLERANCE);
            assertEquals(wideOrganisationHeight, organisation.getHeight(), LAYOUT_TOLERANCE);
            assertFullyDisplayed(name, LONG_NAME);
            assertFullyDisplayed(organisation, LONG_ORGANISATION);
            assertTrue(content.localToScene(content.getBoundsInLocal()).getMaxY()
                    <= root.localToScene(root.getBoundsInLocal()).getMaxY());
        });
    }

    @Test
    public void layout_shortNameAndOrganisation_remainsOnSingleLine() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withName("Amy Tan").withOrganisation("SPCA").build();
            PersonCard card = createLaidOutCard(person, 400);
            Label name = (Label) card.getRoot().lookup("#name");
            Label organisation = (Label) card.getRoot().lookup("#organisation");

            assertFullyDisplayed(name, "Amy Tan");
            assertFullyDisplayed(organisation, "SPCA");
            assertEquals(name.prefHeight(-1), name.getHeight(), LAYOUT_TOLERANCE);
            assertEquals(organisation.prefHeight(-1), organisation.getHeight(), LAYOUT_TOLERANCE);
        });
    }

    private static PersonCard createLaidOutCard(Person person, double width) {
        PersonCard card = new PersonCard(person, 1);
        Region root = card.getRoot();
        Scene scene = new Scene(root, width, 500);
        scene.getStylesheets().add(PersonCardTest.class.getResource("/view/DarkTheme.css").toExternalForm());
        root.applyCss();
        root.resize(width, root.prefHeight(width));
        root.layout();
        return card;
    }

    private static void assertFullyDisplayed(Label label, String expectedText) {
        Text renderedText = (Text) label.lookup(".text");
        assertEquals(expectedText, renderedText.getText(), "The rendered text should not contain an ellipsis");
        assertTrue(label.getHeight() + LAYOUT_TOLERANCE >= label.prefHeight(label.getWidth()),
                "The label should have enough height for all wrapped lines");
    }

    private static double getBaselineInScene(Label label) {
        return label.localToScene(0, label.getBaselineOffset()).getY();
    }

    private static void assertBelow(Node lower, Node upper) {
        assertTrue(lower.localToScene(lower.getBoundsInLocal()).getMinY()
                >= upper.localToScene(upper.getBoundsInLocal()).getMaxY(),
                "The lower element should not overlap the upper element");
    }

    private static void verifyLastContact(Person person, String expectedText) throws Exception {
        runOnFxThread(() -> {
            PersonCard card = createLaidOutCard(person, 400);
            Label lastContact = (Label) card.getRoot().lookup("#lastContact");
            Label organisation = (Label) card.getRoot().lookup("#organisation");

            assertEquals(expectedText, lastContact.getText());
            assertTrue(lastContact.isVisible());
            assertTrue(lastContact.isManaged());
            assertTrue(lastContact.getHeight() > 0);
            assertTrue(lastContact.getLayoutY() >= organisation.getLayoutY() + organisation.getHeight());
        });
    }

}
