package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests deletion through the parser, model and storage, including filter and viewed-supporter behavior.
 */
public class DeleteCommandIntegrationTest {

    private static final Interaction DELETED_INTERACTION =
            new Interaction(LocalDate.of(2026, 10, 12), "Discussed the year-end appeal");
    private static final Interaction OTHER_INTERACTION =
            new Interaction(LocalDate.of(2026, 10, 13), "Mentioned Benson Meier during a call");

    @TempDir
    public Path testFolder;

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private Logic logic;
    private Storage storage;
    private Path dataFile;
    private Person personToDelete;
    private Person otherPerson;

    @BeforeEach
    public void setUp() throws IOException {
        dataFile = testFolder.resolve("supporterbook.json");
        storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        logic = new LogicManager(model, storage);
        personToDelete = new PersonBuilder(BENSON)
                .withInteractions(DELETED_INTERACTION, DELETED_INTERACTION).build();
        otherPerson = new PersonBuilder(DANIEL).withInteractions(OTHER_INTERACTION).build();
        model.setPerson(BENSON, personToDelete);
        model.setPerson(DANIEL, otherPerson);
        storage.saveAddressBook(model.getAddressBook());
    }

    @Test
    public void execute_filteredDeletion_preservesOtherHistoryAndSavesDeletion() throws Exception {
        logic.execute("find meier");
        assertEquals(List.of(personToDelete, otherPerson), model.getFilteredPersonList());
        logic.execute("view 1");
        AddressBook expectedData = new AddressBook(model.getAddressBook());
        expectedData.removePerson(personToDelete);

        CommandResult result = logic.execute("delete 1");

        assertEquals("Deleted supporter: " + Messages.format(personToDelete)
                + "\n2 logged interaction(s) were deleted with them.", result.getFeedbackToUser());
        assertEquals(expectedData, model.getAddressBook());
        assertEquals(List.of(otherPerson), model.getFilteredPersonList());
        assertEquals(Optional.empty(), model.getViewedPerson());
        ReadOnlyAddressBook savedData = storage.readAddressBook().orElseThrow();
        assertEquals(expectedData, savedData);
        Model reloadedModel = new ModelManager(savedData, new UserPrefs());
        assertEquals(expectedData.getPersonList(), reloadedModel.getFilteredPersonList());
    }

    @Test
    public void execute_invalidDisplayedIndex_preservesDataFilterSelectionAndFile() throws Exception {
        logic.execute("view 1");
        logic.execute("find meier");

        // Index 3 exists in the full list, but only two supporters are displayed.
        assertFailurePreservesState(CommandException.class, "delete 3",
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidFormat_preservesDataFilterSelectionAndFile() throws Exception {
        logic.execute("find meier");
        logic.execute("view 1");

        assertFailurePreservesState(ParseException.class, "delete 1 2",
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
    }

    private void assertFailurePreservesState(Class<? extends Throwable> exceptionType,
            String command, String expectedMessage) throws IOException {
        AddressBook expectedData = new AddressBook(model.getAddressBook());
        List<Person> expectedDisplayedPersons = List.copyOf(model.getFilteredPersonList());
        Optional<Person> expectedViewedPerson = model.getViewedPerson();
        byte[] originalContents = Files.readAllBytes(dataFile);

        assertThrows(exceptionType, expectedMessage, () -> logic.execute(command));

        assertEquals(expectedData, model.getAddressBook());
        assertEquals(expectedDisplayedPersons, model.getFilteredPersonList());
        assertEquals(expectedViewedPerson, model.getViewedPerson());
        assertArrayEquals(originalContents, Files.readAllBytes(dataFile));
    }
}
