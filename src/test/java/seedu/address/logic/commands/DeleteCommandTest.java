package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteCommandTest {

    private static final Interaction FIRST_INTERACTION =
            new Interaction(LocalDate.of(2026, 10, 12), "Called about the year-end appeal");
    private static final Interaction SECOND_INTERACTION =
            new Interaction(LocalDate.of(2026, 10, 13), "Discussed the volunteering day");

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_reportsZeroInteractions() {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = "Deleted supporter: " + Messages.format(personToDelete)
                + "\n0 logged interaction(s) were deleted with them.";

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_oneInteraction_reportsOneInteraction() {
        assertDeleteReportsInteractions(FIRST_INTERACTION);
    }

    @Test
    public void execute_multipleInteractions_reportsAllInteractions() {
        assertDeleteReportsInteractions(FIRST_INTERACTION, SECOND_INTERACTION);
    }

    @Test
    public void execute_duplicateInteractions_countsEveryEntry() {
        assertDeleteReportsInteractions(FIRST_INTERACTION, FIRST_INTERACTION, SECOND_INTERACTION);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person viewedPerson = new PersonBuilder(originalPerson).withInteractions(FIRST_INTERACTION).build();
        model.setPerson(originalPerson, viewedPerson);
        model.setViewedPerson(viewedPerson);
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertEquals(Optional.of(viewedPerson), model.getViewedPerson());
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete), personToDelete.getInteractions().size());

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);
        showNoPerson(expectedModel);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Person viewedPerson = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        model.setViewedPerson(viewedPerson);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertEquals(Optional.of(viewedPerson), model.getViewedPerson());
    }

    @Test
    public void execute_viewedSupporter_clearsViewedPerson() throws Exception {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        new ViewCommand(INDEX_FIRST_PERSON).execute(model);

        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(Optional.empty(), model.getViewedPerson());
        assertFalse(model.hasPerson(personToDelete));
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_anotherSupporter_preservesViewedPersonOutsideFilter() throws Exception {
        Person viewedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        new ViewCommand(INDEX_FIRST_PERSON).execute(model);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        new DeleteCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(Optional.of(viewedPerson), model.getViewedPerson());
        assertTrue(model.hasPerson(viewedPerson));
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        DeleteCommand deleteSecondCommand = new DeleteCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(INDEX_FIRST_PERSON);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        DeleteCommand deleteCommand = new DeleteCommand(targetIndex);
        String expected = DeleteCommand.class.getCanonicalName() + "{targetIndex=" + targetIndex + "}";
        assertEquals(expected, deleteCommand.toString());
    }

    /**
     * Deletes a supporter with the given history and checks the reported count and remaining data.
     */
    private void assertDeleteReportsInteractions(Interaction... interactions) {
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personToDelete = new PersonBuilder(originalPerson).withInteractions(interactions).build();
        model.setPerson(originalPerson, personToDelete);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);
        String expectedMessage = "Deleted supporter: " + Messages.format(personToDelete)
                + "\n" + interactions.length + " logged interaction(s) were deleted with them.";

        assertCommandSuccess(new DeleteCommand(INDEX_FIRST_PERSON), model, expectedMessage, expectedModel);
        assertFalse(model.hasPerson(personToDelete));
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoPerson(Model model) {
        model.updateFilteredPersonList(p -> false);

        assertTrue(model.getFilteredPersonList().isEmpty());
    }
}
