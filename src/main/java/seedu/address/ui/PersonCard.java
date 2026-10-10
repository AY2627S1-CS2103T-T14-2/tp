package seedu.address.ui;

import java.util.Comparator;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final String NO_ORGANISATION = "(no organisation)";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private Label organisation;
    @FXML
    private Label stage;
    @FXML
    private FlowPane tags;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        stage.setText(person.getStage().toString());
        stage.getStyleClass().add("stage_" + person.getStage().name().toLowerCase());
        organisation.setText(person.getOrganisation().map(org -> org.value).orElse(NO_ORGANISATION));
        showIfPresent(phone, person.getPhone().map(p -> p.value));
        showIfPresent(address, person.getAddress().map(a -> a.value));
        showIfPresent(email, person.getEmail().map(e -> e.value));
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
    }

    /**
     * Shows {@code value} in {@code label}, or hides the label and the space it takes up if there is no value.
     */
    private static void showIfPresent(Label label, Optional<String> value) {
        value.ifPresentOrElse(label::setText, () -> {
            label.setVisible(false);
            label.setManaged(false);
        });
    }
}
