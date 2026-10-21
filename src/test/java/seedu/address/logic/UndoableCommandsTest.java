package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Stage;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonUtil;

/**
 * Checks, through {@code LogicManager}, that every command which changes supporter data saves exactly one
 * undo point automatically, and that commands which change nothing save none.
 */
public class UndoableCommandsTest {

    @TempDir
    private Path temporaryFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private StorageManager storage;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        storage = new StorageManager(new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_add_savesOneUndoPoint() throws Exception {
        assertSavesOneUndoPoint(PersonUtil.getAddCommand(AMY));
    }

    @Test
    public void execute_edit_savesOneUndoPoint() throws Exception {
        assertSavesOneUndoPoint("edit 1 p/91234567");
    }

    @Test
    public void execute_delete_savesOneUndoPoint() throws Exception {
        assertSavesOneUndoPoint("delete 1");
    }

    @Test
    public void execute_clear_savesOneUndoPoint() throws Exception {
        assertSavesOneUndoPoint("clear");
    }

    @Test
    public void execute_log_savesOneUndoPoint() throws Exception {
        assertSavesOneUndoPoint("log 1 nt/Called about the gala");
    }

    @Test
    public void execute_stageChanged_savesOneUndoPoint() throws Exception {
        Stage newStage = model.getFilteredPersonList().get(0).getStage() == Stage.GIVING
                ? Stage.LAPSED
                : Stage.GIVING;
        assertSavesOneUndoPoint("stage 1 " + newStage.toString().toLowerCase());
    }

    @Test
    public void execute_stageUnchanged_savesNoUndoPoint() throws Exception {
        Stage currentStage = model.getFilteredPersonList().get(0).getStage();

        logic.execute("stage 1 " + currentStage.toString().toLowerCase());

        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_commandsNotChangingData_saveNoUndoPoint() throws Exception {
        logic.execute("list");
        logic.execute("find Alice");
        logic.execute("view 1");
        logic.execute("help");

        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_failedCommand_savesNoUndoPoint() {
        int outOfBoundIndex = model.getFilteredPersonList().size() + 1;

        assertThrows(CommandException.class, () -> logic.execute("delete " + outOfBoundIndex));

        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_undo_savesRestoredDataToFile() throws Exception {
        logic.execute("delete 1");

        logic.execute("undo");

        AddressBook savedData = new AddressBook(storage.readAddressBook().orElseThrow());
        assertEquals(getTypicalAddressBook(), savedData);
    }

    @Test
    public void execute_undoThenRedo_reappliesChange() throws Exception {
        logic.execute("delete 1");
        AddressBook afterDelete = new AddressBook(model.getAddressBook());
        logic.execute("undo");

        logic.execute("redo");

        assertEquals(afterDelete, model.getAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    /**
     * Executes {@code commandText}, then checks that one {@code undo} restores the original data and that no
     * further undo is possible.
     */
    private void assertSavesOneUndoPoint(String commandText) throws Exception {
        logic.execute(commandText);
        assertTrue(model.canUndoAddressBook());

        logic.execute("undo");

        assertEquals(getTypicalAddressBook(), model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
    }
}
