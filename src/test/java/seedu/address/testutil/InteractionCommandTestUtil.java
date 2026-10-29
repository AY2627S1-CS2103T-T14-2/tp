package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;

import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.Command;
import seedu.address.model.Model;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;

/**
 * Provides shared setup and failure assertions for interaction command tests.
 */
public final class InteractionCommandTestUtil {

    private InteractionCommandTestUtil() {
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
     * Verifies that failure leaves both model data and the viewed supporter unchanged.
     */
    public static void assertFailurePreservesSelection(Command command, Model model, String expectedMessage) {
        Optional<Person> previouslyViewed = model.getViewedPerson();
        assertCommandFailure(command, model, expectedMessage);
        assertEquals(previouslyViewed, model.getViewedPerson());
    }
}
