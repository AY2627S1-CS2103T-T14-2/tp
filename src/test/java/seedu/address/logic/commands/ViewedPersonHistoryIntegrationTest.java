package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests viewed-supporter restoration through parsing, history, filtering and persistence.
 */
public class ViewedPersonHistoryIntegrationTest {
    @TempDir
    private Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("supporterbook.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_filteredRenameUndoRedo_keepsRestoredSupporterSelected() throws Exception {
        logic.execute("add n/Other Supporter p/456");
        logic.execute("add n/UndoRenameProbe p/123");
        logic.execute("find UndoRenameProbe");
        logic.execute("view 1");
        Person original = model.getViewedPerson().orElseThrow();
        logic.execute("edit 1 n/UndoRenameProbeUpdated");
        Person renamed = model.getViewedPerson().orElseThrow();

        logic.execute("undo");
        assertViewedAndSavedPerson(original);
        logic.execute("redo");
        assertViewedAndSavedPerson(renamed);
        logic.execute("undo");
        logic.execute("edit 2 p/789");
        Person newVersion = model.getViewedPerson().orElseThrow();
        assertFalse(model.canRedoAddressBook());
        logic.execute("undo");
        assertViewedAndSavedPerson(original);
        logic.execute("redo");
        assertViewedAndSavedPerson(newVersion);
    }

    @Test
    public void execute_renamedIntroducerUndoRedo_preservesViewedDependent() throws Exception {
        logic.execute("add n/Introducer p/123");
        logic.execute("add n/Dependent p/456");
        logic.execute("intro 2 by/1");
        logic.execute("view 2");
        Person original = model.getViewedPerson().orElseThrow();
        logic.execute("edit 1 n/Renamed Introducer");
        Person updated = model.getViewedPerson().orElseThrow();

        logic.execute("undo");
        assertViewedAndSavedPerson(original);
        logic.execute("redo");
        assertViewedAndSavedPerson(updated);
    }

    @Test
    public void execute_undoRemovesViewedSupporter_clearsPanel() throws Exception {
        logic.execute("add n/UndoRenameProbe p/123");
        logic.execute("view 1");
        logic.execute("edit 1 n/UndoRenameProbeUpdated");

        logic.execute("undo");
        assertEquals("UndoRenameProbe", model.getViewedPerson().orElseThrow().getName().fullName);
        logic.execute("undo");
        assertFalse(model.getViewedPerson().isPresent());
        logic.execute("redo");
        assertFalse(model.getViewedPerson().isPresent());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private void assertViewedAndSavedPerson(Person expected) throws Exception {
        assertEquals(expected, model.getViewedPerson().orElseThrow());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }
}
