package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ORGANISATION_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.model.person.Stage;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person editedPerson = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).withTags(VALID_TAG_HUSBAND).build();
        EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_editNonProspectSupporter_stagePreserved() {
        // second typical person is at stage Giving, which edit must not reset to the default
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        assertEquals(Stage.GIVING, secondPerson.getStage());

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        Person editedPerson = new PersonBuilder(secondPerson).withPhone(VALID_PHONE_BOB).build();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(secondPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
        assertEquals(Stage.GIVING, model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased()).getStage());
    }

    @Test
    public void execute_organisationSpecifiedUnfilteredList_success() {
        // first typical person has no organisation, so this adds one
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(firstPerson).withOrganisation(VALID_ORGANISATION_AMY).build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withOrganisation(VALID_ORGANISATION_AMY).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(firstPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_addPhoneToEmailOnlySupporter_success() throws Exception {
        // a supporter who gave only an email can have a phone number added later
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person emailOnly = new PersonBuilder(firstPerson).withoutPhone().withoutAddress().build();
        model.setPerson(firstPerson, emailOnly);

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);

        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(new PersonBuilder(emailOnly).withPhone(VALID_PHONE_BOB).build(), editedPerson);
    }

    @Test
    public void execute_editSupporterWithOrganisation_organisationPreserved() throws Exception {
        // second typical person has an organisation, which edit must carry over unchanged
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        assertTrue(secondPerson.getOrganisation().isPresent());

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();
        new EditCommand(INDEX_SECOND_PERSON, descriptor).execute(model);

        Person editedPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        assertEquals(secondPerson.getOrganisation(), editedPerson.getOrganisation());
    }

    @Test
    public void execute_editSupporterWithInteractions_interactionsPreserved() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personWithInteraction = new PersonBuilder(firstPerson)
                .withInteractions(new Interaction(LocalDate.of(2026, 9, 16), "Called"))
                .build();
        model.setPerson(firstPerson, personWithInteraction);

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);

        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(personWithInteraction.getInteractions(), editedPerson.getInteractions());
    }

    @Test
    public void execute_editSupporterWithIntroducer_introducerPreserved() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person introducedPerson = new PersonBuilder(firstPerson)
                .withIntroducer(secondPerson.getName().fullName)
                .build();
        model.setPerson(firstPerson, introducedPerson);

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);

        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(introducedPerson.getIntroducer(), editedPerson.getIntroducer());
    }

    @Test
    public void execute_renameIntroducer_introductionsFollowNewName() throws Exception {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person introducedPerson = new PersonBuilder(secondPerson)
                .withIntroducer(firstPerson.getName().fullName)
                .build();
        model.setPerson(secondPerson, introducedPerson);

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);

        Person updatedPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        assertEquals(VALID_NAME_BOB, updatedPerson.getIntroducer().get().fullName);
    }

    @Test
    public void execute_clearAddressAndOrganisation_success() {
        // second typical person has both an address and an organisation
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        assertTrue(secondPerson.getAddress().isPresent());
        assertTrue(secondPerson.getOrganisation().isPresent());

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withoutAddress()
                .withoutOrganisation().build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        Person editedPerson = new PersonBuilder(secondPerson).withoutAddress().withoutOrganisation().build();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(secondPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_clearAbsentField_success() {
        // clearing a field the supporter does not have leaves it empty
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person noOrganisation = new PersonBuilder(firstPerson).withoutOrganisation().build();
        model.setPerson(firstPerson, noOrganisation);

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withoutOrganisation().build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(noOrganisation));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_clearPhoneWithEmailKept_success() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertTrue(firstPerson.getEmail().isPresent());

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withoutPhone().build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        Person editedPerson = new PersonBuilder(firstPerson).withoutPhone().build();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(firstPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_clearEmailAndSetNewPhone_success() throws Exception {
        // an email-only supporter can swap their email for a phone number in one edit
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person emailOnly = new PersonBuilder(firstPerson).withoutPhone().build();
        model.setPerson(firstPerson, emailOnly);

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withoutEmail()
                .withPhone(VALID_PHONE_BOB).build();
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);

        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(new PersonBuilder(emailOnly).withoutEmail().withPhone(VALID_PHONE_BOB).build(), editedPerson);
    }

    @Test
    public void execute_clearOnlyContact_failure() {
        // an email-only supporter would be left with no way to reach them
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person emailOnly = new PersonBuilder(firstPerson).withoutPhone().build();
        model.setPerson(firstPerson, emailOnly);

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withoutEmail().build();
        assertCommandFailure(new EditCommand(INDEX_FIRST_PERSON, descriptor), model,
                EditCommand.MESSAGE_NO_CONTACT_LEFT);
    }

    @Test
    public void execute_clearPhoneAndEmail_failure() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withoutPhone().withoutEmail().build();
        assertCommandFailure(new EditCommand(INDEX_FIRST_PERSON, descriptor), model,
                EditCommand.MESSAGE_NO_CONTACT_LEFT);
    }

    @Test
    public void execute_renameToOwnIntroducer_failure() {
        // the new name clashes with the introducer, so this is a duplicate rather than a self-introduction
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person introducedPerson = new PersonBuilder(firstPerson)
                .withIntroducer(secondPerson.getName().fullName)
                .build();
        model.setPerson(firstPerson, introducedPerson);

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(secondPerson.getName().fullName.toLowerCase()).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(Messages.MESSAGE_DUPLICATE_PERSON, secondPerson.getName());
        assertCommandFailure(editCommand, model, expectedMessage);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        String expectedMessage = String.format(Messages.MESSAGE_DUPLICATE_PERSON, firstPerson.getName());
        assertCommandFailure(editCommand, model, expectedMessage);
    }

    @Test
    public void execute_nameDiffersOnlyInCaseAndSpaces_failure() {
        // "alice  pauline" counts as the same supporter as "Alice Pauline"
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        String sameNameDifferentCase = "  " + firstPerson.getName().fullName.toLowerCase().replace(" ", "  ");
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(sameNameDifferentCase).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        String expectedMessage = String.format(Messages.MESSAGE_DUPLICATE_PERSON, firstPerson.getName());
        assertCommandFailure(editCommand, model, expectedMessage);
    }

    @Test
    public void execute_changeCaseOfOwnName_success() {
        // changing only the capitalisation of a supporter's own name is not a duplicate
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        String newName = firstPerson.getName().fullName.toUpperCase();
        Person editedPerson = new PersonBuilder(firstPerson).withName(newName).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(newName).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(firstPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        String expectedMessage = String.format(Messages.MESSAGE_DUPLICATE_PERSON, personInList.getName());
        assertCommandFailure(editCommand, model, expectedMessage);
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

}
