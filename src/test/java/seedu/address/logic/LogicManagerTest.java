package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.STAGE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.StageCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Stage;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException dummyIoException = new IOException("dummy IO exception");
    private static final IOException dummyAdException = new AccessDeniedException("dummy access denied exception");

    @TempDir
    private Path temporaryFolder;

    private Model model = new ModelManager();
    private JsonAddressBookStorage addressBookStorage;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(dummyIoException, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, dummyIoException.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(dummyAdException, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, dummyAdException.getMessage()));
    }

    @Test
    public void execute_renameIntroducer_savedIntroductionFollowsNewName() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        logic.execute("intro 2 by/1");
        logic.execute("edit 1 n/Alice Tan");

        ReadOnlyAddressBook savedAddressBook = addressBookStorage.readAddressBook().get();
        assertEquals(model.getAddressBook(), savedAddressBook);
        Person savedBenson = savedAddressBook.getPersonList().get(1);
        assertEquals(Optional.of(new Name("Alice Tan")), savedBenson.getIntroducer());
    }

    @Test
    public void execute_deleteIntroducer_savedIntroductionRemoved() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        logic.execute("intro 2 by/1");
        logic.execute("delete 1");

        ReadOnlyAddressBook savedAddressBook = addressBookStorage.readAddressBook().get();
        assertEquals(model.getAddressBook(), savedAddressBook);
        assertEquals(1, savedAddressBook.getPersonList().size());
        assertEquals(Optional.empty(), savedAddressBook.getPersonList().get(0).getIntroducer());
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    @Test
    public void getViewedPerson_selectionChanges_returnsCurrentModelSelection() {
        assertEquals(Optional.empty(), logic.getViewedPerson());

        model.addPerson(AMY);
        model.setViewedPerson(AMY);
        assertEquals(Optional.of(AMY), logic.getViewedPerson());

        model.updateFilteredPersonList(person -> false);
        assertEquals(Optional.of(AMY), logic.getViewedPerson());
        assertEquals(0, logic.getFilteredPersonList().size());

        Person updatedPerson = new PersonBuilder(AMY).withName("Amy Updated").build();
        model.setPerson(AMY, updatedPerson);
        assertEquals(Optional.of(updatedPerson), logic.getViewedPerson());

        model.deletePerson(updatedPerson);
        assertEquals(Optional.empty(), logic.getViewedPerson());
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     *
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     *
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     *
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     *
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component.
     * @param expectedSaveError the save error expected inside the exception thrown by the Logic component.
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedSaveError) {
        useStorageFailingWhile(e, () -> true);

        // Triggers the saveAddressBook method by executing an add command, which is kept despite the error
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY + STAGE_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        String addFeedback = String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(expectedPerson));
        String expectedMessage = addFeedback + "\n" + expectedSaveError + "\n"
                + String.format(LogicManager.MESSAGE_CHANGE_NOT_SAVED_FORMAT, UndoCommand.COMMAND_WORD);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    /**
     * Injects LogicManager with a JsonAddressBookStorage that throws {@code e} when saving while
     * {@code isSaveFailing} is true, and saves to {@code addressBookStorage}'s file otherwise.
     */
    private void useStorageFailingWhile(IOException e, BooleanSupplier isSaveFailing) {
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                if (isSaveFailing.getAsBoolean()) {
                    throw e;
                }
                super.saveAddressBook(addressBook);
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        logic = new LogicManager(model, new StorageManager(addressBookStorage, userPrefsStorage));
    }

    @Test
    public void execute_storageThrowsWithoutChange_throwsOnlySaveError() {
        model.addPerson(ALICE);
        useStorageFailingWhile(dummyIoException, () -> true);

        assertCommandException(ListCommand.COMMAND_WORD,
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, dummyIoException.getMessage()));
    }

    @Test
    public void execute_deleteNotSaved_keepsDeletionAndNextCommandSavesIt() throws Exception {
        model.addPerson(ALICE);
        model.addPerson(BENSON);
        AtomicBoolean isSaveFailing = new AtomicBoolean(true);
        useStorageFailingWhile(dummyAdException, isSaveFailing::get);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        CommandResult deleteResult = new DeleteCommand(INDEX_FIRST_PERSON).execute(expectedModel);
        String saveError = String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT,
                dummyAdException.getMessage());
        String expectedMessage = deleteResult.getFeedbackToUser() + "\n" + saveError + "\n"
                + String.format(LogicManager.MESSAGE_CHANGE_NOT_SAVED_FORMAT, UndoCommand.COMMAND_WORD);
        // the error shows that Alice was deleted, so the user does not repeat delete 1 and delete Benson
        assertCommandFailure("delete 1", CommandException.class, expectedMessage, expectedModel);

        isSaveFailing.set(false);
        logic.execute(ListCommand.COMMAND_WORD);
        assertEquals(List.of(BENSON), addressBookStorage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_undoNotSaved_suggestsRedoToReverseIt() throws Exception {
        model.addPerson(ALICE);
        model.commitAddressBookIfChanged();
        AtomicBoolean isSaveFailing = new AtomicBoolean(false);
        useStorageFailingWhile(dummyIoException, isSaveFailing::get);
        logic.execute("delete 1");

        isSaveFailing.set(true);
        String expectedMessage = UndoCommand.MESSAGE_SUCCESS + "\n"
                + String.format(LogicManager.FILE_OPS_ERROR_FORMAT, dummyIoException.getMessage()) + "\n"
                + String.format(LogicManager.MESSAGE_CHANGE_NOT_SAVED_FORMAT, RedoCommand.COMMAND_WORD);
        // another undo would reverse an earlier change instead of this one
        assertThrows(CommandException.class, expectedMessage, () -> logic.execute(UndoCommand.COMMAND_WORD));
        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_stageWithStagePrefix_changesStageWithoutNote() throws Exception {
        model.addPerson(ALICE);
        CommandResult result = logic.execute("stage 1 st/declined");

        assertEquals(String.format(StageCommand.MESSAGE_STAGE_CHANGED, ALICE.getName(), ALICE.getStage(),
                Stage.DECLINED), result.getFeedbackToUser());
    }

    @Test
    public void execute_changeWithUnrecognisedPrefix_addsNote() throws Exception {
        model.addPerson(ALICE);
        CommandResult result = logic.execute("log 1 nt/Called about the gala dte/2024-01-01");

        assertEquals(String.format(Messages.MESSAGE_UNRECOGNISED_PREFIX, "dte/"),
                result.getFeedbackToUser().lines().toList().getLast());
    }

    @Test
    public void execute_noChangeWithUnrecognisedPrefix_noNote() throws Exception {
        model.addPerson(ALICE);
        CommandResult result = logic.execute("find x/Alice");

        assertEquals(String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, 0), result.getFeedbackToUser());
    }

    @Test
    public void execute_changeWithoutUnrecognisedPrefix_noNote() throws Exception {
        model.addPerson(ALICE);
        CommandResult result = logic.execute("log 1 nt/Called about the gala, and/or the dinner");

        assertEquals(1, result.getFeedbackToUser().lines().count());
    }

    @Test
    public void execute_rejectedWithUnrecognisedPrefix_addsNoteToError() {
        assertParseException("add n/Tan p/91234567 x/foo", Phone.MESSAGE_CONSTRAINTS + "\n"
                + String.format(Messages.MESSAGE_UNRECOGNISED_PREFIX_IN_ERROR, "x/"));
        assertParseException("add n/Tan x/foo p/91234567 ee/tan@example.com", Name.MESSAGE_CONSTRAINTS + "\n"
                + String.format(Messages.MESSAGE_UNRECOGNISED_PREFIXES_IN_ERROR, "x/, ee/"));
    }

    @Test
    public void execute_rejectedWithoutUnrecognisedPrefix_errorUnchanged() {
        assertParseException("add n/Tan s/o Lim p/abc", Phone.MESSAGE_CONSTRAINTS);
    }
}
