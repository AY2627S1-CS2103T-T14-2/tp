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
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests email validation through command parsing, searching and saved-data reloads.
 */
public class EmailCommandIntegrationTest {
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
    public void execute_consecutiveDomainHyphens_addsEditsSearchesAndReloads() throws Exception {
        logic.execute("add n/Domain Test e/contact@foo--bar.com");
        logic.execute("edit 1 e/contact@xn--bcher-kva.de");
        logic.execute("find e/@xn--bcher-kva.de");

        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals("contact@xn--bcher-kva.de",
                model.getFilteredPersonList().getFirst().getEmail().orElseThrow().value);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_veryLongEmail_addsAndReloadsWithoutStackOverflow() throws Exception {
        String email = "a.".repeat(5000) + "a@example.com";
        logic.execute("add n/Long Email e/" + email);

        assertEquals(email, model.getAddressBook().getPersonList().getFirst().getEmail().orElseThrow().value);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_invalidLongEmail_preservesDataSelectionAndSavedFile() throws Exception {
        logic.execute("add n/Email Test e/contact@example.com");
        logic.execute("view 1");
        AddressBook before = new AddressBook(model.getAddressBook());
        var selection = model.getViewedPerson();
        byte[] saved = Files.readAllBytes(storage.getAddressBookFilePath());
        String email = "a.".repeat(5000) + "@example.com";

        for (String command : List.of("add n/Long Email e/" + email, "edit 1 e/" + email)) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(before, model.getAddressBook());
            assertEquals(selection, model.getViewedPerson());
            assertArrayEquals(saved, Files.readAllBytes(storage.getAddressBookFilePath()));
        }
    }
}
