package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
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
 * Tests advisory contact warnings through parsing, filtering, saving and undo history.
 */
public class SharedContactWarningIntegrationTest {
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
    public void execute_reportedPhoneAndEmailCases_addsBothWithWarnings() throws Exception {
        int originalCount = model.getAddressBook().getPersonList().size();

        CommandResult phoneResult = logic.execute("add n/Dup Phone p/87438807");
        CommandResult emailResult = logic.execute("add n/Dup Email e/alexyeoh@example.com");

        assertTrue(phoneResult.getFeedbackToUser().startsWith("New supporter added: Dup Phone"));
        assertTrue(phoneResult.getFeedbackToUser().contains("Alex Yeoh (phone)"));
        assertTrue(emailResult.getFeedbackToUser().startsWith("New supporter added: Dup Email"));
        assertTrue(emailResult.getFeedbackToUser().contains("Alex Yeoh (email)"));
        assertEquals(originalCount + 2, model.getAddressBook().getPersonList().size());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_matchHiddenByFilter_warnsAndPreservesViewedSupporter() throws Exception {
        logic.execute("find Bernice");
        logic.execute("view 1");
        var selection = model.getViewedPerson();

        CommandResult result = logic.execute("add n/Hidden Match p/8743-8807");

        assertTrue(result.getFeedbackToUser().contains("Alex Yeoh (phone)"));
        assertEquals(selection, model.getViewedPerson());
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_noContactMatch_preservesNormalSuccessMessage() throws Exception {
        CommandResult result = logic.execute("add n/Unrelated Supporter p/123456789");

        assertFalse(result.getFeedbackToUser().contains("Warning:"));
        assertEquals("New supporter added: Unrelated Supporter; Stage: Prospect; Phone: 123456789",
                result.getFeedbackToUser());
    }

    @Test
    public void execute_firstSupporter_doesNotWarnAboutItself() throws Exception {
        Model emptyModel = new ModelManager();
        Logic emptyLogic = createLogic(emptyModel);

        CommandResult result = emptyLogic.execute("add n/First Supporter p/123");

        assertFalse(result.getFeedbackToUser().contains("Warning:"));
        assertEquals(1, emptyModel.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_warningAddition_isOneUndoableChange() throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        logic.execute("add n/Dup Phone p/87438807");
        AddressBook after = new AddressBook(model.getAddressBook());

        logic.execute("undo");
        assertEquals(before, model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
        assertTrue(model.canRedoAddressBook());
        logic.execute("redo");
        assertEquals(after, model.getAddressBook());
        assertEquals(after, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_duplicateNameOrInvalidContact_rejectsWithoutChangingDataOrRedo() throws Exception {
        logic.execute("add n/Dup Phone p/87438807");
        logic.execute("undo");
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());

        assertThrows(CommandException.class, () -> logic.execute("add n/Alex Yeoh p/87438807"));
        assertThrows(ParseException.class, () -> logic.execute("add n/Bad Email e/not-an-email"));

        assertEquals(before, model.getAddressBook());
        assertTrue(model.canRedoAddressBook());
        assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_warningAddition_doesNotChangeExistingSupporter() throws Exception {
        Person original = model.getAddressBook().getPersonList().get(0);

        logic.execute("add n/Shared Contact p/87438807 e/ALEXYEOH@EXAMPLE.COM");

        assertEquals(original, model.getAddressBook().getPersonList().get(0));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private Logic createLogic(Model targetModel) {
        return new LogicManager(targetModel, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
