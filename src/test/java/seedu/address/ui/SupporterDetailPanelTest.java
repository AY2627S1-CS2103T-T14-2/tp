package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies detail-panel FXML loading and rendering on the JavaFX application thread without opening a window.
 */
public class SupporterDetailPanelTest {

    @Test
    public void setSupporter_selectionChanges_rendersDetailsAndScrollableHistory() throws Exception {
        FutureTask<Void> verification = new FutureTask<>(() -> {
            verifyPanelRendering();
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

    private static void verifyPanelRendering() {
        SupporterDetailPanel panel = new SupporterDetailPanel();
        Scene scene = new Scene(panel.getRoot(), 400, 500);
        scene.getStylesheets().add(SupporterDetailPanelTest.class.getResource("/view/DarkTheme.css").toExternalForm());
        Label startup = (Label) panel.getRoot().lookup("#startupMessage");
        VBox details = (VBox) panel.getRoot().lookup("#detailsContainer");
        Label lastContact = (Label) panel.getRoot().lookup("#lastContact");
        ScrollPane history = (ScrollPane) panel.getRoot().lookup("#historyScrollPane");
        VBox entries = (VBox) history.getContent().lookup("#interactionEntries");
        Label placeholder = (Label) history.getContent().lookup("#historyPlaceholder");

        assertEquals(SupporterDetailData.MESSAGE_STARTUP, startup.getText());
        assertTrue(startup.isVisible());
        assertTrue(startup.isManaged());
        assertFalse(details.isVisible());
        assertFalse(details.isManaged());
        assertEquals("-", lastContact.getText());

        Person person = new PersonBuilder().withoutAddress().build();
        panel.setSupporter(Optional.of(person));
        assertFalse(startup.isVisible());
        assertFalse(startup.isManaged());
        assertTrue(details.isVisible());
        assertTrue(details.isManaged());
        assertEquals("Amy Bee", ((Label) panel.getRoot().lookup("#supporterName")).getText());
        assertEquals("Prospect", ((Label) panel.getRoot().lookup("#supporterStage")).getText());
        assertEquals("85355255", ((Label) panel.getRoot().lookup("#supporterPhone")).getText());
        assertEquals("amy@gmail.com", ((Label) panel.getRoot().lookup("#supporterEmail")).getText());
        assertEquals("-", ((Label) panel.getRoot().lookup("#supporterAddress")).getText());
        assertEquals("-", ((Label) panel.getRoot().lookup("#supporterOrganisation")).getText());
        assertEquals("-", ((Label) panel.getRoot().lookup("#supporterTags")).getText());
        assertEquals(SupporterDetailData.MESSAGE_EMPTY_HISTORY, placeholder.getText());
        assertEquals("No interactions yet", lastContact.getText());
        assertTrue(lastContact.isVisible());
        assertTrue(lastContact.isManaged());
        assertTrue(placeholder.isVisible());
        assertTrue(placeholder.isManaged());
        assertTrue(entries.getChildren().isEmpty());

        String note = "Discussion ".repeat(40).trim();
        List<Interaction> interactions = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            interactions.add(new Interaction(LocalDate.of(2026, 9, 16), note));
        }
        Person personWithHistory = new PersonBuilder(person)
                .withInteractions(interactions.toArray(Interaction[]::new)).build();
        panel.setSupporter(Optional.of(personWithHistory));
        panel.getRoot().applyCss();
        panel.getRoot().layout();

        assertEquals(40, entries.getChildren().size());
        Label heading = (Label) panel.getRoot().lookup("#historyHeading");
        assertEquals("Interactions (40) - newest first", heading.getText());
        assertEquals("Last contact: 16 Sep 2026", lastContact.getText());
        assertTrue(lastContact.getHeight() > 0);
        assertTrue(lastContact.getLayoutY() + lastContact.getHeight() <= heading.getLayoutY());
        assertFalse(placeholder.isVisible());
        assertFalse(placeholder.isManaged());
        HBox firstRow = (HBox) entries.getChildren().get(0);
        assertEquals("#40", ((Label) firstRow.getChildren().get(0)).getText());
        assertEquals("16 Sep 2026", ((Label) firstRow.getChildren().get(1)).getText());
        Label firstNote = (Label) firstRow.getChildren().get(2);
        assertEquals(note, firstNote.getText());
        assertTrue(firstNote.isWrapText());
        assertTrue(firstNote.getHeight() > firstNote.getFont().getSize() * 2);
        assertTrue(history.getContent().getBoundsInLocal().getHeight() > history.getViewportBounds().getHeight());
        assertTrue(history.isFitToWidth());
        assertEquals(ScrollPane.ScrollBarPolicy.NEVER, history.getHbarPolicy());

        history.setVvalue(0.8);
        panel.setSupporter(Optional.of(personWithHistory));
        assertEquals(0.8, history.getVvalue());

        Person updatedPerson = new PersonBuilder(personWithHistory).withStage("giving").build();
        panel.setSupporter(Optional.of(updatedPerson));
        assertEquals(0.0, history.getVvalue());
        assertEquals("Giving", ((Label) panel.getRoot().lookup("#supporterStage")).getText());

        Person updatedHistory = new PersonBuilder(updatedPerson)
                .withInteractions(new Interaction(LocalDate.of(2026, 10, 3), "Called"),
                        new Interaction(LocalDate.of(2026, 9, 30), "Met"))
                .build();
        panel.setSupporter(Optional.of(updatedHistory));
        assertEquals("Last contact: 03 Oct 2026", lastContact.getText());
        HBox updatedFirstRow = (HBox) entries.getChildren().get(0);
        Label latestHistoryDate = (Label) updatedFirstRow.getChildren().get(1);
        assertEquals("Last contact: " + latestHistoryDate.getText(), lastContact.getText());

        panel.setSupporter(Optional.of(person));
        assertEquals("No interactions yet", lastContact.getText());

        panel.setSupporter(Optional.empty());
        assertTrue(startup.isVisible());
        assertTrue(startup.isManaged());
        assertEquals(SupporterDetailData.MESSAGE_STARTUP, startup.getText());
        assertFalse(details.isVisible());
        assertFalse(details.isManaged());
        assertTrue(entries.getChildren().isEmpty());
        assertEquals("-", ((Label) panel.getRoot().lookup("#supporterName")).getText());
        assertEquals("", placeholder.getText());
        assertEquals("-", lastContact.getText());
    }
}
