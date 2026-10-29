package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;

import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.Command;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;

/**
 * Provides shared setup and model assertions for interaction command tests.
 */
public final class InteractionCommandTestUtil {

    private InteractionCommandTestUtil() {
    }

    /**
     * Creates a model containing the given supporters in the supplied storage order.
     */
    public static Model createModelWithPersons(Person... persons) {
        Model model = new ModelManager();
        for (Person person : persons) {
            model.addPerson(person);
        }
        return model;
    }

    /**
     * Replaces the displayed supporter's history and returns the updated supporter.
     */
    public static Person setInteractions(Model model, Index personIndex, Interaction... interactions) {
        Person original = model.getFilteredPersonList().get(personIndex.getZeroBased());
        Person updated = new PersonBuilder(original).withInteractions(interactions).build();
        model.setPerson(original, updated);
        return updated;
    }

    /**
     * Verifies the selected supporter, single-person filter and complete stored supporter list.
     */
    public static void assertSelectedFilteredSupporter(Model model, Person expectedPerson,
            List<Person> expectedStoredPersons) {
        assertEquals(Optional.of(expectedPerson), model.getViewedPerson());
        assertEquals(List.of(expectedPerson), model.getFilteredPersonList());
        assertEquals(expectedStoredPersons, model.getAddressBook().getPersonList());
    }

    /**
     * Verifies that failure leaves both model data and the viewed supporter unchanged.
     */
    public static void assertFailurePreservesSelection(Command command, Model model, String expectedMessage) {
        Optional<Person> previouslyViewed = model.getViewedPerson();
        assertCommandFailure(command, model, expectedMessage);
        assertEquals(previouslyViewed, model.getViewedPerson());
    }
}
