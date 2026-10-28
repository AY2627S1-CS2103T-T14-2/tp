package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class UndoCommandTest {

    private final Model model = getTypicalModel();

    @Test
    public void execute_noChange_throwsCommandException() {
        assertCommandFailure(new UndoCommand(), model, UndoCommand.MESSAGE_NOTHING_TO_UNDO);
    }

    @Test
    public void execute_afterChange_restoresPreviousData() {
        deleteAliceAndCommit();

        assertCommandSuccess(new UndoCommand(), model, UndoCommand.MESSAGE_SUCCESS, getTypicalModel());
    }

    @Test
    public void execute_describedChange_namesUndoneChange() {
        model.deletePerson(ALICE);
        model.commitAddressBookIfChanged("delete 1");

        String expectedMessage = String.format(UndoCommand.MESSAGE_UNDID_CHANGE, "delete 1");
        assertCommandSuccess(new UndoCommand(), model, expectedMessage, getTypicalModel());
    }

    @Test
    public void execute_filteredList_showsAllSupporters() {
        deleteAliceAndCommit();
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        assertCommandSuccess(new UndoCommand(), model, UndoCommand.MESSAGE_SUCCESS, getTypicalModel());
    }

    @Test
    public void execute_viewedPersonStillExists_showsRestoredRecord() throws Exception {
        Person editedAlice = new PersonBuilder(ALICE).withPhone("91234567").build();
        model.setPerson(ALICE, editedAlice);
        model.setViewedPerson(editedAlice);
        model.commitAddressBookIfChanged();

        new UndoCommand().execute(model);

        assertEquals(Optional.of(ALICE), model.getViewedPerson());
    }

    @Test
    public void execute_viewedPersonNoLongerExists_clearsViewedPerson() throws Exception {
        model.addPerson(AMY);
        model.setViewedPerson(AMY);
        model.commitAddressBookIfChanged();

        new UndoCommand().execute(model);

        assertEquals(Optional.empty(), model.getViewedPerson());
    }

    @Test
    public void execute_allChangesUndone_throwsCommandException() throws Exception {
        deleteAliceAndCommit();
        new UndoCommand().execute(model);

        assertCommandFailure(new UndoCommand(), model, UndoCommand.MESSAGE_NOTHING_TO_UNDO);
    }

    private static Model getTypicalModel() {
        return new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    private void deleteAliceAndCommit() {
        model.deletePerson(ALICE);
        model.commitAddressBookIfChanged();
    }
}
