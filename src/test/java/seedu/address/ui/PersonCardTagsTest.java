package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests the order of tags on a list card without opening a window.
 */
public class PersonCardTagsTest {

    @Test
    public void constructor_mixedCaseTags_sortsIgnoringCase() throws Exception {
        Person person = new PersonBuilder().withTags("VIP", "boardIntro", "monthlyDonor").build();
        FutureTask<List<String>> task = new FutureTask<>(() -> {
            FlowPane tags = (FlowPane) new PersonCard(person, 1).getRoot().lookup("#tags");
            return tags.getChildren().stream().map(tag -> ((Label) tag).getText()).toList();
        });
        try {
            Platform.startup(task);
        } catch (IllegalStateException e) {
            // Reuse the toolkit if another UI test has already started it.
            Platform.runLater(task);
        }

        assertEquals(List.of("boardIntro", "monthlyDonor", "VIP"), task.get(15, TimeUnit.SECONDS));
    }
}
