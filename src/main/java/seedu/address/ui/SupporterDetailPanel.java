package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;
import seedu.address.ui.SupporterDetailData.InteractionEntry;

/**
 * Displays the selected supporter's details and a scrollable interaction history.
 */
public class SupporterDetailPanel extends UiPart<Region> {

    private static final String FXML = "SupporterDetailPanel.fxml";

    private Optional<Person> displayedPerson = Optional.empty();

    @FXML
    private Label startupMessage;
    @FXML
    private VBox detailsContainer;
    @FXML
    private Label supporterName;
    @FXML
    private Label supporterStage;
    @FXML
    private Label supporterOrganisation;
    @FXML
    private Label supporterPhone;
    @FXML
    private Label supporterEmail;
    @FXML
    private Label supporterAddress;
    @FXML
    private Label supporterTags;
    @FXML
    private Label lastContact;
    @FXML
    private Label historyHeading;
    @FXML
    private ScrollPane historyScrollPane;
    @FXML
    private Label historyPlaceholder;
    @FXML
    private VBox interactionEntries;

    /**
     * Creates the panel showing the startup prompt with no supporter selected.
     */
    public SupporterDetailPanel() {
        super(FXML);
        render(new SupporterDetailData(Optional.empty()));
    }

    /**
     * Shows {@code supporter}, or clears the details and shows the startup prompt when selection is empty.
     * Returns the history to the top when the supporter or their record changes, and preserves scrolling
     * when the displayed record is unchanged.
     */
    public void setSupporter(Optional<Person> supporter) {
        requireNonNull(supporter);
        if (displayedPerson.equals(supporter)) {
            return;
        }

        render(new SupporterDetailData(supporter));
        displayedPerson = supporter;
    }

    private void render(SupporterDetailData data) {
        setShown(startupMessage, !data.hasSupporter());
        startupMessage.setText(data.hasSupporter() ? "" : data.getPlaceholderMessage());
        setShown(detailsContainer, data.hasSupporter());
        supporterName.setText(data.getName());
        supporterStage.setText(data.getStage());
        supporterOrganisation.setText(data.getOrganisation());
        supporterPhone.setText(data.getPhone());
        supporterEmail.setText(data.getEmail());
        supporterAddress.setText(data.getAddress());
        supporterTags.setText(data.getTags());
        lastContact.setText(data.getLastContact());
        historyHeading.setText(data.getHistoryHeading());

        historyPlaceholder.setText(data.hasSupporter() ? data.getPlaceholderMessage() : "");
        setShown(historyPlaceholder, data.hasSupporter() && data.getInteractionEntries().isEmpty());
        interactionEntries.getChildren().clear();
        for (InteractionEntry entry : data.getInteractionEntries()) {
            interactionEntries.getChildren().add(createInteractionRow(entry));
        }
        historyScrollPane.setVvalue(0);
    }

    private static HBox createInteractionRow(InteractionEntry entry) {
        Label number = new Label("#" + entry.getNumber());
        number.setMinWidth(Region.USE_PREF_SIZE);
        number.getStyleClass().add("label-bright");

        Label date = new Label(entry.getDate());
        date.setMinWidth(Region.USE_PREF_SIZE);
        date.getStyleClass().add("label-bright");

        Label note = new Label(entry.getNote());
        note.setWrapText(true);
        note.setMinWidth(0);
        note.setMaxWidth(Double.MAX_VALUE);
        note.setMinHeight(Region.USE_PREF_SIZE);
        note.getStyleClass().add("label-bright");
        HBox.setHgrow(note, Priority.ALWAYS);

        HBox row = new HBox(12, number, date, note);
        row.setAlignment(Pos.TOP_LEFT);
        row.setMinHeight(Region.USE_PREF_SIZE);
        return row;
    }

    private static void setShown(Node node, boolean isShown) {
        node.setVisible(isShown);
        node.setManaged(isShown);
    }
}
