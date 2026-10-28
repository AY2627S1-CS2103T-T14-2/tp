package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.SharedContactWarning;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests shared-contact edit warnings through parsing, filtering, selection, persistence and undo/redo.
 */
public class EditSharedContactWarningIntegrationTest {
    @TempDir
    private Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("supporterbook.json"));
        logic = createLogic(model);
    }

    @Test
    public void execute_reportedPhoneAndEmailCases_warnsWithoutChangingMatchingSupporter() throws Exception {
        Person alex = model.getAddressBook().getPersonList().get(0);
        int originalCount = model.getAddressBook().getPersonList().size();

        CommandResult phoneResult = logic.execute("edit 2 p/87438807");
        CommandResult emailResult = logic.execute("edit 3 e/ALEXYEOH@example.com");

        assertTrue(phoneResult.getFeedbackToUser().startsWith("Edited supporter: Bernice Yu"));
        assertTrue(phoneResult.getFeedbackToUser().endsWith(editWarning("Alex Yeoh (phone)")));
        assertTrue(emailResult.getFeedbackToUser().endsWith(editWarning("Alex Yeoh (email)")));
        assertEquals(alex, model.getAddressBook().getPersonList().get(0));
        assertEquals(originalCount, model.getAddressBook().getPersonList().size());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_matchHiddenByFilter_preservesSelectionAndOneUndoableChange() throws Exception {
        AddressBook original = new AddressBook(model.getAddressBook());
        logic.execute("find Bernice");
        logic.execute("view 1");
        Person before = model.getViewedPerson().orElseThrow();

        CommandResult result = logic.execute("edit 1 p/8743-8807");
        AddressBook updated = new AddressBook(model.getAddressBook());
        Person after = model.getViewedPerson().orElseThrow();
        assertTrue(result.getFeedbackToUser().endsWith(editWarning("Alex Yeoh (phone)")));
        assertEquals("8743-8807", after.getPhone().orElseThrow().value);
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());

        logic.execute("undo");
        assertEquals(original, model.getAddressBook());
        assertEquals(before, model.getViewedPerson().orElseThrow());
        assertFalse(model.canUndoAddressBook());
        logic.execute("redo");
        assertEquals(updated, model.getAddressBook());
        assertEquals(after, model.getViewedPerson().orElseThrow());
        assertEquals(updated, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_multipleMatches_retainsFullListOrderWithoutSelfMatch() throws Exception {
        logic.execute("add n/Additional Match p/87438807");

        CommandResult result = logic.execute("edit 2 p/87438807 e/ALEXYEOH@EXAMPLE.COM");

        assertTrue(result.getFeedbackToUser().endsWith(
                editWarning("Alex Yeoh (phone and email); Additional Match (phone)")));
    }

    @Test
    public void execute_noOpOnSharedContact_warnsWithoutCreatingAnotherUndoPoint() throws Exception {
        AddressBook original = new AddressBook(model.getAddressBook());
        logic.execute("edit 2 p/87438807");

        CommandResult result = logic.execute("edit 2 p/87438807");

        assertTrue(result.getFeedbackToUser().endsWith(editWarning("Alex Yeoh (phone)")));
        logic.execute("undo");
        assertEquals(original, model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_invalidEdit_preservesDataSelectionSavedFileAndRedo() throws Exception {
        logic.execute("view 2");
        logic.execute("edit 2 p/87438807");
        logic.execute("undo");
        AddressBook original = new AddressBook(model.getAddressBook());
        var selection = model.getViewedPerson();
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());

        assertThrows(CommandException.class, () -> logic.execute("edit 2 n/Alex Yeoh p/87438807"));
        assertThrows(ParseException.class, () -> logic.execute("edit 2 p/invalid"));
        assertThrows(CommandException.class, () -> logic.execute("edit 2 p/ e/"));

        assertEquals(original, model.getAddressBook());
        assertEquals(selection, model.getViewedPerson());
        assertTrue(model.canRedoAddressBook());
        assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_warningAndMistypedPrefix_keepsBothMessages() throws Exception {
        CommandResult result = logic.execute("edit 2 p/87438807 a/Office x/foo");

        assertTrue(result.getFeedbackToUser().contains(editWarning("Alex Yeoh (phone)")));
        assertTrue(result.getFeedbackToUser().contains("Note: x/"));
        assertEquals("Office x/foo", model.getAddressBook().getPersonList().get(1).getAddress().orElseThrow().value);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_phoneAnnotations_matchesCompleteNumberButNotDifferentExtension() throws Exception {
        model = new ModelManager();
        logic = createLogic(model);
        logic.execute("add n/Source p/+65 9123-4567 ext 12 (HP)");
        logic.execute("add n/Target p/123");

        CommandResult matching = logic.execute("edit 2 p/6591234567 EXT 12 (Office)");
        assertTrue(matching.getFeedbackToUser().endsWith(editWarning("Source (phone)")));
        CommandResult differentExtension = logic.execute("edit 2 p/6591234567 ext 012");
        assertFalse(differentExtension.getFeedbackToUser().contains("Warning:"));
        CommandResult partialNumber = logic.execute("edit 2 p/9123");
        assertFalse(partialNumber.getFeedbackToUser().contains("Warning:"));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private Logic createLogic(Model targetModel) {
        return new LogicManager(targetModel, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private String editWarning(String matches) {
        return String.format(SharedContactWarning.MESSAGE_SHARED_CONTACTS_AFTER_EDIT, matches);
    }
}
