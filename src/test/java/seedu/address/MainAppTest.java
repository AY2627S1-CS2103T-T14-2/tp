package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.CommandResult;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class MainAppTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data");

    @TempDir
    public Path testFolder;

    private final MainApp mainApp = new MainApp();

    @Test
    public void initModelManager_validFile_loadsSupportersWithoutWarning() throws Exception {
        Path dataFile = testFolder.resolve("supporterbook.json");
        AddressBook expectedData = getTypicalAddressBook();
        Storage storage = createStorage(dataFile);
        storage.saveAddressBook(expectedData);
        byte[] originalContents = Files.readAllBytes(dataFile);

        Model model = mainApp.initModelManager(storage, new UserPrefs());

        assertEquals(expectedData, model.getAddressBook());
        assertEquals("", mainApp.getStartupWarning());
        assertArrayEquals(originalContents, Files.readAllBytes(dataFile));
    }

    @Test
    public void initModelManager_missingFile_loadsSamplesWithoutWarning() {
        Path dataFile = testFolder.resolve("missing.json");

        Model model = mainApp.initModelManager(createStorage(dataFile), new UserPrefs());

        assertEquals(SampleDataUtil.getSampleAddressBook(), model.getAddressBook());
        assertEquals("", mainApp.getStartupWarning());
        assertFalse(Files.exists(dataFile));
    }

    @Test
    public void initModelManager_validEmptyFile_loadsEmptyListWithoutWarning() throws Exception {
        Path dataFile = testFolder.resolve("empty.json");
        Storage storage = createStorage(dataFile);
        storage.saveAddressBook(new AddressBook());

        Model model = mainApp.initModelManager(storage, new UserPrefs());

        assertEquals(new AddressBook(), model.getAddressBook());
        assertEquals("", mainApp.getStartupWarning());
    }

    @Test
    public void initModelManager_savedLongName_loadsWithoutWarningOrChangingFile() throws Exception {
        Path dataFile = testFolder.resolve("long-name.json");
        AddressBook expectedData = new AddressBook();
        expectedData.addPerson(new PersonBuilder().withName("A".repeat(5_000)).build());
        Storage storage = createStorage(dataFile);
        storage.saveAddressBook(expectedData);
        byte[] originalContents = Files.readAllBytes(dataFile);

        Model model = mainApp.initModelManager(storage, new UserPrefs());

        assertEquals(expectedData, model.getAddressBook());
        assertEquals("", mainApp.getStartupWarning());
        assertArrayEquals(originalContents, Files.readAllBytes(dataFile));
    }

    @Test
    public void initModelManager_malformedJson_startsEmptyWithWarning() throws Exception {
        assertInvalidFileStartsEmpty(TEST_DATA_FOLDER.resolve("JsonAddressBookStorageTest")
                .resolve("notJsonFormatAddressBook.json"));
    }

    @Test
    public void initModelManager_trailingJson_startsEmptyWithWarningWithoutChangingFile() throws Exception {
        Path fixture = testFolder.resolve("trailing.json");
        Files.writeString(fixture, "{\"persons\":[]}{\"persons\":[{\"name\":\"Lost\",\"phone\":\"123\","
                + "\"stage\":\"Giving\"}]}");

        assertInvalidFileStartsEmpty(fixture);
    }

    @Test
    public void initModelManager_invalidSupporter_startsEmptyWithWarning() throws Exception {
        assertInvalidFileStartsEmpty(TEST_DATA_FOLDER.resolve("JsonAddressBookStorageTest")
                .resolve("invalidPersonAddressBook.json"));
    }

    @Test
    public void initModelManager_outOfRangeInteractionDate_startsEmptyWithWarning() throws Exception {
        assertInvalidFileStartsEmpty(TEST_DATA_FOLDER.resolve("JsonAddressBookStorageTest")
                .resolve("outOfRangeInteractionDateAddressBook.json"));
    }

    @Test
    public void initModelManager_nullTag_startsEmptyWithWarning() throws Exception {
        assertInvalidFileStartsEmpty(TEST_DATA_FOLDER.resolve("JsonAddressBookStorageTest")
                .resolve("nullTagAddressBook.json"));
    }

    @Test
    public void initModelManager_nullRoot_startsEmptyWithWarning() throws Exception {
        assertNullDataStartsEmpty("null");
    }

    @Test
    public void initModelManager_nullPerson_startsEmptyWithWarning() throws Exception {
        assertNullDataStartsEmpty("{\"persons\":[null]}");
    }

    @Test
    public void initModelManager_validThenNullPerson_startsEmptyWithWarning() throws Exception {
        assertNullDataStartsEmpty("{\"persons\":["
                + "{\"name\":\"Valid Supporter\",\"phone\":\"123\",\"stage\":\"prospect\"},null]}");
    }

    private void assertNullDataStartsEmpty(String contents) throws Exception {
        Path fixture = testFolder.resolve("null-data.json");
        Files.writeString(fixture, contents);
        assertInvalidFileStartsEmpty(fixture);
    }

    @Test
    public void initModelManager_mixedValidAndInvalidSupporters_startsEmptyWithWarning() throws Exception {
        assertInvalidFileStartsEmpty(TEST_DATA_FOLDER.resolve("JsonAddressBookStorageTest")
                .resolve("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void initModelManager_duplicateSupporters_startsEmptyWithWarning() throws Exception {
        assertInvalidFileStartsEmpty(TEST_DATA_FOLDER.resolve("JsonSerializableAddressBookTest")
                .resolve("duplicatePersonAddressBook.json"));
    }

    @Test
    public void initModelManager_successAfterFailure_clearsWarning() throws Exception {
        assertInvalidFileStartsEmpty(TEST_DATA_FOLDER.resolve("JsonAddressBookStorageTest")
                .resolve("notJsonFormatAddressBook.json"));
        Storage storage = createStorage(testFolder.resolve("valid.json"));
        storage.saveAddressBook(getTypicalAddressBook());

        Model model = mainApp.initModelManager(storage, new UserPrefs());

        assertEquals(getTypicalAddressBook(), model.getAddressBook());
        assertEquals("", mainApp.getStartupWarning());
    }

    @Test
    public void execute_commandAfterLoadingFailure_savesEmptyData() throws Exception {
        Path dataFile = testFolder.resolve("supporterbook.json");
        Files.copy(TEST_DATA_FOLDER.resolve("JsonAddressBookStorageTest")
                .resolve("notJsonFormatAddressBook.json"), dataFile);
        Storage storage = createStorage(dataFile);
        Model model = mainApp.initModelManager(storage, new UserPrefs());
        Logic logic = new LogicManager(model, storage);

        CommandResult result = logic.execute("list");

        assertEquals("Listed all supporters.", result.getFeedbackToUser());
        assertEquals(new AddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void initPrefs_nullGuiSettings_usesAndSavesDefaultPrefs() throws Exception {
        Path prefsFile = testFolder.resolve("preferences.json");
        Files.copy(TEST_DATA_FOLDER.resolve("JsonUserPrefsStorageTest")
                .resolve("NullGuiSettingsUserPrefs.json"), prefsFile);
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(prefsFile);

        UserPrefs prefs = mainApp.initPrefs(prefsStorage);

        assertEquals(new UserPrefs(), prefs);
        assertEquals(new UserPrefs(), prefsStorage.readUserPrefs().orElseThrow());
    }

    private Storage createStorage(Path dataFile) {
        return new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
    }

    private void assertInvalidFileStartsEmpty(Path fixture) throws Exception {
        Path dataFile = testFolder.resolve("supporterbook.json");
        Files.copy(fixture, dataFile);
        byte[] originalContents = Files.readAllBytes(dataFile);

        Model model = mainApp.initModelManager(createStorage(dataFile), new UserPrefs());

        assertEquals(new AddressBook(), model.getAddressBook());
        assertEquals("Could not load " + dataFile + " because it is not in the expected format. "
                + "Starting with an empty list. Your next command will overwrite the file, "
                + "so copy it somewhere safe now if you want to repair it.", mainApp.getStartupWarning());
        assertArrayEquals(originalContents, Files.readAllBytes(dataFile));
    }
}
