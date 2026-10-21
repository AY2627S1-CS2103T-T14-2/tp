package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
 * Tests interaction deletion from command parsing through the model to detail-panel data, without starting JavaFX.
 */
public class UnlogCommandIntegrationTest {

    @Test
    public void execute_chronologicalEntry_selectsTargetAndRenumbersFilteredHistory() throws Exception {
        // set up a filtered supporter whose stored history differs from chronological order
        Interaction middle = new Interaction(LocalDate.of(2024, 2, 29), "Middle meeting");
        Interaction oldest = new Interaction(LocalDate.of(2024, 2, 20), "Earlier call");
        Interaction newest = new Interaction(LocalDate.of(2024, 3, 10), "Recent call");
        Person other = new PersonBuilder().withName("Other Supporter").build();
        Person target = new PersonBuilder().withIntroducer(other.getName().fullName)
                .withInteractions(middle, oldest, newest).build();
        Model model = new ModelManager();
        model.addPerson(other);
        model.addPerson(target);
        model.setViewedPerson(other);
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("find " + target.getName().fullName).execute(model);
        assertEquals(List.of(target), model.getFilteredPersonList());

        // filtered supporter #1 is second in storage, and interaction #2 is first in its stored history
        CommandResult middleDeletionResult = parser.parseCommand("unlog 1 2").execute(model);
        Person expected = new PersonBuilder(target).withInteractions(oldest, newest).build();
        assertEquals(Optional.of(expected), model.getViewedPerson());
        assertEquals(List.of(expected), model.getFilteredPersonList());
        assertEquals(List.of(other, expected), model.getAddressBook().getPersonList());
        assertEquals("Deleted interaction #2 with " + target.getName() + " (29 Feb 2024): Middle meeting.",
                middleDeletionResult.getFeedbackToUser());

        SupporterDetailData displayAfterMiddleDeletion = new SupporterDetailData(model.getViewedPerson());
        assertHistory(displayAfterMiddleDeletion, List.of(2, 1),
                List.of("10 Mar 2024", "20 Feb 2024"), List.of("Recent call", "Earlier call"));
        assertEquals("Interactions (2) - newest first", displayAfterMiddleDeletion.getHistoryHeading());
        assertEquals("", displayAfterMiddleDeletion.getPlaceholderMessage());
        assertEquals(other.getName().fullName, displayAfterMiddleDeletion.getIntroducer());

        // the next command uses the refreshed number: #2 now identifies the newest remaining interaction
        CommandResult newestDeletionResult = parser.parseCommand("unlog 1 2").execute(model);
        SupporterDetailData displayAfterNewestDeletion = new SupporterDetailData(model.getViewedPerson());
        assertEquals("Deleted interaction #2 with " + target.getName() + " (10 Mar 2024): Recent call.",
                newestDeletionResult.getFeedbackToUser());
        assertHistory(displayAfterNewestDeletion, List.of(1), List.of("20 Feb 2024"), List.of("Earlier call"));
        assertEquals(Optional.of(oldest.getDate()), model.getViewedPerson().orElseThrow().getLastInteractionDate());
    }

    @Test
    public void execute_identicalEntries_removesSelectedOccurrenceAndPreservesTieOrder() throws Exception {
        LocalDate date = LocalDate.of(2024, 2, 29);
        Interaction duplicate = new Interaction(date, "Meeting");
        Interaction between = new Interaction(date, "Follow-up");
        Person target = new PersonBuilder().withInteractions(duplicate, between,
                new Interaction(date, "Meeting")).build();
        Model model = new ModelManager();
        model.addPerson(target);

        // #3 is the later duplicate; removing the first equal value would reverse the expected remaining order
        new AddressBookParser().parseCommand("unlog 1 3").execute(model);

        assertEquals(List.of(duplicate, between), model.getViewedPerson().orElseThrow().getInteractions());
        SupporterDetailData display = new SupporterDetailData(model.getViewedPerson());
        assertHistory(display, List.of(2, 1), List.of("29 Feb 2024", "29 Feb 2024"),
                List.of("Follow-up", "Meeting"));
    }

    @Test
    public void execute_lastEntry_showsEmptyHistoryForSelectedSupporter() throws Exception {
        Interaction interaction = new Interaction(LocalDate.of(2024, 2, 29), "Meeting");
        Person target = new PersonBuilder().withInteractions(interaction).build();
        Model model = new ModelManager();
        model.addPerson(target);

        new AddressBookParser().parseCommand("unlog 1 1").execute(model);

        Person expected = new PersonBuilder(target).withInteractions().build();
        assertEquals(Optional.of(expected), model.getViewedPerson());
        assertEquals(List.of(expected), model.getFilteredPersonList());
        SupporterDetailData display = new SupporterDetailData(model.getViewedPerson());
        assertTrue(display.hasSupporter());
        assertEquals(target.getName().fullName, display.getName());
        assertEquals(List.of(), display.getInteractionEntries());
        assertEquals("Interactions (0) - newest first", display.getHistoryHeading());
        assertEquals(SupporterDetailData.MESSAGE_EMPTY_HISTORY, display.getPlaceholderMessage());
        assertEquals(Optional.empty(), expected.getLastInteractionDate());
    }

    /**
     * Verifies entry numbers, formatted dates and notes in display order.
     */
    private void assertHistory(SupporterDetailData display, List<Integer> expectedNumbers,
            List<String> expectedDates, List<String> expectedNotes) {
        List<InteractionEntry> entries = display.getInteractionEntries();
        assertEquals(expectedNumbers, entries.stream().map(InteractionEntry::getNumber).toList());
        assertEquals(expectedDates, entries.stream().map(InteractionEntry::getDate).toList());
        assertEquals(expectedNotes, entries.stream().map(InteractionEntry::getNote).toList());
    }

}
