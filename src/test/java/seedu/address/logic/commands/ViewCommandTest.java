package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code ViewCommand}.
 */
public class ViewCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCommand(INDEX_FIRST_PERSON).execute(null));
    }

    @Test
    public void execute_noInteractions_selectsSupporterAndReportsEmptyHistory() throws Exception {
        Person personToView = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        assertViewSuccess(INDEX_FIRST_PERSON, "Showing " + personToView.getName() + ": no interactions logged yet");
    }

    @Test
    public void execute_oneInteraction_selectsSupporterAndReportsCount() throws Exception {
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Interaction interaction = new Interaction(LocalDate.of(2026, 9, 16), "Called about volunteering");
        Person personToView = new PersonBuilder(originalPerson).withInteractions(interaction).build();
        model.setPerson(originalPerson, personToView);

        assertViewSuccess(INDEX_FIRST_PERSON, "Showing " + personToView.getName() + ": 1 interaction(s) logged");
    }

    @Test
    public void execute_multipleInteractions_selectsSupporterAndReportsCount() throws Exception {
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Interaction firstInteraction = new Interaction(LocalDate.of(2026, 9, 16), "Called about volunteering");
        Interaction secondInteraction = new Interaction(LocalDate.of(2026, 9, 17), "Met to discuss the appeal");
        Person personToView = new PersonBuilder(originalPerson)
                .withInteractions(firstInteraction, secondInteraction, secondInteraction)
                .build();
        model.setPerson(originalPerson, personToView);

        assertViewSuccess(INDEX_FIRST_PERSON, "Showing " + personToView.getName() + ": 3 interaction(s) logged");
    }

    @Test
    public void execute_validIndexFilteredList_selectsDisplayedSupporterAndKeepsFilter() throws Exception {
        Person personToView = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertEquals(List.of(personToView), model.getFilteredPersonList());

        assertViewSuccess(INDEX_FIRST_PERSON, "Showing " + personToView.getName() + ": no interactions logged yet");
    }

    @Test
    public void execute_anotherSupporter_switchesViewedSupporter() throws Exception {
        new ViewCommand(INDEX_FIRST_PERSON).execute(model);
        Person personToView = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());

        assertViewSuccess(INDEX_SECOND_PERSON, "Showing " + personToView.getName() + ": no interactions logged yet");
    }

    @Test
    public void execute_invalidIndexUnfilteredList_preservesPreviousSelection() {
        Person previouslyViewedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setViewedPerson(previouslyViewedPerson);
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new ViewCommand(invalidIndex), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertEquals(Optional.of(previouslyViewedPerson), model.getViewedPerson());
    }

    @Test
    public void execute_invalidIndexFilteredList_preservesPreviousSelection() {
        Person previouslyViewedPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        model.setViewedPerson(previouslyViewedPerson);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertTrue(INDEX_SECOND_PERSON.getZeroBased() < model.getAddressBook().getPersonList().size());

        assertCommandFailure(new ViewCommand(INDEX_SECOND_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertEquals(Optional.of(previouslyViewedPerson), model.getViewedPerson());
    }

    @Test
    public void execute_emptyDisplayedList_preservesEmptySelection() {
        model.updateFilteredPersonList(unused -> false);

        assertCommandFailure(new ViewCommand(INDEX_FIRST_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertEquals(Optional.empty(), model.getViewedPerson());
    }

    @Test
    public void equals() {
        ViewCommand viewFirstCommand = new ViewCommand(INDEX_FIRST_PERSON);
        ViewCommand viewFirstCommandCopy = new ViewCommand(INDEX_FIRST_PERSON);
        ViewCommand viewSecondCommand = new ViewCommand(INDEX_SECOND_PERSON);

        assertTrue(viewFirstCommand.equals(viewFirstCommand));
        assertTrue(viewFirstCommand.equals(viewFirstCommandCopy));
        assertFalse(viewFirstCommand.equals(1));
        assertFalse(viewFirstCommand.equals(null));
        assertFalse(viewFirstCommand.equals(viewSecondCommand));
    }

    @Test
    public void toStringMethod() {
        ViewCommand viewCommand = new ViewCommand(INDEX_FIRST_PERSON);
        String expected = ViewCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON + "}";
        assertEquals(expected, viewCommand.toString());
    }

    /**
     * Verifies feedback and selection while checking that supporter data and the displayed list are unchanged.
     */
    private void assertViewSuccess(Index targetIndex, String expectedMessage) throws Exception {
        AddressBook expectedAddressBook = new AddressBook(model.getAddressBook());
        List<Person> expectedDisplayedPersons = List.copyOf(model.getFilteredPersonList());
        Person expectedViewedPerson = expectedDisplayedPersons.get(targetIndex.getZeroBased());

        CommandResult result = new ViewCommand(targetIndex).execute(model);

        assertEquals(new CommandResult(expectedMessage), result);
        assertEquals(Optional.of(expectedViewedPerson), model.getViewedPerson());
        assertEquals(expectedAddressBook, model.getAddressBook());
        assertEquals(expectedDisplayedPersons, model.getFilteredPersonList());
    }
}
