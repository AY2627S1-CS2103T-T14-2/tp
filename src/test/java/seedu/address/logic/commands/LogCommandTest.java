package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code LogCommand}.
 */
public class LogCommandTest {

    private static final String VALID_NOTE = "Called to discuss the year-end appeal";
    private static final LocalDate FIXED_DATE = LocalDate.of(2026, 9, 16);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-09-16T12:00:00Z"), ZoneOffset.UTC);

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LogCommand(null, VALID_NOTE));
        assertThrows(NullPointerException.class, () -> new LogCommand(INDEX_FIRST_PERSON, null));
        assertThrows(NullPointerException.class, () -> new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, (Clock) null));
        assertThrows(NullPointerException.class, () ->
                new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, (Optional<LocalDate>) null));
    }

    @Test
    public void constructor_invalidNote_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new LogCommand(INDEX_FIRST_PERSON, "   "));
        assertThrows(IllegalArgumentException.class, () ->
                new LogCommand(INDEX_FIRST_PERSON, "a".repeat(Interaction.MAX_NOTE_LENGTH + 1)));
    }

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person personToUpdate = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Interaction interaction = new Interaction(FIXED_DATE, VALID_NOTE);
        Person updatedPerson = new PersonBuilder(personToUpdate).withInteractions(interaction).build();
        LogCommand logCommand = new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, FIXED_CLOCK);

        String expectedMessage = String.format(LogCommand.MESSAGE_SUCCESS, 1, personToUpdate.getName(),
                "16 Sep 2026", VALID_NOTE);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToUpdate, updatedPerson);
        expectedModel.setViewedPerson(updatedPerson);

        assertCommandSuccess(logCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_explicitPastDate_appendsAndPreservesSupporter() {
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Interaction existingInteraction = new Interaction(FIXED_DATE, "Existing meeting");
        Person personToUpdate = new PersonBuilder(originalPerson).withInteractions(existingInteraction).build();
        model.setPerson(originalPerson, personToUpdate);
        LocalDate pastDate = FIXED_DATE.minusDays(10);
        Person updatedPerson = new PersonBuilder(personToUpdate)
                .withInteractions(existingInteraction, new Interaction(pastDate, VALID_NOTE)).build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToUpdate, updatedPerson);
        expectedModel.setViewedPerson(updatedPerson);

        LogCommand command = new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, Optional.of(pastDate), FIXED_CLOCK);
        String expectedMessage = String.format(LogCommand.MESSAGE_SUCCESS, 1, personToUpdate.getName(),
                "06 Sep 2026", VALID_NOTE);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_explicitToday_usesSpecifiedDate() throws Exception {
        new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, Optional.of(FIXED_DATE), FIXED_CLOCK).execute(model);

        assertEquals(List.of(new Interaction(FIXED_DATE, VALID_NOTE)),
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()).getInteractions());
    }

    @Test
    public void execute_emptyDate_usesClockAtExecution() throws Exception {
        // the clock must only be consulted when execute is called, not when the command is constructed
        boolean[] executing = {false};
        Clock executionClock = new Clock() {

            @Override
            public java.time.ZoneId getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(java.time.ZoneId zone) {
                return FIXED_CLOCK.withZone(zone);
            }

            @Override
            public Instant instant() {
                assertTrue(executing[0]);
                return FIXED_CLOCK.instant();
            }
        };
        LogCommand command = new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, Optional.empty(), executionClock);
        executing[0] = true;
        command.execute(model);

        assertEquals(List.of(new Interaction(FIXED_DATE, VALID_NOTE)),
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()).getInteractions());
    }

    @Test
    public void execute_singleDigitDay_dateHasLeadingZero() throws Exception {
        Clock singleDigitDayClock = Clock.fixed(Instant.parse("2026-08-05T12:00:00Z"), ZoneOffset.UTC);
        Person personToUpdate = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        CommandResult result = new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, singleDigitDayClock).execute(model);

        assertEquals(String.format(LogCommand.MESSAGE_SUCCESS, 1, personToUpdate.getName(),
                "05 Aug 2026", VALID_NOTE), result.getFeedbackToUser());
    }

    @Test
    public void execute_anotherSupporterViewed_selectsUpdatedTarget() throws Exception {
        Person previouslyViewedPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        model.setViewedPerson(previouslyViewedPerson);
        Person personToUpdate = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person updatedPerson = new PersonBuilder(personToUpdate)
                .withInteractions(new Interaction(FIXED_DATE, VALID_NOTE))
                .build();

        new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, FIXED_CLOCK).execute(model);

        assertEquals(Optional.of(updatedPerson), model.getViewedPerson());
    }

    @Test
    public void execute_existingIdenticalInteraction_appendsDuplicate() throws Exception {
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Interaction existingInteraction = new Interaction(FIXED_DATE, VALID_NOTE);
        Person personWithInteraction = new PersonBuilder(originalPerson)
                .withInteractions(existingInteraction)
                .build();
        model.setPerson(originalPerson, personWithInteraction);

        // identical interactions are valid because the same interaction can happen more than once
        CommandResult result = new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, FIXED_CLOCK).execute(model);

        Person updatedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(List.of(existingInteraction, existingInteraction), updatedPerson.getInteractions());
        assertEquals(String.format(LogCommand.MESSAGE_SUCCESS, 2, personWithInteraction.getName(),
                "16 Sep 2026", VALID_NOTE), result.getFeedbackToUser());
    }

    @Test
    public void execute_supporterDetails_detailsPreserved() throws Exception {
        Person personToUpdate = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());

        new LogCommand(INDEX_SECOND_PERSON, VALID_NOTE, FIXED_CLOCK).execute(model);

        Person updatedPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        assertEquals(personToUpdate.getName(), updatedPerson.getName());
        assertEquals(personToUpdate.getPhone(), updatedPerson.getPhone());
        assertEquals(personToUpdate.getEmail(), updatedPerson.getEmail());
        assertEquals(personToUpdate.getAddress(), updatedPerson.getAddress());
        assertEquals(personToUpdate.getOrganisation(), updatedPerson.getOrganisation());
        assertEquals(personToUpdate.getStage(), updatedPerson.getStage());
        assertEquals(personToUpdate.getTags(), updatedPerson.getTags());
    }

    @Test
    public void execute_supporterWithIntroducer_introducerPreserved() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person introducedPerson = new PersonBuilder(firstPerson)
                .withIntroducer(secondPerson.getName().fullName)
                .build();
        model.setPerson(firstPerson, introducedPerson);

        new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, FIXED_CLOCK).execute(model);

        Person updatedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(introducedPerson.getIntroducer(), updatedPerson.getIntroducer());
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        LogCommand logCommand = new LogCommand(outOfBoundIndex, VALID_NOTE, FIXED_CLOCK);
        Person previouslyViewedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setViewedPerson(previouslyViewedPerson);

        assertCommandFailure(logCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertEquals(Optional.of(previouslyViewedPerson), model.getViewedPerson());
    }

    @Test
    public void execute_validIndexFilteredList_successAndFilterKept() {
        // show only the second supporter, whose displayed index is now 1
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person personToUpdate = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Interaction interaction = new Interaction(FIXED_DATE, VALID_NOTE);
        Person updatedPerson = new PersonBuilder(personToUpdate).withInteractions(interaction).build();
        LogCommand logCommand = new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, FIXED_CLOCK);

        String expectedMessage = String.format(LogCommand.MESSAGE_SUCCESS, 1, personToUpdate.getName(),
                "16 Sep 2026", VALID_NOTE);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        expectedModel.setPerson(personToUpdate, updatedPerson);
        expectedModel.setViewedPerson(updatedPerson);

        assertCommandSuccess(logCommand, model, expectedMessage, expectedModel);
        assertEquals(List.of(updatedPerson), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        Person previouslyViewedPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        model.setViewedPerson(previouslyViewedPerson);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // supporter exists in the full list but is not displayed
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());
        LogCommand logCommand = new LogCommand(outOfBoundIndex, VALID_NOTE, FIXED_CLOCK);

        assertCommandFailure(logCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertEquals(Optional.of(previouslyViewedPerson), model.getViewedPerson());
    }

    @Test
    public void equals() {
        LogCommand logFirstCommand = new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, FIXED_CLOCK);
        LogCommand logFirstCommandCopy = new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, FIXED_CLOCK);
        LogCommand logSecondCommand = new LogCommand(INDEX_SECOND_PERSON, VALID_NOTE, FIXED_CLOCK);
        LogCommand logDifferentNoteCommand = new LogCommand(INDEX_FIRST_PERSON, "Emailed", FIXED_CLOCK);

        // same object -> returns true
        assertTrue(logFirstCommand.equals(logFirstCommand));

        // same values -> returns true
        assertTrue(logFirstCommand.equals(logFirstCommandCopy));

        LogCommand datedCommand = new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, Optional.of(FIXED_DATE));
        assertTrue(datedCommand.equals(new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, Optional.of(FIXED_DATE))));
        assertFalse(datedCommand.equals(logFirstCommand));
        assertFalse(datedCommand.equals(new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE,
                Optional.of(FIXED_DATE.minusDays(1)))));

        // different types -> returns false
        assertFalse(logFirstCommand.equals(1));

        // null -> returns false
        assertFalse(logFirstCommand.equals(null));

        // different index -> returns false
        assertFalse(logFirstCommand.equals(logSecondCommand));

        // different note -> returns false
        assertFalse(logFirstCommand.equals(logDifferentNoteCommand));
    }

    @Test
    public void toStringMethod() {
        LogCommand logCommand = new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, FIXED_CLOCK);
        String expected = LogCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", note=" + VALID_NOTE + ", date=Optional.empty}";
        assertEquals(expected, logCommand.toString());
    }
    @Test
    public void toStringMethod_explicitDate() {
        LogCommand command = new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, Optional.of(FIXED_DATE));
        String expected = LogCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", note=" + VALID_NOTE + ", date=Optional[2026-09-16]}";
        assertEquals(expected, command.toString());
    }

}
