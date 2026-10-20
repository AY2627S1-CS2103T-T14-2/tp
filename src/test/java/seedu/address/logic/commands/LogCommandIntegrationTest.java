package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.ui.SupporterDetailData;
import seedu.address.ui.SupporterDetailData.InteractionEntry;

/**
 * Tests dated logging from command parsing through the model to detail-panel data, without starting JavaFX.
 */
public class LogCommandIntegrationTest {

    @Test
    public void execute_backDatedLogs_updatesSelectedHistoryInDateOrder() throws Exception {
        Interaction recentInteraction = new Interaction(LocalDate.of(2024, 3, 10), "Recent call");
        Person target = new PersonBuilder().withInteractions(recentInteraction).build();
        Person other = new PersonBuilder().withName("Other Supporter").build();
        Model model = new ModelManager();
        model.addPerson(target);
        model.addPerson(other);
        model.setViewedPerson(other);
        model.updateFilteredPersonList(person -> person.getName().equals(target.getName()));
        AddressBookParser parser = new AddressBookParser();

        // a back-dated entry selects the target but appears below the more recent interaction
        CommandResult firstResult = parser.parseCommand("log 1 nt/Earlier meeting dt/2024-02-29").execute(model);
        Person firstUpdate = model.getViewedPerson().orElseThrow();
        SupporterDetailData firstDisplay = new SupporterDetailData(model.getViewedPerson());
        assertEquals("Logged interaction #2 with " + target.getName() + " (29 Feb 2024): Earlier meeting.",
                firstResult.getFeedbackToUser());
        assertEquals(List.of(1, 2), firstDisplay.getInteractionEntries().stream()
                .map(InteractionEntry::getNumber).toList());
        assertEquals(List.of("Recent call", "Earlier meeting"), firstDisplay.getInteractionEntries().stream()
                .map(InteractionEntry::getNote).toList());

        // another entry on the same past date refreshes the selected history and uses the higher number first
        parser.parseCommand("log 1 dt/2024-02-29 nt/Second earlier meeting").execute(model);
        Person secondUpdate = model.getViewedPerson().orElseThrow();
        SupporterDetailData secondDisplay = new SupporterDetailData(model.getViewedPerson());
        assertEquals(List.of(1, 3, 2), secondDisplay.getInteractionEntries().stream()
                .map(InteractionEntry::getNumber).toList());
        assertEquals(List.of("10 Mar 2024", "29 Feb 2024", "29 Feb 2024"),
                secondDisplay.getInteractionEntries().stream().map(InteractionEntry::getDate).toList());
        assertEquals(List.of("Recent call", "Second earlier meeting", "Earlier meeting"),
                secondDisplay.getInteractionEntries().stream().map(InteractionEntry::getNote).toList());
        assertEquals("Interactions (3) - newest first", secondDisplay.getHistoryHeading());

        // display sorting leaves stored logging order and the current supporter filter unchanged
        assertEquals(List.of(recentInteraction, new Interaction(LocalDate.of(2024, 2, 29), "Earlier meeting"),
                new Interaction(LocalDate.of(2024, 2, 29), "Second earlier meeting")),
                secondUpdate.getInteractions());
        assertEquals(List.of(secondUpdate), model.getFilteredPersonList());
        assertEquals(2, firstUpdate.getInteractions().size());
        assertEquals(other, model.getAddressBook().getPersonList().get(1));
    }
}
