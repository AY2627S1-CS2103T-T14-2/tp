package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalInteractionNotes.CHARACTER_PRESERVATION_NOTES;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
    public void readAndSaveAddressBook_specialCharacterNotes_preservesNotes() throws Exception {
        List<String> notes = new ArrayList<>(CHARACTER_PRESERVATION_NOTES);
        // also check persistence of a valid boundary note rejected by the former UTF-16 length check
        notes.add("a".repeat(499) + "\ud83d\ude00");
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
    public void readAddressBook_nullTag_throwsDataLoadingExceptionWithoutChangingFile() throws Exception {
        Path dataFile = testFolder.resolve("supporterbook.json");
        Files.copy(TEST_DATA_FOLDER.resolve("nullTagAddressBook.json"), dataFile);
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
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
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
