package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests long names through parsing, editing, undo history and a saved-data reload.
 */
public class LongNameCommandIntegrationTest {
    @TempDir
    private Path temporaryFolder;

    @Test
    public void execute_longName_roundTripsThroughCommandsAndStorage() throws Exception {
        Model model = new ModelManager();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("supporterbook.json"));
        Logic logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        String originalName = "A".repeat(5_000);
        String editedName = originalName + " D/O O'Brien-Tan";

        logic.execute("add n/" + originalName + " p/123");
        assertEquals(originalName, storage.readAddressBook().orElseThrow().getPersonList().get(0).getName().fullName);
        logic.execute("edit 1 n/" + editedName);
        assertEquals(editedName, model.getAddressBook().getPersonList().get(0).getName().fullName);
        logic.execute("undo");
        assertEquals(originalName, model.getAddressBook().getPersonList().get(0).getName().fullName);
        logic.execute("redo");
        assertThrows(ParseException.class, () -> logic.execute("edit 1 n/" + editedName + "/invalid"));

        Model reloadedModel = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reloadedModel.getAddressBook());
        assertEquals(editedName, reloadedModel.getAddressBook().getPersonList().get(0).getName().fullName);
    }
}
