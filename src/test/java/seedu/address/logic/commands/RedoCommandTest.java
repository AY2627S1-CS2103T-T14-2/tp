package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

public class RedoCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_noUndoneChange_throwsCommandException() {
        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_NOTHING_TO_REDO);
    }

    @Test
    public void execute_afterUndo_reappliesChange() throws Exception {
        deleteCommitAndUndo(ALICE);

        assertCommandSuccess(new RedoCommand(), model, RedoCommand.MESSAGE_SUCCESS, getModelWithoutAlice());
    }

    @Test
    public void execute_describedChange_namesRedoneChange() throws Exception {
        model.deletePerson(ALICE);
        model.commitAddressBookIfChanged("delete 1");
        new UndoCommand().execute(model);

        String expectedMessage = String.format(RedoCommand.MESSAGE_REDID_CHANGE, "delete 1");
        assertCommandSuccess(new RedoCommand(), model, expectedMessage, getModelWithoutAlice());
    }

    @Test
    public void execute_filteredList_showsAllSupporters() throws Exception {
        deleteCommitAndUndo(ALICE);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        assertCommandSuccess(new RedoCommand(), model, RedoCommand.MESSAGE_SUCCESS, getModelWithoutAlice());
    }

    @Test
    public void execute_viewedPersonNotInRedoneData_clearsViewedPerson() throws Exception {
        deleteCommitAndUndo(BENSON);
        model.setViewedPerson(BENSON);

        new RedoCommand().execute(model);

        assertEquals(Optional.empty(), model.getViewedPerson());
    }

    @Test
    public void execute_newChangeAfterUndo_throwsCommandException() throws Exception {
        deleteCommitAndUndo(ALICE);
        model.addPerson(AMY);
        model.commitAddressBookIfChanged();

        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_NOTHING_TO_REDO);
    }

    private static Model getModelWithoutAlice() {
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.deletePerson(ALICE);
        return expectedModel;
    }

    /**
     * Deletes {@code person}, saves an undo point and undoes it, leaving one change to redo.
     */
    private void deleteCommitAndUndo(Person person) throws Exception {
        model.deletePerson(person);
        model.commitAddressBookIfChanged();
        new UndoCommand().execute(model);
    }
}
