package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class QuietCommandTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 21);
    private static final ZoneId ZONE = ZoneId.of("Asia/Singapore");
    private static final Clock CLOCK = Clock.fixed(TODAY.atStartOfDay(ZONE).toInstant(), ZONE);

    @Test
    public void constructor_invalidDays_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new QuietCommand(0));
        assertThrows(IllegalArgumentException.class, () -> new QuietCommand(-1));
    }

    @Test
    public void constructor_nullClock_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new QuietCommand(60, null));
    }

    @Test
    public void execute_emptyAddressBook_returnsZeroCount() {
        Model model = new ModelManager();

        CommandResult result = new QuietCommand(60, CLOCK).execute(model);

        assertEquals(String.format(QuietCommand.MESSAGE_SUCCESS, 0, 60), result.getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_thresholdBoundary_includesOnlyDatesStrictlyBeforeCutoff() {
        Person older = createSupporter("Older", TODAY.minusDays(61));
        Model model = createModelWith(createSupporter("Recent", TODAY.minusDays(59)),
                createSupporter("Boundary", TODAY.minusDays(60)), older, createSupporter("Today", TODAY));

        CommandResult result = new QuietCommand(60, CLOCK).execute(model);

        assertEquals(List.of(older), model.getFilteredPersonList());
        assertEquals(String.format(QuietCommand.MESSAGE_SUCCESS, 1, 60), result.getFeedbackToUser());
    }

    @Test
    public void execute_noInteractions_includedBeforeOldestDate() {
        Person oldest = createSupporter("Oldest", TODAY.minusDays(300));
        Person neverContacted = createSupporter("Never Contacted");
        Person later = createSupporter("Later", TODAY.minusDays(90));
        Model model = createModelWith(later, oldest, neverContacted);

        new QuietCommand(60, CLOCK).execute(model);

        assertEquals(List.of(neverContacted, oldest, later), model.getFilteredPersonList());
    }

    @Test
    public void execute_declinedSupporters_excludedWithOrWithoutInteractions() {
        Person active = createSupporter("Active");
        Model model = createModelWith(new PersonBuilder(createSupporter("Declined With History", TODAY.minusDays(100)))
                .withStage("declined").build(), active,
                new PersonBuilder(createSupporter("Declined Without History")).withStage("declined").build());

        new QuietCommand(60, CLOCK).execute(model);

        assertEquals(List.of(active), model.getFilteredPersonList());
    }

    @Test
    public void execute_backdatedLastEntry_usesLatestInteractionDate() {
        Person recent = createSupporter("Recent", TODAY.minusDays(100), TODAY.minusDays(10), TODAY.minusDays(200));
        Person old = createSupporter("Old", TODAY.minusDays(90), TODAY.minusDays(150));
        Model model = createModelWith(recent, old);

        new QuietCommand(60, CLOCK).execute(model);

        assertEquals(List.of(old), model.getFilteredPersonList());
    }

    @Test
    public void execute_equalDatesAndNoHistory_preservesUnderlyingOrder() {
        Person firstOld = createSupporter("First Old", TODAY.minusDays(90));
        Person secondOld = createSupporter("Second Old", TODAY.minusDays(90));
        Person firstNever = createSupporter("First Never");
        Person secondNever = createSupporter("Second Never");
        Model model = createModelWith(firstOld, firstNever, secondOld, secondNever);

        new QuietCommand(60, CLOCK).execute(model);

        assertEquals(List.of(firstNever, secondNever, firstOld, secondOld), model.getFilteredPersonList());
    }

    @Test
    public void execute_largeThreshold_doesNotOverflowDateArithmetic() {
        Person neverContacted = createSupporter("Never Contacted");
        Model model = createModelWith(createSupporter("Old", LocalDate.of(1900, 1, 1)), neverContacted);

        new QuietCommand(Integer.MAX_VALUE, CLOCK).execute(model);

        assertEquals(List.of(neverContacted), model.getFilteredPersonList());
    }

    @Test
    public void execute_differentDays_changesThreshold() {
        Person old = createSupporter("Old", TODAY.minusDays(90));
        Person recent = createSupporter("Recent", TODAY.minusDays(45));
        Model model = createModelWith(recent, old);

        new QuietCommand(60, CLOCK).execute(model);
        assertEquals(List.of(old), model.getFilteredPersonList());

        new QuietCommand(30, CLOCK).execute(model);
        assertEquals(List.of(old, recent), model.getFilteredPersonList());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new QuietCommand(60, CLOCK).execute(null));
    }

    @Test
    public void equals() {
        QuietCommand command = new QuietCommand();
        assertTrue(command.equals(command));
        assertTrue(command.equals(new QuietCommand(60)));
        assertEquals(command.hashCode(), new QuietCommand(60).hashCode());
        assertFalse(command.equals(new QuietCommand(30)));
        assertFalse(command.equals(null));
        assertFalse(command.equals("quiet"));
    }

    @Test
    public void toStringMethod() {
        assertEquals(QuietCommand.class.getCanonicalName() + "{days=30}", new QuietCommand(30).toString());
    }

    private Person createSupporter(String name, LocalDate... dates) {
        Interaction[] interactions = Arrays.stream(dates).map(date -> new Interaction(date, "Contact"))
                .toArray(Interaction[]::new);
        return new PersonBuilder().withName(name).withInteractions(interactions).build();
    }

    private Model createModelWith(Person... supporters) {
        AddressBook addressBook = new AddressBook();
        for (Person supporter : supporters) {
            addressBook.addPerson(supporter);
        }
        return new ModelManager(addressBook, new UserPrefs());
    }
}
