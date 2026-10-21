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
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code UntagCommand}.
 */
public class UntagCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new UntagCommand(null, tags("vip")));
        assertThrows(NullPointerException.class, () -> new UntagCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void constructor_noTags_throwsAssertionError() {
        // the parser requires at least one t/, so an empty set is a programming error
        assertThrows(AssertionError.class, () -> new UntagCommand(INDEX_FIRST_PERSON, Set.of()));
    }

    @Test
    public void execute_existingTag_removesTagAndKeepsOthers() {
        // second typical person is tagged "owesMoney" and "friends"
        Person target = personAt(INDEX_SECOND_PERSON);
        Person expectedPerson = new PersonBuilder(target).withTags("friends").build();

        String expectedMessage = String.format(UntagCommand.MESSAGE_TAGS_REMOVED, target.getName(), "[owesMoney]");
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, expectedPerson);
        expectedModel.setViewedPerson(expectedPerson);

        assertCommandSuccess(new UntagCommand(INDEX_SECOND_PERSON, tags("owesMoney")), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_differentCase_removesTagAsStored() {
        // "FRIENDS" removes the first typical person's "friends" tag, and the message shows it as stored
        Person target = personAt(INDEX_FIRST_PERSON);
        Person expectedPerson = new PersonBuilder(target).withTags().build();

        String expectedMessage = String.format(UntagCommand.MESSAGE_TAGS_REMOVED, target.getName(), "[friends]");
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, expectedPerson);
        expectedModel.setViewedPerson(expectedPerson);

        assertCommandSuccess(new UntagCommand(INDEX_FIRST_PERSON, tags("FRIENDS")), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_someTagsMissing_skipsThem() {
        Person target = personAt(INDEX_SECOND_PERSON);
        Person expectedPerson = new PersonBuilder(target).withTags("owesMoney").build();

        String expectedMessage = String.format(UntagCommand.MESSAGE_TAGS_REMOVED, target.getName(), "[friends]")
                + "\n" + String.format(UntagCommand.MESSAGE_TAGS_SKIPPED, "[donor] [vip]");
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, expectedPerson);
        expectedModel.setViewedPerson(expectedPerson);

        assertCommandSuccess(new UntagCommand(INDEX_SECOND_PERSON, tags("vip", "friends", "donor")), model,
                expectedMessage, expectedModel);
    }

    @Test
    public void execute_noMatchingTags_successWithoutChange() {
        // third typical person has no tags
        Person target = personAt(INDEX_THIRD_PERSON);

        String expectedMessage = String.format(UntagCommand.MESSAGE_NOTHING_REMOVED, target.getName(), "[vip]");
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setViewedPerson(target);

        assertCommandSuccess(new UntagCommand(INDEX_THIRD_PERSON, tags("vip")), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_tagsDifferingOnlyInCase_removesBoth() {
        Person target = personAt(INDEX_THIRD_PERSON);
        Person taggedTwice = new PersonBuilder(target).withTags("VIP", "vip", "donor").build();
        model.setPerson(target, taggedTwice);
        Person expectedPerson = new PersonBuilder(target).withTags("donor").build();

        String expectedMessage = String.format(UntagCommand.MESSAGE_TAGS_REMOVED, target.getName(), "[VIP] [vip]");
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(taggedTwice, expectedPerson);
        expectedModel.setViewedPerson(expectedPerson);

        assertCommandSuccess(new UntagCommand(INDEX_THIRD_PERSON, tags("vip")), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_filteredList_successAndFilterKept() throws Exception {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        new UntagCommand(INDEX_FIRST_PERSON, tags("friends")).execute(model);

        assertEquals(1, model.getFilteredPersonList().size());
        Person updatedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(Set.of(new Tag("owesMoney")), updatedPerson.getTags());
        assertEquals(Optional.of(updatedPerson), model.getViewedPerson());
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new UntagCommand(outOfBoundIndex, tags("vip")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new UntagCommand(INDEX_SECOND_PERSON, tags("vip")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        UntagCommand untagFirst = new UntagCommand(INDEX_FIRST_PERSON, tags("vip"));

        // same object -> returns true
        assertTrue(untagFirst.equals(untagFirst));

        // same values -> returns true
        assertTrue(untagFirst.equals(new UntagCommand(INDEX_FIRST_PERSON, tags("vip"))));

        // different types -> returns false
        assertFalse(untagFirst.equals(1));

        // null -> returns false
        assertFalse(untagFirst.equals(null));

        // different index -> returns false
        assertFalse(untagFirst.equals(new UntagCommand(INDEX_SECOND_PERSON, tags("vip"))));

        // different tags -> returns false
        assertFalse(untagFirst.equals(new UntagCommand(INDEX_FIRST_PERSON, tags("donor"))));
    }

    @Test
    public void toStringMethod() {
        Set<Tag> tagsToRemove = tags("vip");
        UntagCommand untagCommand = new UntagCommand(INDEX_FIRST_PERSON, tagsToRemove);
        String expected = UntagCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", tagsToRemove=" + tagsToRemove + "}";
        assertEquals(expected, untagCommand.toString());
    }

    private Person personAt(Index index) {
        return model.getFilteredPersonList().get(index.getZeroBased());
    }

    private static Set<Tag> tags(String... tagNames) {
        return Stream.of(tagNames).map(Tag::new).collect(Collectors.toSet());
    }
}
