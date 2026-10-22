package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
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
    public void execute_editLogUnchangedAfterUndo_preservesRedo() throws Exception {
        logic.execute("log 1 nt/Original dt/2024-03-10");
        logic.execute("editlog 1 1 nt/Changed");
        AddressBook afterEdit = new AddressBook(model.getAddressBook());
        logic.execute("undo");

        logic.execute("editlog 1 1 nt/Original");

        assertTrue(model.canRedoAddressBook());
        logic.execute("redo");
        assertEquals(afterEdit, model.getAddressBook());
    }

    @Test
    public void execute_editLogUnchanged_selectsSupporterWithoutSavingUndoPoint() throws Exception {
        logic.execute("log 1 nt/Original dt/2024-03-10");
        logic.execute("view 2");

        logic.execute("editlog 1 1 nt/Original dt/2024-03-10");

        assertEquals(model.getFilteredPersonList().get(0), model.getViewedPerson().orElseThrow());
        // undo the original log directly, without an extra undo for the unchanged edit
        logic.execute("undo");
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
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
    public void execute_intro_savesOneUndoPoint() throws Exception {
        assertSavesOneUndoPoint("intro 2 by/1");
    }

    @Test
    public void execute_tag_savesOneUndoPoint() throws Exception {
        assertSavesOneUndoPoint("tag 1 t/vip");
    }

    @Test
    public void execute_untag_savesOneUndoPoint() throws Exception {
        assertSavesOneUndoPoint("untag 1 t/friends");
    }

    @Test
    public void execute_introAndTagsUnchanged_saveNoUndoPoint() throws Exception {
        logic.execute("intro 1 by/");
        logic.execute("tag 1 t/friends");
        logic.execute("untag 1 t/vip");

        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_undoDeleteOfIntroducer_restoresSupporterAndIntroductions() throws Exception {
        logic.execute("intro 2 by/1");
        logic.execute("intro 3 by/1");
        AddressBook beforeDelete = new AddressBook(model.getAddressBook());
        logic.execute("delete 1");
        assertTrue(model.getPersonsIntroducedBy(ALICE.getName()).isEmpty());

        logic.execute("undo");

        assertEquals(beforeDelete, model.getAddressBook());
        assertEquals(2, model.getPersonsIntroducedBy(ALICE.getName()).size());
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
