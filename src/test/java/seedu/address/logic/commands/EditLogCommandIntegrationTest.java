package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.InteractionHistoryTestUtil.assertHistory;

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

/**
 * Tests interaction editing from command parsing through the model to detail-panel data, without starting JavaFX.
 */
public class EditLogCommandIntegrationTest {

    @Test
    public void execute_dateThenNoteEdit_usesRefreshedNumbersAndPreservesFilter() throws Exception {
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

        // filtered supporter #1 is second in storage, and entry #2 is first in its stored history
        CommandResult result = parser.parseCommand("editlog 1 2 dt/2024-03-15").execute(model);
        Interaction edited = new Interaction(LocalDate.of(2024, 3, 15), "Middle meeting");
        Person expected = new PersonBuilder(target).withInteractions(edited, oldest, newest).build();
        assertEquals(Optional.of(expected), model.getViewedPerson());
        assertEquals(List.of(expected), model.getFilteredPersonList());
        assertEquals(List.of(other, expected), model.getAddressBook().getPersonList());
        assertEquals("Edited interaction #3 with " + target.getName() + " (15 Mar 2024): Middle meeting.",
                result.getFeedbackToUser());
        SupporterDetailData displayAfterDateEdit = new SupporterDetailData(model.getViewedPerson());
        assertHistory(displayAfterDateEdit, List.of(3, 2, 1),
                List.of("15 Mar 2024", "10 Mar 2024", "20 Feb 2024"),
                List.of("Middle meeting", "Recent call", "Earlier call"));
        assertEquals("Interactions (3) - newest first", displayAfterDateEdit.getHistoryHeading());
        assertEquals(other.getName().fullName, displayAfterDateEdit.getIntroducer());
        assertEquals(Optional.of(edited.getDate()), model.getViewedPerson().orElseThrow().getLastInteractionDate());

        // use the refreshed number to edit the same interaction's note while retaining its new date
        CommandResult nextResult = parser.parseCommand("editlog 1 3 nt/Corrected meeting").execute(model);
        Person nextExpected = new PersonBuilder(target)
                .withInteractions(new Interaction(edited.getDate(), "Corrected meeting"), oldest, newest).build();
        assertEquals(Optional.of(nextExpected), model.getViewedPerson());
        assertEquals(List.of(nextExpected), model.getFilteredPersonList());
        assertEquals(List.of(other, nextExpected), model.getAddressBook().getPersonList());
        assertEquals("Edited interaction #3 with " + target.getName() + " (15 Mar 2024): Corrected meeting.",
                nextResult.getFeedbackToUser());
        SupporterDetailData displayAfterNoteEdit = new SupporterDetailData(model.getViewedPerson());
        assertHistory(displayAfterNoteEdit, List.of(3, 2, 1),
                List.of("15 Mar 2024", "10 Mar 2024", "20 Feb 2024"),
                List.of("Corrected meeting", "Recent call", "Earlier call"));
    }

    @Test
    public void execute_backDateToEqualDate_preservesLoggingOrderAndUpdatesLastContact() throws Exception {
        Interaction newest = new Interaction(LocalDate.of(2024, 3, 10), "Recent meeting");
        Interaction earlier = new Interaction(LocalDate.of(2024, 2, 29), "Earlier call");
        Person target = new PersonBuilder().withInteractions(newest, earlier).build();
        Model model = new ModelManager();
        model.addPerson(target);

        // the first logged entry moves below the later-logged entry when their dates become equal
        CommandResult result = new AddressBookParser()
                .parseCommand("editlog 1 2 dt/2024-02-29 nt/Corrected meeting").execute(model);

        Person expected = new PersonBuilder(target)
                .withInteractions(new Interaction(earlier.getDate(), "Corrected meeting"), earlier).build();
        assertEquals(Optional.of(expected), model.getViewedPerson());
        assertEquals("Edited interaction #1 with " + target.getName() + " (29 Feb 2024): Corrected meeting.",
                result.getFeedbackToUser());
        SupporterDetailData display = new SupporterDetailData(model.getViewedPerson());
        assertHistory(display, List.of(2, 1), List.of("29 Feb 2024", "29 Feb 2024"),
                List.of("Earlier call", "Corrected meeting"));
        assertEquals(Optional.of(earlier.getDate()), model.getViewedPerson().orElseThrow().getLastInteractionDate());
    }

    @Test
    public void execute_identicalEntries_editsOnlySelectedOccurrence() throws Exception {
        LocalDate date = LocalDate.of(2024, 2, 29);
        Interaction duplicate = new Interaction(date, "Meeting");
        Interaction between = new Interaction(date, "Follow-up");
        Person target = new PersonBuilder().withInteractions(duplicate, between,
                new Interaction(date, "Meeting")).build();
        Model model = new ModelManager();
        model.addPerson(target);

        // #3 is the later duplicate, so the identical entry at stored index 0 must remain unchanged
        new AddressBookParser().parseCommand("editlog 1 3 nt/Corrected meeting").execute(model);

        assertEquals(List.of(duplicate, between, new Interaction(date, "Corrected meeting")),
                model.getViewedPerson().orElseThrow().getInteractions());
        SupporterDetailData display = new SupporterDetailData(model.getViewedPerson());
        assertHistory(display, List.of(3, 2, 1),
                List.of("29 Feb 2024", "29 Feb 2024", "29 Feb 2024"),
                List.of("Corrected meeting", "Follow-up", "Meeting"));
    }
}
