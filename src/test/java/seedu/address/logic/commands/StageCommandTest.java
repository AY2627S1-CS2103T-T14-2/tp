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

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Stage;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code StageCommand}.
 */
public class StageCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StageCommand(null, Stage.GIVING));
        assertThrows(NullPointerException.class, () -> new StageCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_newStageUnfilteredList_success() {
        Person personToUpdate = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person updatedPerson = new PersonBuilder(personToUpdate).withStage("cultivating").build();
        StageCommand stageCommand = new StageCommand(INDEX_FIRST_PERSON, Stage.CULTIVATING);

        String expectedMessage = String.format(StageCommand.MESSAGE_STAGE_CHANGED,
                personToUpdate.getName(), Stage.PROSPECT, Stage.CULTIVATING);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToUpdate, updatedPerson);

        assertCommandSuccess(stageCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_anyStageToAnyStage_success() {
        // relationships do not move in a straight line: a giving supporter can go back to prospect
        Person personToUpdate = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person updatedPerson = new PersonBuilder(personToUpdate).withStage("prospect").build();
        StageCommand stageCommand = new StageCommand(INDEX_SECOND_PERSON, Stage.PROSPECT);

        String expectedMessage = String.format(StageCommand.MESSAGE_STAGE_CHANGED,
                personToUpdate.getName(), Stage.GIVING, Stage.PROSPECT);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToUpdate, updatedPerson);

        assertCommandSuccess(stageCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_sameStage_successWithoutChange() {
        Person person = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        StageCommand stageCommand = new StageCommand(INDEX_SECOND_PERSON, Stage.GIVING);

        String expectedMessage = String.format(StageCommand.MESSAGE_ALREADY_AT_STAGE, person.getName(), Stage.GIVING);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(stageCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_messageFormat_matchesSpecification() throws Exception {
        String result = new StageCommand(INDEX_SECOND_PERSON, Stage.LAPSED).execute(model).getFeedbackToUser();
        assertEquals("Changed stage of Benson Meier: Giving -> Lapsed.", result);

        result = new StageCommand(INDEX_SECOND_PERSON, Stage.LAPSED).execute(model).getFeedbackToUser();
        assertEquals("Benson Meier is already at stage Lapsed. Nothing was changed.", result);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        StageCommand stageCommand = new StageCommand(outOfBoundIndex, Stage.GIVING);

        assertCommandFailure(stageCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_successAndFilterKept() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToUpdate = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person updatedPerson = new PersonBuilder(personToUpdate).withStage("giving").build();
        StageCommand stageCommand = new StageCommand(INDEX_FIRST_PERSON, Stage.GIVING);

        String expectedMessage = String.format(StageCommand.MESSAGE_STAGE_CHANGED,
                personToUpdate.getName(), Stage.PROSPECT, Stage.GIVING);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);
        expectedModel.setPerson(personToUpdate, updatedPerson);

        assertCommandSuccess(stageCommand, model, expectedMessage, expectedModel);
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // supporter exists in the full list but is not displayed
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        StageCommand stageCommand = new StageCommand(outOfBoundIndex, Stage.GIVING);

        assertCommandFailure(stageCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        StageCommand stageFirstGiving = new StageCommand(INDEX_FIRST_PERSON, Stage.GIVING);
        StageCommand stageFirstLapsed = new StageCommand(INDEX_FIRST_PERSON, Stage.LAPSED);
        StageCommand stageSecondGiving = new StageCommand(INDEX_SECOND_PERSON, Stage.GIVING);

        // same object -> returns true
        assertTrue(stageFirstGiving.equals(stageFirstGiving));

        // same values -> returns true
        assertTrue(stageFirstGiving.equals(new StageCommand(INDEX_FIRST_PERSON, Stage.GIVING)));

        // different types -> returns false
        assertFalse(stageFirstGiving.equals(1));

        // null -> returns false
        assertFalse(stageFirstGiving.equals(null));

        // different stage -> returns false
        assertFalse(stageFirstGiving.equals(stageFirstLapsed));

        // different index -> returns false
        assertFalse(stageFirstGiving.equals(stageSecondGiving));
    }

    @Test
    public void toStringMethod() {
        StageCommand stageCommand = new StageCommand(INDEX_FIRST_PERSON, Stage.GIVING);
        String expected = StageCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", stage=" + Stage.GIVING + "}";
        assertEquals(expected, stageCommand.toString());
    }
}
