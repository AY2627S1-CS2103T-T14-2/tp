package seedu.address.testutil;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Provides shared setup for tests of commands that update one supporter and show them in the detail panel,
 * such as {@code tag}, {@code untag} and {@code intro}.
 */
public final class SupporterUpdateTestUtil {

    private SupporterUpdateTestUtil() {
    }

    /**
     * Returns the supporter at {@code index} in {@code model}'s displayed list.
     */
    public static Person getPersonAt(Model model, Index index) {
        return model.getFilteredPersonList().get(index.getZeroBased());
    }

    /**
     * Returns tags with the given names.
     */
    public static Set<Tag> createTags(String... tagNames) {
        return Stream.of(tagNames).map(Tag::new).collect(Collectors.toSet());
    }

    /**
     * Returns a copy of {@code model} in which {@code target} is replaced by {@code updated},
     * who is also the viewed supporter.
     */
    public static Model createExpectedModelAfterUpdate(Model model, Person target, Person updated) {
        Model expectedModel = copyOf(model);
        expectedModel.setPerson(target, updated);
        expectedModel.setViewedPerson(updated);
        return expectedModel;
    }

    /**
     * Returns a copy of {@code model} whose supporters are unchanged and whose viewed supporter is {@code viewed}.
     */
    public static Model createExpectedModelViewing(Model model, Person viewed) {
        Model expectedModel = copyOf(model);
        expectedModel.setViewedPerson(viewed);
        return expectedModel;
    }

    private static Model copyOf(Model model) {
        return new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
    }
}
