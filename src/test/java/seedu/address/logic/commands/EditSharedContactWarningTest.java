package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

public class EditSharedContactWarningTest {
    private final Person alice = new PersonBuilder().withName("Alice").withPhone("+(65) 9123-4567 (HP)")
            .withEmail("alice@example.com").build();
    private final Person bob = new PersonBuilder().withName("Bob").withPhone("123")
            .withEmail("bob@example.com").build();

    @Test
    public void execute_matchingPhone_warnsUsingFullListDespiteFilter() throws Exception {
        Model model = createModel();
        model.updateFilteredPersonList(person -> person.equals(bob));
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone("6591234567").build();

        CommandResult result = new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);

        assertTrue(result.getFeedbackToUser().contains("Alice (phone)"));
        assertTrue(result.getFeedbackToUser().contains("The supporter was updated"));
        assertFalse(result.getFeedbackToUser().contains("The supporter was added"));
        assertEquals(alice, model.getAddressBook().getPersonList().get(0));
    }

    @Test
    public void execute_matchingBothContacts_namesOtherSupporterOnce() throws Exception {
        Model model = createModel();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone("6591234567")
                .withEmail("ALICE@EXAMPLE.COM").build();

        CommandResult result = new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model);

        assertTrue(result.getFeedbackToUser().contains("Alice (phone and email)"));
        assertEquals(1, result.getFeedbackToUser().lines().filter(line -> line.startsWith("Warning:")).count());
    }

    @Test
    public void execute_renamedSupporterWithOwnContacts_doesNotWarnAboutItself() throws Exception {
        Model model = createModel();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName("Renamed Bob").build();
        Person expected = new PersonBuilder(bob).withName("Renamed Bob").build();

        CommandResult result = new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model);

        assertEquals(String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(expected)),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_noMatchingContacts_preservesNormalSuccessFeedback() throws Exception {
        Model model = createModel();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone("456").build();
        Person expected = new PersonBuilder(bob).withPhone("456").build();

        CommandResult result = new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model);

        assertEquals(String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(expected)),
                result.getFeedbackToUser());
    }

    private Model createModel() {
        Model model = new ModelManager();
        model.addPerson(alice);
        model.addPerson(bob);
        return model;
    }
}
