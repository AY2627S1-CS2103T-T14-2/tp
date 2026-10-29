package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.LogTestUtil.captureLogs;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonMatchesSearchPredicate;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks diagnostic summaries and assertions that detect broken internal model contracts.
 */
public class CommandDiagnosticsTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 21);
    private static final ZoneId ZONE = ZoneId.of("Asia/Singapore");
    private static final Clock CLOCK = Clock.fixed(TODAY.atStartOfDay(ZONE).toInstant(), ZONE);

    @Test
    public void assertions_auditedClasses_areEnabled() {
        for (Class<?> source : List.of(FindCommand.class, QuietCommand.class, DeleteCommand.class,
                PersonMatchesSearchPredicate.class)) {
            assertTrue(source.desiredAssertionStatus(), source.getSimpleName());
        }
    }

    @Test
    public void execute_findModelIgnoresPredicate_throwsAssertionError() {
        ModelManager model = new ModelManager() {
            @Override
            public void updateFilteredPersonList(Predicate<Person> predicate) {
                // Deliberately violate the model contract to exercise the assertion.
            }
        };
        model.addPerson(new PersonBuilder().build());

        assertThrows(AssertionError.class, () -> new FindCommand(person -> false).execute(model));
    }

    @Test
    public void execute_quietModelIgnoresPredicate_throwsAssertionError() {
        ModelManager model = new ModelManager() {
            @Override
            public void updateSortedPersonList(Predicate<Person> predicate, Comparator<Person> comparator) {
                // Deliberately leave an ineligible supporter in the displayed list.
            }
        };
        model.addPerson(new PersonBuilder().withStage("declined").build());

        assertThrows(AssertionError.class, () -> new QuietCommand(60, CLOCK).execute(model));
    }

    @Test
    public void execute_quietModelIgnoresOrdering_throwsAssertionError() {
        ModelManager model = new ModelManager() {
            @Override
            public void updateSortedPersonList(Predicate<Person> predicate, Comparator<Person> comparator) {
                super.updateFilteredPersonList(predicate);
            }
        };
        model.addPerson(new PersonBuilder().withName("Later")
                .withInteractions(new Interaction(TODAY.minusDays(90), "Called")).build());
        model.addPerson(new PersonBuilder().withName("Earlier")
                .withInteractions(new Interaction(TODAY.minusDays(120), "Called")).build());

        assertThrows(AssertionError.class, () -> new QuietCommand(60, CLOCK).execute(model));
    }

    @Test
    public void execute_deleteModelRetainsSupporter_throwsAssertionError() {
        ModelManager model = new ModelManager() {
            @Override
            public void deletePerson(Person person) {
                // Deliberately keep the supporter in storage.
            }
        };
        model.addPerson(new PersonBuilder().build());

        assertThrows(AssertionError.class, () -> new DeleteCommand(INDEX_FIRST_PERSON).execute(model));
    }

    @Test
    public void execute_deleteModelRetainsIntroduction_throwsAssertionError() {
        ModelManager model = new ModelManager() {
            @Override
            public int replaceIntroducer(Name introducer, Optional<Name> replacement) {
                return 0;
            }
        };
        model.addPerson(new PersonBuilder().withName("Introducer").build());
        model.addPerson(new PersonBuilder().withName("Introduced").withIntroducer("Introducer").build());

        assertThrows(AssertionError.class, () -> new DeleteCommand(INDEX_FIRST_PERSON).execute(model));
    }

    @Test
    public void execute_find_logsCountsWithoutSearchTextOrSupporterDetails() throws Throwable {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        List<String> messages = captureLogs(FindCommand.class, () ->
                new FindCommand(person -> true).execute(model));

        assertEquals(List.of("Applied find filter: matches=1, total=1"), messages);
    }

    @Test
    public void execute_quiet_logsThresholdDateAndCount() throws Throwable {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        List<String> messages = captureLogs(QuietCommand.class, () -> new QuietCommand(60, CLOCK).execute(model));

        assertEquals(List.of("Applied quiet filter: days=60, date=2026-10-21, matches=1"), messages);
    }

    @Test
    public void execute_delete_logsRemovalCountsWithoutSupporterDetails() throws Throwable {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Introducer")
                .withInteractions(new Interaction(TODAY, "Private note")).build());
        model.addPerson(new PersonBuilder().withName("Introduced").withIntroducer("Introducer").build());
        List<String> messages = captureLogs(DeleteCommand.class, () ->
                new DeleteCommand(INDEX_FIRST_PERSON).execute(model));

        assertEquals(List.of("Deleted supporter: displayed index=1, interactions removed=1, introductions cleared=1"),
                messages);
    }

    @Test
    public void execute_invalidDelete_logsRejectionAndKeepsNormalError() throws Throwable {
        ModelManager model = new ModelManager();
        List<String> messages = captureLogs(DeleteCommand.class, () -> {
            CommandException error = assertThrows(CommandException.class, () ->
                    new DeleteCommand(INDEX_FIRST_PERSON).execute(model));
            assertEquals(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, error.getMessage());
        });

        assertEquals(List.of("Rejected delete: displayed index=1, displayed supporters=0"), messages);
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void constructor_searchPredicate_logsOnlyCriterionCounts() throws Throwable {
        List<String> messages = captureLogs(PersonMatchesSearchPredicate.class, () ->
                new PersonMatchesSearchPredicate(List.of("Private Name"), List.of("Private Organisation"),
                        List.of(), List.of(), List.of("+65 9123-4567"), List.of("private@example.com")));

        String expected = "Created search predicate: names=1, organisations=1, tags=0, stages=0, phones=1, emails=1";
        assertEquals(List.of(expected), messages);
    }

}
