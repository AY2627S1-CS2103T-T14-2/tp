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
        Person older = supporter("Older", TODAY.minusDays(61));
        Model model = modelWith(supporter("Recent", TODAY.minusDays(59)),
                supporter("Boundary", TODAY.minusDays(60)), older, supporter("Today", TODAY));

        CommandResult result = new QuietCommand(60, CLOCK).execute(model);

        assertEquals(List.of(older), model.getFilteredPersonList());
        assertEquals(String.format(QuietCommand.MESSAGE_SUCCESS, 1, 60), result.getFeedbackToUser());
    }

    @Test
    public void execute_noInteractions_includedBeforeOldestDate() {
        Person oldest = supporter("Oldest", TODAY.minusDays(300));
        Person neverContacted = supporter("Never Contacted");
        Person later = supporter("Later", TODAY.minusDays(90));
        Model model = modelWith(later, oldest, neverContacted);

        new QuietCommand(60, CLOCK).execute(model);

        assertEquals(List.of(neverContacted, oldest, later), model.getFilteredPersonList());
    }

    @Test
    public void execute_declinedSupporters_excludedWithOrWithoutInteractions() {
        Person active = supporter("Active");
        Model model = modelWith(new PersonBuilder(supporter("Declined With History", TODAY.minusDays(100)))
                .withStage("declined").build(), active,
                new PersonBuilder(supporter("Declined Without History")).withStage("declined").build());

        new QuietCommand(60, CLOCK).execute(model);

        assertEquals(List.of(active), model.getFilteredPersonList());
    }

    @Test
    public void execute_backdatedLastEntry_usesLatestInteractionDate() {
        Person recent = supporter("Recent", TODAY.minusDays(100), TODAY.minusDays(10), TODAY.minusDays(200));
        Person old = supporter("Old", TODAY.minusDays(90), TODAY.minusDays(150));
        Model model = modelWith(recent, old);

        new QuietCommand(60, CLOCK).execute(model);

        assertEquals(List.of(old), model.getFilteredPersonList());
    }

    @Test
    public void execute_equalDatesAndNoHistory_preservesUnderlyingOrder() {
        Person firstOld = supporter("First Old", TODAY.minusDays(90));
        Person secondOld = supporter("Second Old", TODAY.minusDays(90));
        Person firstNever = supporter("First Never");
        Person secondNever = supporter("Second Never");
        Model model = modelWith(firstOld, firstNever, secondOld, secondNever);

        new QuietCommand(60, CLOCK).execute(model);

        assertEquals(List.of(firstNever, secondNever, firstOld, secondOld), model.getFilteredPersonList());
    }

    @Test
    public void execute_largeThreshold_doesNotOverflowDateArithmetic() {
        Person neverContacted = supporter("Never Contacted");
        Model model = modelWith(supporter("Old", LocalDate.of(1900, 1, 1)), neverContacted);

        new QuietCommand(Integer.MAX_VALUE, CLOCK).execute(model);

        assertEquals(List.of(neverContacted), model.getFilteredPersonList());
    }

    @Test
    public void execute_differentDays_changesThreshold() {
        Person old = supporter("Old", TODAY.minusDays(90));
        Person recent = supporter("Recent", TODAY.minusDays(45));
        Model model = modelWith(recent, old);

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

    private Person supporter(String name, LocalDate... dates) {
        Interaction[] interactions = Arrays.stream(dates).map(date -> new Interaction(date, "Contact"))
                .toArray(Interaction[]::new);
        return new PersonBuilder().withName(name).withInteractions(interactions).build();
    }

    private Model modelWith(Person... supporters) {
        AddressBook addressBook = new AddressBook();
        for (Person supporter : supporters) {
            addressBook.addPerson(supporter);
        }
        return new ModelManager(addressBook, new UserPrefs());
    }
}
