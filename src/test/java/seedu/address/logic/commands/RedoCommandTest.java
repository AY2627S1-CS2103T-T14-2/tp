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

public class RedoCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_noUndoneChange_throwsCommandException() {
        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_NOTHING_TO_REDO);
    }

    @Test
    public void execute_afterUndo_reappliesChange() throws Exception {
        model.deletePerson(ALICE);
        model.commitAddressBookIfChanged();
        new UndoCommand().execute(model);
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.deletePerson(ALICE);

        assertCommandSuccess(new RedoCommand(), model, RedoCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_filteredList_showsAllSupporters() throws Exception {
        model.deletePerson(ALICE);
        model.commitAddressBookIfChanged();
        new UndoCommand().execute(model);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.deletePerson(ALICE);

        assertCommandSuccess(new RedoCommand(), model, RedoCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_viewedPersonNotInRedoneData_clearsViewedPerson() throws Exception {
        model.deletePerson(BENSON);
        model.commitAddressBookIfChanged();
        new UndoCommand().execute(model);
        model.setViewedPerson(BENSON);

        new RedoCommand().execute(model);

        assertEquals(Optional.empty(), model.getViewedPerson());
    }

    @Test
    public void execute_newChangeAfterUndo_throwsCommandException() throws Exception {
        model.deletePerson(ALICE);
        model.commitAddressBookIfChanged();
        new UndoCommand().execute(model);
        model.addPerson(AMY);
        model.commitAddressBookIfChanged();

        assertCommandFailure(new RedoCommand(), model, RedoCommand.MESSAGE_NOTHING_TO_REDO);
    }
}
