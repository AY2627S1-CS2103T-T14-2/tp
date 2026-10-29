package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.InteractionCommandTestUtil.createModelWithPersons;
import static seedu.address.testutil.InteractionHistoryTestUtil.assertHistory;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.ui.SupporterDetailData;

/**
 * Tests dated logging from command parsing through the model to detail-panel data, without starting JavaFX.
 */
public class LogCommandIntegrationTest {

    @Test
    public void execute_backDatedLogs_updatesSelectedHistoryInDateOrder() throws Exception {
        Interaction recentInteraction = new Interaction(LocalDate.of(2024, 3, 10), "Recent call");
        Person target = new PersonBuilder().withInteractions(recentInteraction).build();
        Person other = new PersonBuilder().withName("Other Supporter").build();
        Model model = createModelWithPersons(target, other);
        model.setViewedPerson(other);
        model.updateFilteredPersonList(person -> person.getName().equals(target.getName()));
        AddressBookParser parser = new AddressBookParser();

        assertBackdatedLogSelectsTarget(parser, model, target);
        assertEqualDateLogPreservesLoggingOrder(parser, model, target, recentInteraction);
        assertLatestLogAppearsFirst(parser, model, target);

        // the current supporter filter and other supporter remain unchanged
        assertEquals(List.of(model.getViewedPerson().orElseThrow()), model.getFilteredPersonList());
        assertEquals(other, model.getAddressBook().getPersonList().get(1));
    }

    private void assertBackdatedLogSelectsTarget(AddressBookParser parser, Model model, Person target)
            throws Exception {
        // a back-dated entry selects the target but appears below the more recent interaction
        CommandResult backdatedResult = parser.parseCommand("log 1 nt/Earlier meeting dt/2024-02-29").execute(model);
        Person supporterAfterBackdatedLog = model.getViewedPerson().orElseThrow();
        SupporterDetailData displayAfterBackdatedLog = new SupporterDetailData(model.getViewedPerson());
        assertEquals("Logged interaction #1 with " + target.getName() + " (29 Feb 2024): Earlier meeting",
                backdatedResult.getFeedbackToUser());
        assertHistory(displayAfterBackdatedLog, List.of(2, 1),
                List.of("10 Mar 2024", "29 Feb 2024"), List.of("Recent call", "Earlier meeting"));
        assertEquals(2, supporterAfterBackdatedLog.getInteractionCount());
    }

    private void assertEqualDateLogPreservesLoggingOrder(AddressBookParser parser, Model model,
            Person target, Interaction recentInteraction) throws Exception {
        // another entry on the same past date refreshes the selected history and shows the later-logged entry first
        CommandResult equalDateResult = parser.parseCommand("log 1 dt/2024-02-29 nt/Second earlier meeting")
                .execute(model);
        assertEquals("Logged interaction #2 with " + target.getName() + " (29 Feb 2024): Second earlier meeting",
                equalDateResult.getFeedbackToUser());
        Person supporterAfterEqualDateLog = model.getViewedPerson().orElseThrow();
        SupporterDetailData displayAfterEqualDateLog = new SupporterDetailData(model.getViewedPerson());
        assertHistory(displayAfterEqualDateLog, List.of(3, 2, 1),
                List.of("10 Mar 2024", "29 Feb 2024", "29 Feb 2024"),
                List.of("Recent call", "Second earlier meeting", "Earlier meeting"));
        assertEquals("Interactions (3) - newest first", displayAfterEqualDateLog.getHistoryHeading());

        // display sorting leaves stored logging order unchanged
        assertEquals(List.of(recentInteraction, new Interaction(LocalDate.of(2024, 2, 29), "Earlier meeting"),
                new Interaction(LocalDate.of(2024, 2, 29), "Second earlier meeting")),
                supporterAfterEqualDateLog.getInteractions());
    }

    private void assertLatestLogAppearsFirst(AddressBookParser parser, Model model, Person target) throws Exception {
        // a new latest interaction gets the largest number and is displayed first
        CommandResult latestResult = parser.parseCommand("log 1 nt/Latest call. dt/2024-03-11").execute(model);
        assertEquals("Logged interaction #4 with " + target.getName() + " (11 Mar 2024): Latest call.",
                latestResult.getFeedbackToUser());
        SupporterDetailData displayAfterLatestLog = new SupporterDetailData(model.getViewedPerson());
        assertHistory(displayAfterLatestLog, List.of(4, 3, 2, 1),
                List.of("11 Mar 2024", "10 Mar 2024", "29 Feb 2024", "29 Feb 2024"),
                List.of("Latest call.", "Recent call", "Second earlier meeting", "Earlier meeting"));
    }
}
