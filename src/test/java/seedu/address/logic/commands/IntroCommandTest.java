package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.SupporterUpdateTestUtil.createExpectedModelAfterUpdate;
import static seedu.address.testutil.SupporterUpdateTestUtil.createExpectedModelViewing;
import static seedu.address.testutil.SupporterUpdateTestUtil.getPersonAt;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code IntroCommand}.
 */
public class IntroCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new IntroCommand(null, Optional.of(INDEX_FIRST_PERSON)));
        assertThrows(NullPointerException.class, () -> new IntroCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_noIntroducerYet_recordsIntroducer() {
        Person target = getPersonAt(model, INDEX_FIRST_PERSON);
        Person introducer = getPersonAt(model, INDEX_SECOND_PERSON);
        IntroCommand command = new IntroCommand(INDEX_FIRST_PERSON, Optional.of(INDEX_SECOND_PERSON));

        Person expectedPerson = target.withIntroducer(Optional.of(introducer.getName()));
        String expectedMessage = String.format(IntroCommand.MESSAGE_INTRODUCER_SET, introducer.getName(),
                target.getName());
        Model expectedModel = createExpectedModelAfterUpdate(model, target, expectedPerson);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_differentIntroducer_replacesIntroducer() {
        Person target = getPersonAt(model, INDEX_FIRST_PERSON);
        Person oldIntroducer = getPersonAt(model, INDEX_SECOND_PERSON);
        Person newIntroducer = getPersonAt(model, INDEX_THIRD_PERSON);
        Person introducedTarget = target.withIntroducer(Optional.of(oldIntroducer.getName()));
        model.setPerson(target, introducedTarget);

        Person expectedPerson = target.withIntroducer(Optional.of(newIntroducer.getName()));
        String expectedMessage = String.format(IntroCommand.MESSAGE_INTRODUCER_CHANGED, target.getName(),
                oldIntroducer.getName(), newIntroducer.getName());
        Model expectedModel = createExpectedModelAfterUpdate(model, introducedTarget, expectedPerson);

        assertCommandSuccess(new IntroCommand(INDEX_FIRST_PERSON, Optional.of(INDEX_THIRD_PERSON)), model,
                expectedMessage, expectedModel);
    }

    @Test
    public void execute_emptyIntroducerIndex_removesIntroducer() {
        Person target = getPersonAt(model, INDEX_FIRST_PERSON);
        Person introducer = getPersonAt(model, INDEX_SECOND_PERSON);
        Person introducedTarget = target.withIntroducer(Optional.of(introducer.getName()));
        model.setPerson(target, introducedTarget);

        String expectedMessage = String.format(IntroCommand.MESSAGE_INTRODUCER_REMOVED, introducer.getName(),
                target.getName());
        Model expectedModel = createExpectedModelAfterUpdate(model, introducedTarget, target);

        assertCommandSuccess(new IntroCommand(INDEX_FIRST_PERSON, Optional.empty()), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_sameIntroducer_successWithoutChange() {
        Person target = getPersonAt(model, INDEX_FIRST_PERSON);
        Person introducer = getPersonAt(model, INDEX_SECOND_PERSON);
        Person introducedTarget = target.withIntroducer(Optional.of(introducer.getName()));
        model.setPerson(target, introducedTarget);

        String expectedMessage = String.format(IntroCommand.MESSAGE_INTRODUCER_UNCHANGED, introducer.getName(),
                target.getName());
        Model expectedModel = createExpectedModelViewing(model, introducedTarget);

        assertCommandSuccess(new IntroCommand(INDEX_FIRST_PERSON, Optional.of(INDEX_SECOND_PERSON)), model,
                expectedMessage, expectedModel);
    }

    @Test
    public void execute_removeWhenNoIntroducer_successWithoutChange() {
        Person target = getPersonAt(model, INDEX_FIRST_PERSON);
        String expectedMessage = String.format(IntroCommand.MESSAGE_NO_INTRODUCER, target.getName());
        Model expectedModel = createExpectedModelViewing(model, target);

        assertCommandSuccess(new IntroCommand(INDEX_FIRST_PERSON, Optional.empty()), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_ownIndexAsIntroducer_throwsCommandException() {
        assertCommandFailure(new IntroCommand(INDEX_FIRST_PERSON, Optional.of(INDEX_FIRST_PERSON)), model,
                Person.MESSAGE_SELF_INTRODUCTION);
    }

    @Test
    public void execute_introducerIntroducedByTarget_throwsCommandException() {
        // the first supporter introduced the second, so the second cannot have introduced the first
        Person first = getPersonAt(model, INDEX_FIRST_PERSON);
        Person second = getPersonAt(model, INDEX_SECOND_PERSON);
        model.setPerson(second, second.withIntroducer(Optional.of(first.getName())));

        String expectedMessage = String.format(IntroCommand.MESSAGE_INTRODUCTION_LOOP, second.getName(),
                first.getName());
        assertCommandFailure(new IntroCommand(INDEX_FIRST_PERSON, Optional.of(INDEX_SECOND_PERSON)), model,
                expectedMessage);
    }

    @Test
    public void execute_introducerIntroducedByTargetThroughOthers_throwsCommandException() {
        // the first supporter introduced the second, who introduced the third
        Person first = getPersonAt(model, INDEX_FIRST_PERSON);
        Person second = getPersonAt(model, INDEX_SECOND_PERSON);
        Person third = getPersonAt(model, INDEX_THIRD_PERSON);
        model.setPerson(second, second.withIntroducer(Optional.of(first.getName())));
        model.setPerson(third, third.withIntroducer(Optional.of(second.getName())));

        String expectedMessage = String.format(IntroCommand.MESSAGE_INTRODUCTION_LOOP, third.getName(),
                first.getName());
        assertCommandFailure(new IntroCommand(INDEX_FIRST_PERSON, Optional.of(INDEX_THIRD_PERSON)), model,
                expectedMessage);
    }

    @Test
    public void execute_introducerHasOtherIntroducer_recordsIntroducer() {
        // the third supporter introduced the second, so the chain third -> second -> first has no loop
        Person target = getPersonAt(model, INDEX_FIRST_PERSON);
        Person second = getPersonAt(model, INDEX_SECOND_PERSON);
        Person third = getPersonAt(model, INDEX_THIRD_PERSON);
        model.setPerson(second, second.withIntroducer(Optional.of(third.getName())));

        Person expectedPerson = target.withIntroducer(Optional.of(second.getName()));
        String expectedMessage = String.format(IntroCommand.MESSAGE_INTRODUCER_SET, second.getName(),
                target.getName());

        assertCommandSuccess(new IntroCommand(INDEX_FIRST_PERSON, Optional.of(INDEX_SECOND_PERSON)), model,
                expectedMessage, createExpectedModelAfterUpdate(model, target, expectedPerson));
    }

    @Test
    public void execute_lastDisplayedIndexAsIntroducer_recordsIntroducer() {
        // the largest valid introducer index
        Index lastIndex = Index.fromOneBased(model.getFilteredPersonList().size());
        Person target = getPersonAt(model, INDEX_FIRST_PERSON);
        Person introducer = getPersonAt(model, lastIndex);

        Person expectedPerson = target.withIntroducer(Optional.of(introducer.getName()));
        String expectedMessage = String.format(IntroCommand.MESSAGE_INTRODUCER_SET, introducer.getName(),
                target.getName());

        assertCommandSuccess(new IntroCommand(INDEX_FIRST_PERSON, Optional.of(lastIndex)), model,
                expectedMessage, createExpectedModelAfterUpdate(model, target, expectedPerson));
    }

    @Test
    public void execute_invalidTargetIndex_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new IntroCommand(outOfBoundIndex, Optional.of(INDEX_FIRST_PERSON)), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIntroducerIndex_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new IntroCommand(INDEX_FIRST_PERSON, Optional.of(outOfBoundIndex)), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_introducerHiddenByFilter_throwsCommandException() {
        // both supporters must be in the displayed list
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new IntroCommand(INDEX_FIRST_PERSON, Optional.of(INDEX_SECOND_PERSON)), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredList_successAndFilterKept() throws Exception {
        Person target = getPersonAt(model, INDEX_SECOND_PERSON);
        Person introducer = getPersonAt(model, INDEX_FIRST_PERSON);
        model.updateFilteredPersonList(person -> person.isSamePerson(target) || person.isSamePerson(introducer));

        new IntroCommand(INDEX_SECOND_PERSON, Optional.of(INDEX_FIRST_PERSON)).execute(model);

        assertEquals(2, model.getFilteredPersonList().size());
        Person updatedPerson = getPersonAt(model, INDEX_SECOND_PERSON);
        assertTrue(updatedPerson.isIntroducedBy(introducer.getName()));
        assertEquals(Optional.of(updatedPerson), model.getViewedPerson());
    }

    @Test
    public void equals() {
        IntroCommand setFirst = new IntroCommand(INDEX_FIRST_PERSON, Optional.of(INDEX_SECOND_PERSON));
        IntroCommand setFirstCopy = new IntroCommand(INDEX_FIRST_PERSON, Optional.of(INDEX_SECOND_PERSON));
        IntroCommand removeFirst = new IntroCommand(INDEX_FIRST_PERSON, Optional.empty());

        // same object -> returns true
        assertTrue(setFirst.equals(setFirst));

        // same values -> returns true
        assertTrue(setFirst.equals(setFirstCopy));

        // different types -> returns false
        assertFalse(setFirst.equals(1));

        // null -> returns false
        assertFalse(setFirst.equals(null));

        // different target -> returns false
        assertFalse(setFirst.equals(new IntroCommand(INDEX_SECOND_PERSON, Optional.of(INDEX_SECOND_PERSON))));

        // different introducer -> returns false
        assertFalse(setFirst.equals(removeFirst));
    }

    @Test
    public void toStringMethod() {
        IntroCommand command = new IntroCommand(INDEX_FIRST_PERSON, Optional.of(INDEX_SECOND_PERSON));
        String expected = IntroCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", introducerIndex=" + Optional.of(INDEX_SECOND_PERSON) + "}";
        assertEquals(expected, command.toString());
    }
}
