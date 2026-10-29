package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalInteractionNotes.ASCII_AND_EMOJI_AT_NOTE_LIMIT;
import static seedu.address.testutil.TypicalInteractionNotes.CHARACTER_PRESERVATION_NOTES;
import static seedu.address.testutil.TypicalOrganisationNames.ASCII_AND_EMOJI_AT_ORGANISATION_LIMIT;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_outOfRangeInteractionDates_throwsWithoutChangingFile() throws Exception {
        String fixture = Files.readString(TEST_DATA_FOLDER.resolve("outOfRangeInteractionDateAddressBook.json"));
        // use a date well beyond today here; exact boundaries are covered with a fixed clock in the adapter tests
        for (String date : List.of("1899-12-31", LocalDate.now().plusYears(1).toString())) {
            Path dataFile = testFolder.resolve("interaction-" + date + ".json");
            Files.writeString(dataFile, fixture.replace("1899-12-31", date));
            byte[] originalContents = Files.readAllBytes(dataFile);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);

            // a valid entry preceding the invalid one must not cause a partially loaded history to be returned
            assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertArrayEquals(originalContents, Files.readAllBytes(dataFile));
        }
    }

    @Test
    public void readAddressBook_nullTag_throwsDataLoadingExceptionWithoutChangingFile() throws Exception {
        Path dataFile = testFolder.resolve("supporterbook.json");
        Files.copy(TEST_DATA_FOLDER.resolve("nullTagAddressBook.json"), dataFile);
        byte[] originalContents = Files.readAllBytes(dataFile);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertArrayEquals(originalContents, Files.readAllBytes(dataFile));
    }

    @Test
    public void readAddressBook_trailingContent_throwsWithoutReturningPartialData() throws Exception {
        Path dataFile = testFolder.resolve("trailing.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
        for (String suffix : List.of("not json", "{\"persons\":[{\"name\":\"Lost\",\"phone\":\"123\","
                + "\"stage\":\"Giving\"}]}")) {
            String json = "{\"persons\":[]}" + suffix;
            Files.writeString(dataFile, json);

            assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertEquals(json, Files.readString(dataFile));
        }
    }

    @Test
    public void readAddressBook_veryLongPhone_throwsDataLoadingExceptionWithoutChangingFile() throws Exception {
        Path dataFile = testFolder.resolve("longPhone.json");
        String json = "{\"persons\":[{\"name\":\"Phone Test\",\"stage\":\"Giving\",\"phone\":\""
                + "1-".repeat(5000) + "1\"}]}";
        Files.writeString(dataFile, json);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertEquals(json, Files.readString(dataFile));
    }

    @Test
    public void readAddressBook_nullRoot_throwsDataLoadingExceptionWithoutChangingFile() throws Exception {
        assertNullDataRejectedWithoutChangingFile("null");
    }

    @Test
    public void readAddressBook_nullPerson_throwsDataLoadingExceptionWithoutChangingFile() throws Exception {
        assertNullDataRejectedWithoutChangingFile("{\"persons\":[null]}");
    }

    @Test
    public void readAddressBook_validThenNullPerson_throwsDataLoadingExceptionWithoutChangingFile() throws Exception {
        assertNullDataRejectedWithoutChangingFile("{\"persons\":["
                + "{\"name\":\"Valid Supporter\",\"phone\":\"123\",\"stage\":\"prospect\",\"tags\":[]},null]}");
    }

    private void assertNullDataRejectedWithoutChangingFile(String contents) throws Exception {
        Path dataFile = testFolder.resolve("supporterbook.json");
        Files.writeString(dataFile, contents);
        byte[] originalContents = Files.readAllBytes(dataFile);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertArrayEquals(originalContents, Files.readAllBytes(dataFile));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        Person aliceWithInteraction = new PersonBuilder(ALICE)
                .withInteractions(new Interaction(LocalDate.of(2026, 9, 16), "Called"))
                .build();
        original.setPerson(ALICE, aliceWithInteraction);
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(aliceWithInteraction);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    @ResourceLock("java.util.Locale.default")
    public void readAndSaveAddressBook_turkishDefaultLocale_preservesStages() throws Exception {
        AddressBook book = getTypicalAddressBook();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("locale.json"));
        storage.saveAddressBook(book);
        String originalContents = Files.readString(storage.getAddressBookFilePath());
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            ReadOnlyAddressBook restored = storage.readAddressBook().orElseThrow();
            assertEquals(book, restored);

            storage.saveAddressBook(restored);
            assertEquals(originalContents, Files.readString(storage.getAddressBookFilePath()));
            assertEquals(book, storage.readAddressBook().orElseThrow());
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    @Test
    public void readAndSaveAddressBook_organisationAtUnicodeLimit_preservesOrganisation() throws Exception {
        String organisationAtLimit = ASCII_AND_EMOJI_AT_ORGANISATION_LIMIT;
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder(ALICE).withOrganisation(organisationAtLimit).build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("unicodeOrganisation.json"));

        // exercise actual JSON serialization and deserialization, including the boundary-length emoji
        storage.saveAddressBook(book);
        assertEquals(book, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void readAndSaveAddressBook_specialCharacterNotes_preservesNotes() throws Exception {
        List<String> notes = new ArrayList<>(CHARACTER_PRESERVATION_NOTES);
        // also check persistence of a valid boundary note rejected by the former UTF-16 length check
        notes.add(ASCII_AND_EMOJI_AT_NOTE_LIMIT);
        Interaction[] interactions = notes.stream()
                .map(note -> new Interaction(LocalDate.of(2024, 2, 29), note))
                .toArray(Interaction[]::new);
        Person supporter = new PersonBuilder(ALICE).withInteractions(interactions).build();
        AddressBook original = new AddressBook();
        original.addPerson(supporter);
        Path filePath = testFolder.resolve("specialCharacterNotes.json");
        // write actual JSON, then read it through a fresh storage instance to exercise escaping and decoding
        new JsonAddressBookStorage(filePath).saveAddressBook(original);

        ReadOnlyAddressBook restored = new JsonAddressBookStorage(filePath).readAddressBook().orElseThrow();
        List<String> restoredNotes = restored.getPersonList().get(0).getInteractions().stream()
                .map(Interaction::getNote)
                .toList();
        assertEquals(notes, restoredNotes);
        assertEquals(original, new AddressBook(restored));
    }

    @Test
    public void readAndSaveAddressBook_annotatedPhones_preservesFormatting() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder(ALICE).withPhone("9123 4567 (HP)").build());
        book.addPerson(new PersonBuilder(HOON).withPhone("(65) 9123 4567").build());
        book.addPerson(new PersonBuilder(IDA).withPhone("9123 4567 ext 12").build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("annotated.json"));

        storage.saveAddressBook(book);

        assertEquals(book, storage.readAddressBook().orElseThrow());
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
