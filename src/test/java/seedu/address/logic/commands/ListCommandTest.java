package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.ELLE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;
import static seedu.address.testutil.TypicalPersons.getTypicalPersons;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_afterFind_showsEverythingInOriginalOrder() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("find uR ell").execute(model);
        assertEquals(List.of(CARL, ELLE), model.getFilteredPersonList());

        // List ignores trailing parameters and restores the full list.
        assertCommandSuccess(parser.parseCommand("list 123"), model, "Listed all supporters.", expectedModel);
        assertEquals(getTypicalPersons(), model.getFilteredPersonList());
    }

    @Test
    public void execute_afterFindWithNoMatches_showsEverything() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("find @").execute(model);
        assertEquals(List.of(), model.getFilteredPersonList());

        assertCommandSuccess(parser.parseCommand("list"), model, "Listed all supporters.", expectedModel);
        assertEquals(getTypicalPersons(), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyAddressBook_showsEmptyList() {
        Model emptyModel = new ModelManager();

        assertCommandSuccess(new ListCommand(), emptyModel, "Listed all supporters.", new ModelManager());
        assertEquals(List.of(), emptyModel.getFilteredPersonList());
    }
}
