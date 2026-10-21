package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.ELLE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

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
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests field searches through parsing, execution, storage and undo history.
 */
public class FindCommandIntegrationTest {
    @TempDir
    private Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage addressBookStorage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("supporterbook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"));
        logic = new LogicManager(model, new StorageManager(addressBookStorage, userPrefsStorage));
    }

    @Test
    public void execute_combinedFields_matchesAllSpecifiedGroups() throws Exception {
        CommandResult result = logic.execute("find meier o/DBS t/FRIEND st/giving p/98-76 e/@EXAMPLE");

        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertEquals("1 supporter(s) listed!", result.getFeedbackToUser());
    }

    @Test
    public void execute_repeatedStages_retainsOriginalOrder() throws Exception {
        logic.execute("find st/lapsed st/giving");

        assertEquals(List.of(BENSON, ELLE), model.getFilteredPersonList());
    }

    @Test
    public void execute_repeatedTags_returnsEachSupporterOnce() throws Exception {
        logic.execute("find t/FRIEND t/OWES t/friend");

        assertEquals(List.of(ALICE, BENSON, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_replacementSearch_searchesAllSupporters() throws Exception {
        logic.execute("find o/DBS");
        assertEquals(List.of(BENSON), model.getFilteredPersonList());

        logic.execute("find e/werner");
        assertEquals(List.of(ELLE), model.getFilteredPersonList());

        logic.execute("find Pauline");
        assertEquals(List.of(ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_listAfterSearch_restoresAllSupportersInOriginalOrder() throws Exception {
        logic.execute("find st/giving");
        logic.execute("list");

        assertEquals(getTypicalAddressBook().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_search_preservesViewedSupporterAndStoredData() throws Exception {
        AddressBook original = new AddressBook(model.getAddressBook());
        logic.execute("view 2");
        logic.execute("find st/lapsed");

        assertEquals(Optional.of(BENSON), model.getViewedPerson());
        assertEquals(original, model.getAddressBook());
        assertEquals(original, addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_viewAndDeleteAfterSearch_useDisplayedIndex() throws Exception {
        logic.execute("find st/lapsed");
        logic.execute("view 1");
        assertEquals(Optional.of(ELLE), model.getViewedPerson());

        logic.execute("delete 1");

        AddressBook expected = getTypicalAddressBook();
        expected.removePerson(ELLE);
        assertEquals(expected, model.getAddressBook());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertTrue(model.getViewedPerson().isEmpty());
    }

    @Test
    public void execute_editAfterSearch_updatesDisplayedSupporter() throws Exception {
        logic.execute("find st/lapsed");
        logic.execute("edit 1 o/OCBC Bank");

        Person editedElle = new PersonBuilder(ELLE).withOrganisation("OCBC Bank").build();
        AddressBook expected = getTypicalAddressBook();
        expected.setPerson(ELLE, editedElle);
        assertEquals(expected, model.getAddressBook());
        assertEquals(expected.getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_stageAfterSearch_refreshesMatchingSupporters() throws Exception {
        logic.execute("find st/lapsed");
        logic.execute("stage 1 giving");

        assertTrue(model.getFilteredPersonList().isEmpty());
        Person updatedElle = new PersonBuilder(ELLE).withStage("giving").build();
        AddressBook expected = getTypicalAddressBook();
        expected.setPerson(ELLE, updatedElle);
        assertEquals(expected, model.getAddressBook());
    }

    @Test
    public void execute_noMatches_showsEmptyListWithoutChangingData() throws Exception {
        CommandResult result = logic.execute("find o/Nonexistent Organisation");

        assertEquals("0 supporter(s) listed!", result.getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertThrows(CommandException.class, () -> logic.execute("delete 1"));
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
    }

    @Test
    public void execute_invalidSearch_preservesPreviousResultsAndSelection() throws Exception {
        logic.execute("view 2");
        logic.execute("find st/lapsed");

        assertThrows(ParseException.class, () -> logic.execute("find st/giv"));
        assertThrows(ParseException.class, () -> logic.execute("find o/"));
        assertThrows(ParseException.class, () -> logic.execute("find p/+"));

        assertEquals(List.of(ELLE), model.getFilteredPersonList());
        assertEquals(Optional.of(BENSON), model.getViewedPerson());
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
    }

    @Test
    public void execute_search_doesNotCreateUndoPoint() throws Exception {
        logic.execute("find st/giving");

        assertFalse(model.canUndoAddressBook());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_searchAfterUndo_preservesRedoHistory() throws Exception {
        logic.execute("delete 1");
        logic.execute("undo");
        logic.execute("find st/giving");
        assertTrue(model.canRedoAddressBook());

        logic.execute("redo");
        AddressBook deletedAlice = getTypicalAddressBook();
        deletedAlice.removePerson(ALICE);
        assertEquals(deletedAlice, model.getAddressBook());

        logic.execute("undo");
        assertEquals(getTypicalAddressBook(), model.getAddressBook());
    }
}
