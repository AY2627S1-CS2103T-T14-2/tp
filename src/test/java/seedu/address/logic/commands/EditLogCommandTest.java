package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.InteractionCommandTestUtil.assertFailurePreservesSelection;
import static seedu.address.testutil.InteractionCommandTestUtil.setInteractions;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalInteractionNotes.NOTE_PADDING_SEQUENCES;
import static seedu.address.testutil.TypicalInteractions.MIDDLE;
import static seedu.address.testutil.TypicalInteractions.NEWEST;
import static seedu.address.testutil.TypicalInteractions.OLDEST;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
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
 * Contains integration tests (interaction with the Model) and unit tests for {@code EditLogCommand}.
 */
public class EditLogCommandTest {

    private static final Optional<String> NOTE = Optional.of("Corrected note");
    private static final Optional<LocalDate> DATE = Optional.of(LocalDate.of(2026, 9, 1));
    private static final Index ENTRY = Index.fromOneBased(2);

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EditLogCommand(null, ENTRY, NOTE, DATE));
        assertThrows(NullPointerException.class, () -> new EditLogCommand(INDEX_FIRST_PERSON, null, NOTE, DATE));
        assertThrows(NullPointerException.class, () -> new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, null, DATE));
        assertThrows(NullPointerException.class, () -> new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, NOTE, null));
    }

    @Test
    public void constructor_noFields_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, Optional.empty(), Optional.empty()));
    }

    @Test
    public void constructor_invalidNote_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, Optional.of("   "), DATE));
        assertThrows(IllegalArgumentException.class, () -> new EditLogCommand(INDEX_FIRST_PERSON, ENTRY,
                Optional.of("a".repeat(Interaction.MAX_NOTE_LENGTH + 1)), DATE));
    }

    @Test
    public void constructor_paddedNote_equalsTrimmedCommand() {
        EditLogCommand expected = new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, NOTE, DATE);
        for (String padding : NOTE_PADDING_SEQUENCES) {
            EditLogCommand actual = new EditLogCommand(INDEX_FIRST_PERSON, ENTRY,
                    Optional.of(padding + NOTE.get() + padding), DATE);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, NOTE, DATE).execute(null));
    }

    @Test
    public void execute_bothFields_preservesSupporterAndSelectsTarget() {
        Person original = model.getFilteredPersonList().get(0);
        Person introducer = model.getFilteredPersonList().get(1);

        // entry #2 is stored first, so the command must map the chronological number
        Person person = new PersonBuilder(original).withIntroducer(introducer.getName().fullName)
                .withInteractions(MIDDLE, OLDEST, NEWEST).build();
        model.setPerson(original, person);
        model.setViewedPerson(introducer);
        Interaction replacement = new Interaction(DATE.get(), NOTE.get());
        Person updated = new PersonBuilder(person).withInteractions(replacement, OLDEST, NEWEST).build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(person, updated);
        expectedModel.setViewedPerson(updated);

        // the edited entry becomes the oldest, so the success message must report #1
        String message = String.format(EditLogCommand.MESSAGE_SUCCESS, 1, person.getName(),
                "01 Sep 2026", NOTE.get());
        assertCommandSuccess(new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, NOTE, DATE), model, message, expectedModel);
    }

    @Test
    public void execute_noteOnly_preservesDateAndTrimsNote() throws Exception {
        setInteractions(model, INDEX_FIRST_PERSON, MIDDLE, OLDEST, NEWEST);

        new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, Optional.of("  Corrected note  "), Optional.empty())
                .execute(model);

        assertEquals(List.of(new Interaction(MIDDLE.getDate(), NOTE.get()), OLDEST, NEWEST),
                model.getFilteredPersonList().get(0).getInteractions());
    }

    @Test
    public void execute_unicodePaddedNote_preservesInternalWhitespace() throws Exception {
        setInteractions(model, INDEX_FIRST_PERSON, MIDDLE, OLDEST, NEWEST);
        String expectedNote = "Corrected\u00a0meeting\u2003note";
        EditLogCommand command = new EditLogCommand(INDEX_FIRST_PERSON, ENTRY,
                Optional.of("\u00a0" + expectedNote + "\u2003"), Optional.empty());

        CommandResult result = command.execute(model);

        Person updatedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(List.of(new Interaction(MIDDLE.getDate(), expectedNote), OLDEST, NEWEST),
                updatedPerson.getInteractions());
        assertEquals(String.format(EditLogCommand.MESSAGE_SUCCESS, 2, updatedPerson.getName(),
                "16 Sep 2026", expectedNote), result.getFeedbackToUser());
    }

    @Test
    public void execute_dateOnly_preservesNoteAndReportsNewNumber() throws Exception {
        Person person = setInteractions(model, INDEX_FIRST_PERSON, MIDDLE, OLDEST, NEWEST);
        LocalDate newDate = LocalDate.of(2026, 9, 25);

        CommandResult result = new EditLogCommand(INDEX_FIRST_PERSON, ENTRY,
                Optional.empty(), Optional.of(newDate)).execute(model);

        Person updated = model.getFilteredPersonList().get(0);
        assertEquals(List.of(new Interaction(newDate, MIDDLE.getNote()), OLDEST, NEWEST), updated.getInteractions());
        assertEquals(List.of(0, 2, 1), updated.getInteractionIndicesInDisplayOrder());
        assertEquals(String.format(EditLogCommand.MESSAGE_SUCCESS, 3, person.getName(), "25 Sep 2026", "Meeting"),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_dateBecomesEqual_preservesLoggingPosition() throws Exception {
        setInteractions(model, INDEX_FIRST_PERSON, OLDEST, MIDDLE, NEWEST);

        // the first logged entry must stay behind the second logged entry when their dates become equal
        new EditLogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1), Optional.empty(),
                Optional.of(MIDDLE.getDate())).execute(model);

        Person updated = model.getFilteredPersonList().get(0);
        assertEquals(List.of(new Interaction(MIDDLE.getDate(), OLDEST.getNote()), MIDDLE, NEWEST),
                updated.getInteractions());
        assertEquals(List.of(2, 1, 0), updated.getInteractionIndicesInDisplayOrder());
    }

    @Test
    public void execute_identicalEntries_editsSelectedStoredPosition() throws Exception {
        Interaction sameDateInteraction = new Interaction(MIDDLE.getDate(), "Different note");
        setInteractions(model, INDEX_FIRST_PERSON, MIDDLE, sameDateInteraction,
                new Interaction(MIDDLE.getDate(), MIDDLE.getNote()));

        // edit displayed entry #3 at stored index 2, leaving the identical entry at index 0 unchanged
        new EditLogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(3), NOTE, Optional.empty()).execute(model);

        assertEquals(List.of(MIDDLE, sameDateInteraction, new Interaction(MIDDLE.getDate(), NOTE.get())),
                model.getFilteredPersonList().get(0).getInteractions());
    }

    @Test
    public void execute_unchangedValues_succeeds() throws Exception {
        Person person = setInteractions(model, INDEX_FIRST_PERSON, OLDEST);

        new EditLogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1), Optional.of(OLDEST.getNote()),
                Optional.of(OLDEST.getDate())).execute(model);

        assertEquals(person, model.getFilteredPersonList().get(0));
        assertEquals(Optional.of(person), model.getViewedPerson());
    }

    @Test
    public void execute_unchangedValues_reportsNothingChangedAndSavesNoUndoPoint() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        Person person = setInteractions(model, INDEX_FIRST_PERSON, OLDEST, MIDDLE);
        model.commitAddressBookIfChanged();
        String expectedMessage = String.format(EditLogCommand.MESSAGE_NOT_CHANGED, 2, person.getName());

        // the same note only, the same date only, and both
        assertEquals(expectedMessage, new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, Optional.of(MIDDLE.getNote()),
                Optional.empty()).execute(model).getFeedbackToUser());
        assertEquals(expectedMessage, new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, Optional.empty(),
                Optional.of(MIDDLE.getDate())).execute(model).getFeedbackToUser());
        assertEquals(expectedMessage, new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, Optional.of(MIDDLE.getNote()),
                Optional.of(MIDDLE.getDate())).execute(model).getFeedbackToUser());

        model.commitAddressBookIfChanged();
        model.undoAddressBook();

        // the undo reverts setInteractions, as none of the unchanged edits saved an undo point
        assertEquals(original, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_validFilteredIndex_preservesFilter() throws Exception {
        // the second supporter now has displayed index 1
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person person = setInteractions(model, INDEX_FIRST_PERSON, MIDDLE, OLDEST);
        Person updated = new PersonBuilder(person)
                .withInteractions(new Interaction(MIDDLE.getDate(), NOTE.get()), OLDEST).build();

        new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, NOTE, Optional.empty()).execute(model);

        assertEquals(List.of(updated), model.getFilteredPersonList());
        assertEquals(Optional.of(updated), model.getViewedPerson());
    }

    @Test
    public void execute_invalidSupporterIndex_preservesDataAndSelection() {
        model.setViewedPerson(model.getFilteredPersonList().get(0));
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertFailurePreservesSelection(new EditLogCommand(invalidIndex, ENTRY, NOTE, DATE), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidFilteredIndex_preservesDataAndSelection() {
        model.setViewedPerson(model.getFilteredPersonList().get(1));
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // supporter exists in the full list but is not displayed
        assertFailurePreservesSelection(new EditLogCommand(INDEX_SECOND_PERSON, ENTRY, NOTE, DATE), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidInteractionIndex_preservesDataAndSelection() {
        setInteractions(model, INDEX_FIRST_PERSON, OLDEST);
        model.setViewedPerson(model.getFilteredPersonList().get(1));

        assertFailurePreservesSelection(new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, NOTE, DATE), model,
                Messages.MESSAGE_INVALID_INTERACTION_INDEX);
    }

    @Test
    public void execute_emptyHistory_preservesDataAndSelection() {
        setInteractions(model, INDEX_FIRST_PERSON);
        model.setViewedPerson(model.getFilteredPersonList().get(1));

        assertFailurePreservesSelection(
                new EditLogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1), NOTE, DATE), model,
                Messages.MESSAGE_INVALID_INTERACTION_INDEX);
    }

    @Test
    public void equals() {
        EditLogCommand command = new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, NOTE, DATE);

        // same object and same values -> returns true
        assertTrue(command.equals(command));
        assertTrue(command.equals(new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, NOTE, DATE)));

        // null and different types -> returns false
        assertFalse(command.equals(null));
        assertFalse(command.equals(1));

        // changing either index or either field -> returns false
        assertFalse(command.equals(new EditLogCommand(INDEX_SECOND_PERSON, ENTRY, NOTE, DATE)));
        assertFalse(command.equals(new EditLogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1), NOTE, DATE)));
        assertFalse(command.equals(new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, Optional.empty(), DATE)));
        assertFalse(command.equals(new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, NOTE, Optional.empty())));
        assertFalse(command.equals(new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, Optional.of("Other"), DATE)));
        assertFalse(command.equals(new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, NOTE,
                Optional.of(MIDDLE.getDate()))));
    }

    @Test
    public void toStringMethod() {
        EditLogCommand command = new EditLogCommand(INDEX_FIRST_PERSON, ENTRY, NOTE, DATE);
        assertEquals(EditLogCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", interactionIndex=" + ENTRY + ", note=" + NOTE + ", date=" + DATE + "}", command.toString());
    }

}
