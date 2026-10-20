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
 * Contains integration tests (interaction with the Model) and unit tests for {@code TagCommand}.
 */
public class TagCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TagCommand(null, tags("vip")));
        assertThrows(NullPointerException.class, () -> new TagCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_newTags_addsTagsAndKeepsExisting() {
        // first typical person is tagged "friends"
        Person target = personAt(INDEX_FIRST_PERSON);
        Person expectedPerson = new PersonBuilder(target).withTags("friends", "vip", "donor").build();

        String expectedMessage = String.format(TagCommand.MESSAGE_TAGS_ADDED, target.getName(), "[donor] [vip]");
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, expectedPerson);
        expectedModel.setViewedPerson(expectedPerson);

        assertCommandSuccess(new TagCommand(INDEX_FIRST_PERSON, tags("vip", "donor")), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_someTagsAlreadyPresent_skipsThem() {
        // "FRIENDS" matches the existing "friends" tag, which keeps its original case
        Person target = personAt(INDEX_FIRST_PERSON);
        Person expectedPerson = new PersonBuilder(target).withTags("friends", "vip").build();

        String expectedMessage = String.format(TagCommand.MESSAGE_TAGS_ADDED, target.getName(), "[vip]") + "\n"
                + String.format(TagCommand.MESSAGE_TAGS_SKIPPED, "[FRIENDS]");
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, expectedPerson);
        expectedModel.setViewedPerson(expectedPerson);

        assertCommandSuccess(new TagCommand(INDEX_FIRST_PERSON, tags("FRIENDS", "vip")), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_allTagsPresent_successWithoutChange() {
        // second typical person is tagged "owesMoney" and "friends"
        Person target = personAt(INDEX_SECOND_PERSON);

        String expectedMessage = String.format(TagCommand.MESSAGE_NOTHING_ADDED, target.getName(),
                "[friends] [owesmoney]");
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setViewedPerson(target);

        assertCommandSuccess(new TagCommand(INDEX_SECOND_PERSON, tags("friends", "owesmoney")), model,
                expectedMessage, expectedModel);
    }

    @Test
    public void execute_sameTagInTwoCases_addsOneAndSkipsOther() {
        // third typical person has no tags
        Person target = personAt(INDEX_THIRD_PERSON);
        Person expectedPerson = new PersonBuilder(target).withTags("VIP").build();

        String expectedMessage = String.format(TagCommand.MESSAGE_TAGS_ADDED, target.getName(), "[VIP]") + "\n"
                + String.format(TagCommand.MESSAGE_TAGS_SKIPPED, "[vip]");
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(target, expectedPerson);
        expectedModel.setViewedPerson(expectedPerson);

        assertCommandSuccess(new TagCommand(INDEX_THIRD_PERSON, tags("vip", "VIP")), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_filteredList_successAndFilterKept() throws Exception {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        new TagCommand(INDEX_FIRST_PERSON, tags("vip")).execute(model);

        assertEquals(1, model.getFilteredPersonList().size());
        Person updatedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertTrue(updatedPerson.getTags().contains(new Tag("vip")));
        assertEquals(Optional.of(updatedPerson), model.getViewedPerson());
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new TagCommand(outOfBoundIndex, tags("vip")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new TagCommand(INDEX_SECOND_PERSON, tags("vip")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        TagCommand tagFirst = new TagCommand(INDEX_FIRST_PERSON, tags("vip"));

        // same object -> returns true
        assertTrue(tagFirst.equals(tagFirst));

        // same values -> returns true
        assertTrue(tagFirst.equals(new TagCommand(INDEX_FIRST_PERSON, tags("vip"))));

        // different types -> returns false
        assertFalse(tagFirst.equals(1));

        // null -> returns false
        assertFalse(tagFirst.equals(null));

        // different index -> returns false
        assertFalse(tagFirst.equals(new TagCommand(INDEX_SECOND_PERSON, tags("vip"))));

        // different tags -> returns false
        assertFalse(tagFirst.equals(new TagCommand(INDEX_FIRST_PERSON, tags("donor"))));

        // same tags but untag -> returns false
        assertFalse(tagFirst.equals(new UntagCommand(INDEX_FIRST_PERSON, tags("vip"))));
    }

    @Test
    public void toStringMethod() {
        Set<Tag> tagsToAdd = tags("vip");
        TagCommand tagCommand = new TagCommand(INDEX_FIRST_PERSON, tagsToAdd);
        String expected = TagCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", tagsToAdd=" + tagsToAdd + "}";
        assertEquals(expected, tagCommand.toString());
    }

    private Person personAt(Index index) {
        return model.getFilteredPersonList().get(index.getZeroBased());
    }

    private static Set<Tag> tags(String... tagNames) {
        return Stream.of(tagNames).map(Tag::new).collect(Collectors.toSet());
    }
}
