package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests annotated phones through command parsing, filtering, persistence and undo history.
 */
public class PhoneCommandIntegrationTest {
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
    public void execute_annotatedPhones_addsEditsSearchesAndReloads() throws Exception {
        logic.execute("add n/Label Test p/9123 4567 (HP)");
        logic.execute("add n/Country Test p/(65) 9123 4567");
        logic.execute("add n/Extension Test p/9123 4567 ext 12");

        logic.execute("find p/9123");
        assertEquals(3, model.getFilteredPersonList().size());
        logic.execute("find p/(65)");
        assertEquals("Country Test", model.getFilteredPersonList().get(0).getName().fullName);

        String editedPhone = "+65 8123-4567 ext 1 (Office)";
        logic.execute("edit 1 p/" + editedPhone);
        logic.execute("find p/8123-4567 EXT 1");
        Person edited = model.getFilteredPersonList().get(0);
        assertEquals(editedPhone, edited.getPhone().orElseThrow().value);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());

        logic.execute("undo");
        assertEquals("(65) 9123 4567", model.getFilteredPersonList().get(1).getPhone().orElseThrow().value);
        logic.execute("redo");
        assertEquals(editedPhone, model.getFilteredPersonList().get(1).getPhone().orElseThrow().value);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_veryLongPhones_handlesInputWithoutChangingSavedSupporter() throws Exception {
        logic.execute("add n/Phone Test p/91234567");
        logic.execute("view 1");
        AddressBook before = new AddressBook(model.getAddressBook());
        var selection = model.getViewedPerson();
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());
        String phone = "1-".repeat(5000) + "1";

        for (String command : List.of("add n/Long Phone p/" + phone, "edit 1 p/" + phone,
                "find p/" + phone + "-")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(before, model.getAddressBook());
            assertEquals(selection, model.getViewedPerson());
            assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()));
        }

        logic.execute("find p/" + phone);
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(before, storage.readAddressBook().orElseThrow());
        assertEquals(selection, model.getViewedPerson());
    }

    @Test
    public void execute_invalidAnnotatedPhone_preservesDataResultsSelectionAndSavedFile() throws Exception {
        logic.execute("add n/Phone Test p/(65) 9123 4567 (HP)");
        logic.execute("view 1");
        logic.execute("find p/(65)");
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> shown = List.copyOf(model.getFilteredPersonList());
        var selection = model.getViewedPerson();
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());

        for (String command : List.of("add n/Invalid Phone p/9123 ext 1234567", "edit 1 p/(65 9123",
                "find p/9123 ext", "find p/(HP)")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(before, model.getAddressBook());
            assertEquals(shown, model.getFilteredPersonList());
            assertEquals(selection, model.getViewedPerson());
            assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()));
        }
    }
}
