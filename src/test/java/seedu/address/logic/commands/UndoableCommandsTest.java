package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Stage;
import seedu.address.testutil.EditPersonDescriptorBuilder;

/**
 * Checks that every command which changes supporter data saves exactly one undoable state, and that
 * commands which change nothing save none.
 */
public class UndoableCommandsTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_add_commitsOnce() throws Exception {
        assertCommitsOnce(new AddCommand(AMY));
    }

    @Test
    public void execute_edit_commitsOnce() throws Exception {
        assertCommitsOnce(new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone("91234567").build()));
    }

    @Test
    public void execute_delete_commitsOnce() throws Exception {
        assertCommitsOnce(new DeleteCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void execute_clear_commitsOnce() throws Exception {
        assertCommitsOnce(new ClearCommand());
    }

    @Test
    public void execute_log_commitsOnce() throws Exception {
        assertCommitsOnce(new LogCommand(INDEX_FIRST_PERSON, "Called about the gala"));
    }

    @Test
    public void execute_stageChanged_commitsOnce() throws Exception {
        Stage newStage = model.getFilteredPersonList().get(0).getStage() == Stage.GIVING
                ? Stage.LAPSED
                : Stage.GIVING;
        assertCommitsOnce(new StageCommand(INDEX_FIRST_PERSON, newStage));
    }

    @Test
    public void execute_stageUnchanged_doesNotCommit() throws Exception {
        Stage currentStage = model.getFilteredPersonList().get(0).getStage();

        new StageCommand(INDEX_FIRST_PERSON, currentStage).execute(model);

        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_commandsNotChangingData_doNotCommit() throws Exception {
        new ListCommand().execute(model);
        new ViewCommand(INDEX_FIRST_PERSON).execute(model);
        new HelpCommand().execute(model);

        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_failedCommand_doesNotCommit() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertThrows(CommandException.class, () -> new DeleteCommand(outOfBoundIndex).execute(model));

        assertFalse(model.canUndoAddressBook());
    }

    /**
     * Executes {@code command}, then checks that one undo restores the original data and that no
     * further undo is possible.
     */
    private void assertCommitsOnce(Command command) throws Exception {
        command.execute(model);
        assertTrue(model.canUndoAddressBook());

        model.undoAddressBook();

        assertEquals(getTypicalAddressBook(), model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
    }
}
