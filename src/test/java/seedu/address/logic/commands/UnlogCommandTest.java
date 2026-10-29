package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.InteractionCommandTestUtil.assertFailurePreservesSelection;
import static seedu.address.testutil.InteractionCommandTestUtil.setInteractions;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalInteractions.MIDDLE;
import static seedu.address.testutil.TypicalInteractions.NEWEST;
import static seedu.address.testutil.TypicalInteractions.OLDEST;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

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
 * Contains integration tests (interaction with the Model) and unit tests for {@code UnlogCommand}.
 */
public class UnlogCommandTest {


    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new UnlogCommand(null, Index.fromOneBased(1)));
        assertThrows(NullPointerException.class, () -> new UnlogCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1)).execute(null));
    }

    @Test
    public void execute_middleEntry_removesChronologicalEntryAndPreservesSupporter() {
        Person original = model.getFilteredPersonList().get(0);
        Person introducer = model.getFilteredPersonList().get(1);

        // entry #2 is stored first, so chronological numbering must not be treated as stored position
        Person person = new PersonBuilder(original).withIntroducer(introducer.getName().fullName)
                .withInteractions(MIDDLE, OLDEST, NEWEST).build();
        model.setPerson(original, person);
        // successful deletion should select the target even when another supporter was previously viewed
        model.setViewedPerson(introducer);
        Person updated = new PersonBuilder(person).withInteractions(OLDEST, NEWEST).build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(person, updated);
        expectedModel.setViewedPerson(updated);

        String message = String.format(UnlogCommand.MESSAGE_SUCCESS, 2, person.getName(), "16 Sep 2026", "Meeting");
        assertCommandSuccess(new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(2)),
                model, message, expectedModel);
    }

    @Test
    public void execute_oldestEntry_removesOldest() throws Exception {
        setInteractions(model, INDEX_FIRST_PERSON, MIDDLE, OLDEST, NEWEST);

        new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1)).execute(model);

        assertEquals(List.of(MIDDLE, NEWEST), model.getFilteredPersonList().get(0).getInteractions());
    }

    @Test
    public void execute_newestEntry_removesNewest() throws Exception {
        setInteractions(model, INDEX_FIRST_PERSON, NEWEST, OLDEST, MIDDLE);

        new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(3)).execute(model);

        assertEquals(List.of(OLDEST, MIDDLE), model.getFilteredPersonList().get(0).getInteractions());
    }

    @Test
    public void execute_identicalEntries_removesSelectedStoredPosition() throws Exception {
        Interaction sameDateInteraction = new Interaction(MIDDLE.getDate(), "Different note");
        setInteractions(model, INDEX_FIRST_PERSON, MIDDLE, sameDateInteraction,
                new Interaction(MIDDLE.getDate(), MIDDLE.getNote()));

        // delete displayed entry #3, which maps to stored index 2
        // removing by value would incorrectly remove identical entry at stored index 0 instead
        new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(3)).execute(model);

        assertEquals(List.of(MIDDLE, sameDateInteraction), model.getFilteredPersonList().get(0).getInteractions());
    }

    @Test
    public void execute_lastEntry_selectsSupporterWithEmptyHistory() throws Exception {
        Person person = setInteractions(model, INDEX_FIRST_PERSON, OLDEST);
        Person updated = new PersonBuilder(person).withInteractions().build();

        CommandResult result = new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1)).execute(model);

        assertEquals(List.of(), model.getFilteredPersonList().get(0).getInteractions());
        assertEquals(Optional.of(updated), model.getViewedPerson());
        assertEquals(String.format(UnlogCommand.MESSAGE_SUCCESS, 1, person.getName(), "06 Sep 2026", "Earlier call"),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_validFilteredIndex_preservesFilter() throws Exception {
        // show only the second supporter, whose displayed index is now 1
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person person = setInteractions(model, INDEX_FIRST_PERSON, MIDDLE, OLDEST);
        Person updated = new PersonBuilder(person).withInteractions(MIDDLE).build();

        new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1)).execute(model);

        assertEquals(List.of(updated), model.getFilteredPersonList());
        assertEquals(Optional.of(updated), model.getViewedPerson());
    }

    @Test
    public void execute_invalidSupporterIndex_throwsCommandException() {
        model.setViewedPerson(model.getFilteredPersonList().get(0));
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertFailurePreservesSelection(new UnlogCommand(invalidIndex, Index.fromOneBased(1)), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidFilteredIndex_throwsCommandException() {
        model.setViewedPerson(model.getFilteredPersonList().get(1));
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // supporter exists in the full list but is not displayed
        assertFailurePreservesSelection(new UnlogCommand(INDEX_SECOND_PERSON, Index.fromOneBased(1)), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidInteractionIndex_preservesDataAndSelection() {
        setInteractions(model, INDEX_FIRST_PERSON, OLDEST);
        model.setViewedPerson(model.getFilteredPersonList().get(1));

        assertFailurePreservesSelection(new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(2)), model,
                Messages.MESSAGE_INVALID_INTERACTION_INDEX);
    }

    @Test
    public void execute_emptyHistory_throwsCommandException() {
        setInteractions(model, INDEX_FIRST_PERSON);
        model.setViewedPerson(model.getFilteredPersonList().get(1));

        assertFailurePreservesSelection(new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1)), model,
                Messages.MESSAGE_INVALID_INTERACTION_INDEX);
    }

    @Test
    public void equals() {
        UnlogCommand command = new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1));

        // same object -> returns true
        assertTrue(command.equals(command));

        // same values -> returns true
        assertTrue(command.equals(new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(1))));

        // null -> returns false
        assertFalse(command.equals(null));

        // different types -> returns false
        assertFalse(command.equals(1));

        // different supporter index -> returns false
        assertFalse(command.equals(new UnlogCommand(INDEX_SECOND_PERSON, Index.fromOneBased(1))));

        // different interaction index -> returns false
        assertFalse(command.equals(new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(2))));
    }

    @Test
    public void toStringMethod() {
        UnlogCommand command = new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(2));
        assertEquals(UnlogCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", interactionIndex=" + Index.fromOneBased(2) + "}", command.toString());
    }

}
