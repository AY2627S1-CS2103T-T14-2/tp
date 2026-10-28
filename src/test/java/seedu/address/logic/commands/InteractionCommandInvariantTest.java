package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies that interaction commands detect broken history updates before changing the model.
 */
public class InteractionCommandInvariantTest {

    private static final LocalDate DATE = LocalDate.of(2024, 2, 29);

    @Test
    public void execute_logDropsHistory_throwsAssertionErrorWithoutUpdatingModel() {
        Command command = new LogCommand(INDEX_FIRST_PERSON, "New meeting", Optional.of(DATE));

        assertHistoryInvariantFailure(command, "logging must add exactly one interaction");
    }

    @Test
    public void execute_editDropsHistory_throwsAssertionErrorWithoutUpdatingModel() {
        Command command = new EditLogCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON,
                Optional.of("Updated meeting"), Optional.empty());

        assertHistoryInvariantFailure(command, "editing must preserve the number of interactions");
    }

    @Test
    public void execute_unlogDropsHistory_throwsAssertionErrorWithoutUpdatingModel() {
        Command command = new UnlogCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON);

        assertHistoryInvariantFailure(command, "removing must delete exactly one interaction");
    }

    private void assertHistoryInvariantFailure(Command command, String expectedMessage) {
        Person person = new PersonDroppingHistoryStub(new PersonBuilder()
                .withInteractions(new Interaction(DATE, "First meeting"), new Interaction(DATE, "Second meeting"))
                .build());
        Model model = new ModelManager();
        model.addPerson(person);
        model.setViewedPerson(person);

        assertThrows(AssertionError.class, expectedMessage, () -> command.execute(model));
        assertEquals(List.of(person), model.getFilteredPersonList());
        assertEquals(Optional.of(person), model.getViewedPerson());
    }

    /**
     * Simulates an incorrect history update that discards every entry.
     */
    private static class PersonDroppingHistoryStub extends Person {

        PersonDroppingHistoryStub(Person person) {
            super(person.getName(), person.getPhone(), person.getEmail(), person.getAddress(),
                    person.getOrganisation(), person.getIntroducer(), person.getStage(),
                    person.getInteractions(), person.getTags());
        }

        @Override
        public Person withInteractions(List<Interaction> newInteractions) {
            return super.withInteractions(List.of());
        }
    }
}
