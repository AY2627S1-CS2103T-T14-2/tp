package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.model.person.Stage;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code StageCommand}.
 */
public class StageCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StageCommand(null, Stage.GIVING));
        assertThrows(NullPointerException.class, () -> new StageCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_newStageUnfilteredList_success() {
        assertStageChanged(INDEX_FIRST_PERSON, Stage.CULTIVATING, copyOfModel());
    }

    @Test
    public void execute_anyStageToAnyStage_success() {
        // relationships do not move in a straight line: a giving supporter can go back to prospect
        assertStageChanged(INDEX_SECOND_PERSON, Stage.PROSPECT, copyOfModel());
    }

    @Test
    public void execute_anotherSupporterViewed_selectsUpdatedTarget() throws Exception {
        Person previouslyViewedPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        model.setViewedPerson(previouslyViewedPerson);
        Person personToUpdate = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person updatedPerson = new PersonBuilder(personToUpdate).withStage("cultivating").build();

        new StageCommand(INDEX_FIRST_PERSON, Stage.CULTIVATING).execute(model);

        assertEquals(updatedPerson, model.getViewedPerson().orElseThrow());
    }

    @Test
    public void execute_supporterWithOrganisation_organisationPreserved() throws Exception {
        // second typical person has an organisation, which changing the stage must not remove
        Person personToUpdate = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        assertTrue(personToUpdate.getOrganisation().isPresent());

        new StageCommand(INDEX_SECOND_PERSON, Stage.LAPSED).execute(model);

        Person updatedPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        assertEquals(personToUpdate.getOrganisation(), updatedPerson.getOrganisation());
    }

    @Test
    public void execute_supporterWithInteractions_interactionsPreserved() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personWithInteraction = new PersonBuilder(firstPerson)
                .withInteractions(new Interaction(LocalDate.of(2026, 9, 16), "Called"))
                .build();
        model.setPerson(firstPerson, personWithInteraction);

        new StageCommand(INDEX_FIRST_PERSON, Stage.LAPSED).execute(model);

        Person updatedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(personWithInteraction.getInteractions(), updatedPerson.getInteractions());
    }

    @Test
    public void execute_supporterWithIntroducer_introducerPreserved() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person introducedPerson = new PersonBuilder(firstPerson)
                .withIntroducer(secondPerson.getName().fullName)
                .build();
        model.setPerson(firstPerson, introducedPerson);

        new StageCommand(INDEX_FIRST_PERSON, Stage.LAPSED).execute(model);

        Person updatedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(introducedPerson.getIntroducer(), updatedPerson.getIntroducer());
    }

    @Test
    public void execute_sameStage_successWithoutChange() {
        Person previouslyViewedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setViewedPerson(previouslyViewedPerson);
        Person person = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        StageCommand stageCommand = new StageCommand(INDEX_SECOND_PERSON, Stage.GIVING);

        String expectedMessage = String.format(StageCommand.MESSAGE_ALREADY_AT_STAGE, person.getName(), Stage.GIVING);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setViewedPerson(person);

        assertCommandSuccess(stageCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_messageFormat_matchesSpecification() throws Exception {
        String result = new StageCommand(INDEX_SECOND_PERSON, Stage.LAPSED).execute(model).getFeedbackToUser();
        assertEquals("Changed stage of Benson Meier: Giving -> Lapsed.", result);

        result = new StageCommand(INDEX_SECOND_PERSON, Stage.LAPSED).execute(model).getFeedbackToUser();
        assertEquals("Benson Meier is already at stage Lapsed. Nothing was changed.", result);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        StageCommand stageCommand = new StageCommand(outOfBoundIndex, Stage.GIVING);
        Person previouslyViewedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setViewedPerson(previouslyViewedPerson);

        assertCommandFailure(stageCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertEquals(previouslyViewedPerson, model.getViewedPerson().orElseThrow());
    }

    @Test
    public void execute_validIndexFilteredList_successAndFilterKept() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Model expectedModel = copyOfModel();
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);

        assertStageChanged(INDEX_FIRST_PERSON, Stage.GIVING, expectedModel);
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        Person previouslyViewedPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        model.setViewedPerson(previouslyViewedPerson);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // supporter exists in the full list but is not displayed
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        StageCommand stageCommand = new StageCommand(outOfBoundIndex, Stage.GIVING);

        assertCommandFailure(stageCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertEquals(previouslyViewedPerson, model.getViewedPerson().orElseThrow());
    }

    @Test
    public void equals() {
        StageCommand stageFirstGiving = new StageCommand(INDEX_FIRST_PERSON, Stage.GIVING);
        StageCommand stageFirstLapsed = new StageCommand(INDEX_FIRST_PERSON, Stage.LAPSED);
        StageCommand stageSecondGiving = new StageCommand(INDEX_SECOND_PERSON, Stage.GIVING);

        // same object -> returns true
        assertTrue(stageFirstGiving.equals(stageFirstGiving));

        // same values -> returns true
        assertTrue(stageFirstGiving.equals(new StageCommand(INDEX_FIRST_PERSON, Stage.GIVING)));

        // different types -> returns false
        assertFalse(stageFirstGiving.equals(1));

        // null -> returns false
        assertFalse(stageFirstGiving.equals(null));

        // different stage -> returns false
        assertFalse(stageFirstGiving.equals(stageFirstLapsed));

        // different index -> returns false
        assertFalse(stageFirstGiving.equals(stageSecondGiving));
    }

    @Test
    public void toStringMethod() {
        StageCommand stageCommand = new StageCommand(INDEX_FIRST_PERSON, Stage.GIVING);
        String expected = StageCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", stage=" + Stage.GIVING + "}";
        assertEquals(expected, stageCommand.toString());
    }

    /**
     * Executes a stage command for the supporter at {@code index} and checks that only their stage changes and
     * that they become the viewed supporter. {@code expectedModel} starts as a copy of {@code model}.
     */
    private void assertStageChanged(Index index, Stage newStage, Model expectedModel) {
        Person personToUpdate = model.getFilteredPersonList().get(index.getZeroBased());
        Person updatedPerson = new PersonBuilder(personToUpdate).withStage(newStage.toString()).build();
        String expectedMessage = String.format(StageCommand.MESSAGE_STAGE_CHANGED,
                personToUpdate.getName(), personToUpdate.getStage(), newStage);
        expectedModel.setPerson(personToUpdate, updatedPerson);
        expectedModel.setViewedPerson(updatedPerson);

        assertCommandSuccess(new StageCommand(index, newStage), model, expectedMessage, expectedModel);
    }

    private Model copyOfModel() {
        return new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
    }
}
