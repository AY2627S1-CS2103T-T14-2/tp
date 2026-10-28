package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Phone;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests advisory notes for prefixes that belong to another command without changing parsing or saved values.
 */
public class CommandPrefixWarningIntegrationTest {
    @TempDir
    private Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("supporterbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_addWithIntroducerPrefix_namesPrefixInPhoneErrorWithoutSaving() throws Exception {
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());

        ParseException failure = assertThrows(ParseException.class, () ->
                logic.execute("add n/Lim p/91234567 by/1"));

        assertEquals(Phone.MESSAGE_CONSTRAINTS + "\n"
                + Messages.formatUnrecognisedPrefixErrorNote(List.of("by/")), failure.getMessage());
        assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_editWithStagePrefix_namesPrefixInUsageError() {
        ParseException failure = assertThrows(ParseException.class, () -> logic.execute("edit 1 st/giving"));

        assertTrue(failure.getMessage().endsWith(Messages.formatUnrecognisedPrefixErrorNote(List.of("st/"))));
    }

    @Test
    public void execute_logWithTagPrefix_warnsAndPreservesLiteralNote() throws Exception {
        CommandResult result = logic.execute("log 1 nt/Called t/vip");

        assertTrue(result.getFeedbackToUser().endsWith(Messages.formatUnrecognisedPrefixNote(List.of("t/"))));
        var interactions = model.getAddressBook().getPersonList().get(0).getInteractions();
        assertEquals("Called t/vip", interactions.getLast().getNote());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_parseFailure_preservesSelectionFilterDataAndRedo() throws Exception {
        logic.execute("log 1 nt/Called");
        logic.execute("undo");
        logic.execute("find Bernice");
        logic.execute("view 1");
        var selection = model.getViewedPerson();
        var shown = List.copyOf(model.getFilteredPersonList());
        AddressBook original = new AddressBook(model.getAddressBook());
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());

        assertThrows(ParseException.class, () -> logic.execute("edit 1 st/giving"));

        assertEquals(original, model.getAddressBook());
        assertEquals(selection, model.getViewedPerson());
        assertEquals(shown, model.getFilteredPersonList());
        assertTrue(model.canRedoAddressBook());
        assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_unsupportedPrefixesInSavedText_areOneUndoableChange() throws Exception {
        AddressBook original = new AddressBook(model.getAddressBook());
        CommandResult result = logic.execute("log 1 nt/Called t/vip by/2 t/again x/foo");
        AddressBook updated = new AddressBook(model.getAddressBook());
        assertTrue(result.getFeedbackToUser().endsWith(
                Messages.formatUnrecognisedPrefixNote(List.of("t/", "by/", "x/"))));

        logic.execute("undo");
        assertEquals(original, model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
        logic.execute("redo");
        assertEquals(updated, model.getAddressBook());
        assertEquals(updated, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_executionFailure_doesNotAppendPrefixNoteOrSave() throws Exception {
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());

        CommandException failure = assertThrows(CommandException.class, () ->
                logic.execute("log 999 nt/Called t/vip"));

        assertEquals(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, failure.getMessage());
        assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_nonMutatingAndIgnoredArguments_doNotSuggestUndo() throws Exception {
        CommandResult findResult = logic.execute("find x/Alex");
        assertFalse(findResult.getFeedbackToUser().contains("Note:"));
        CommandResult clearResult = logic.execute("clear t/vip x/foo");
        assertFalse(clearResult.getFeedbackToUser().contains("Note:"));
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void execute_supportedPrefixesAndEverydayWords_doNotWarn() throws Exception {
        CommandResult result = logic.execute("log 1 nt/Called w/ him and/or his wife dt/2024-01-01");

        assertFalse(result.getFeedbackToUser().contains("Note:"));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_findUnsupportedKnownFilters_reportsOnlyExplicitErrorAndPreservesState() throws Exception {
        logic.execute("find Bernice");
        logic.execute("view 1");
        AddressBook original = new AddressBook(model.getAddressBook());
        var selection = model.getViewedPerson();
        var shown = List.copyOf(model.getFilteredPersonList());
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());

        for (String prefix : List.of("n/", "a/", "dt/", "nt/", "by/")) {
            String expectedError = "Unsupported search filter(s): " + prefix + ".\n"
                    + "find supports only o/, t/, st/, p/ and e/.\n" + FindCommand.MESSAGE_USAGE;
            for (String command : List.of("find " + prefix + "value", "find o/DBS " + prefix + "value")) {
                ParseException failure = assertThrows(ParseException.class, () -> logic.execute(command));

                assertEquals(expectedError, failure.getMessage(), command);
                assertFalse(failure.getMessage().contains("Note:"), command);
                assertEquals(original, model.getAddressBook());
                assertEquals(selection, model.getViewedPerson());
                assertEquals(shown, model.getFilteredPersonList());
                assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()));
            }
        }
    }

    @Test
    public void execute_findKnownAndUnknownPrefixes_addsNoteOnlyForUnknownWord() {
        ParseException failure = assertThrows(ParseException.class, () -> logic.execute("find n/Tan x/foo"));

        String expectedError = "Unsupported search filter(s): n/.\n"
                + "find supports only o/, t/, st/, p/ and e/.\n" + FindCommand.MESSAGE_USAGE;
        assertEquals(expectedError + "\n" + Messages.formatUnrecognisedPrefixErrorNote(List.of("x/")),
                failure.getMessage());
        assertFalse(failure.getMessage().contains("Note: n/"));
    }
}
