package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code StatsCommand}.
 */
public class StatsCommandTest {

    private static final String EXPECTED_MESSAGE = "Supporters by stage (4 in total):\n"
            + "Prospect: 1\n"
            + "Contacted: 0\n"
            + "Cultivating: 0\n"
            + "Giving: 2\n"
            + "Lapsed: 1\n"
            + "Declined: 0";

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        AddressBook addressBook = new AddressBookBuilder()
                .withPerson(new PersonBuilder().withName("Alice Tan").withStage("giving").build())
                .withPerson(new PersonBuilder().withName("Benson Lim").withStage("giving").build())
                .withPerson(new PersonBuilder().withName("Carl Ng").withStage("lapsed").build())
                .withPerson(new PersonBuilder().withName("Daniel Koh").build())
                .build();
        model = new ModelManager(addressBook, new UserPrefs());
        expectedModel = new ModelManager(addressBook, new UserPrefs());
    }

    @Test
    public void execute_supportersAtSomeStages_countsEveryStage() {
        // stages with no supporters are still listed, with a count of 0
        assertCommandSuccess(new StatsCommand(), model, EXPECTED_MESSAGE, expectedModel);
    }

    @Test
    public void execute_noSupporters_allCountsZero() {
        String expectedMessage = "Supporters by stage (0 in total):\n"
                + "Prospect: 0\n"
                + "Contacted: 0\n"
                + "Cultivating: 0\n"
                + "Giving: 0\n"
                + "Lapsed: 0\n"
                + "Declined: 0";
        assertCommandSuccess(new StatsCommand(), new ModelManager(), expectedMessage, new ModelManager());
    }

    @Test
    public void execute_filteredList_countsAllSupportersAndKeepsFilter() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);

        assertCommandSuccess(new StatsCommand(), model, EXPECTED_MESSAGE, expectedModel);
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StatsCommand().execute(null));
    }
}
