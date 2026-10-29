package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORGANISATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.SharedContactWarning;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Organisation;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the supporter identified "
            + "by the index number used in the displayed supporter list. "
            + "Existing values will be overwritten by the input values. "
            + "At least one field must be provided.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_ORGANISATION + "ORGANISATION] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Leave " + PREFIX_PHONE + ", " + PREFIX_EMAIL + ", " + PREFIX_ADDRESS + " or " + PREFIX_ORGANISATION
            + " empty to clear that field. A phone or email can be cleared only if the other remains.\n"
            + "Example: " + COMMAND_WORD + " 2 "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_ADDRESS + "1 HarbourFront Avenue";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited supporter: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_NOT_CHANGED = "%1$s already has those details. Nothing was changed.";
    public static final String MESSAGE_NO_CONTACT_LEFT = "A supporter needs at least one way to reach them. "
            + "Keep their phone number or email, or give a new one, when clearing the other.";

    private final Index index;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());

        // checked before the edited supporter is created: renaming them to their introducer's name clashes
        // with the introducer, and would otherwise be rejected by Person as a self-introduction
        if (!personToEdit.getName().isSameName(updatedName)) {
            Optional<Person> namesake = model.getAddressBook().getPersonList().stream()
                    .filter(person -> person.getName().isSameName(updatedName))
                    .findFirst();
            if (namesake.isPresent()) {
                throw new CommandException(
                        String.format(Messages.MESSAGE_DUPLICATE_PERSON, namesake.get().getName()));
            }
        }

        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);
        List<Person> otherPersons = model.getAddressBook().getPersonList().stream()
                .filter(person -> !person.equals(personToEdit))
                .toList();
        String warning = new SharedContactWarning(otherPersons, editedPerson).getEditMessage();

        String feedback;
        if (editedPerson.equals(personToEdit)) {
            // say so, as no undo point is saved and a following undo reverts an earlier change
            feedback = String.format(MESSAGE_NOT_CHANGED, personToEdit.getName());
        } else {
            model.setPerson(personToEdit, editedPerson);
            if (!personToEdit.getName().equals(editedPerson.getName())) {
                // supporters introduced by this one keep pointing at them under the new name
                model.replaceIntroducer(personToEdit.getName(), Optional.of(editedPerson.getName()));
            }
            model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
            feedback = String.format(MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));
        }
        if (!warning.isEmpty()) {
            feedback += "\n" + warning;
        }
        return new CommandResult(feedback);
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     *
     * @throws CommandException if the edit would leave the supporter with neither a phone number nor an email.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor)
            throws CommandException {
        assert personToEdit != null;

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Optional<Phone> updatedPhone = updateField(editPersonDescriptor.getPhone(),
                editPersonDescriptor.isPhoneCleared(), personToEdit.getPhone());
        Optional<Email> updatedEmail = updateField(editPersonDescriptor.getEmail(),
                editPersonDescriptor.isEmailCleared(), personToEdit.getEmail());
        Optional<Address> updatedAddress = updateField(editPersonDescriptor.getAddress(),
                editPersonDescriptor.isAddressCleared(), personToEdit.getAddress());
        Optional<Organisation> updatedOrganisation = updateField(editPersonDescriptor.getOrganisation(),
                editPersonDescriptor.isOrganisationCleared(), personToEdit.getOrganisation());
        Set<Tag> updatedTags = editPersonDescriptor.getTags().orElse(personToEdit.getTags());

        if (!Person.isValidContact(updatedPhone, updatedEmail)) {
            throw new CommandException(MESSAGE_NO_CONTACT_LEFT);
        }

        // introducer, stage and interaction history are changed with their own commands,
        // so edit carries them over unchanged
        return new Person(updatedName, updatedPhone, updatedEmail, updatedAddress, updatedOrganisation,
                personToEdit.getIntroducer(), personToEdit.getStage(), personToEdit.getInteractions(),
                updatedTags);
    }

    /**
     * Returns the value an optional field has after the edit: empty if it is cleared,
     * otherwise {@code newValue}, or {@code currentValue} if the field is left out of the edit.
     */
    private static <T> Optional<T> updateField(Optional<T> newValue, boolean isCleared, Optional<T> currentValue) {
        return isCleared ? Optional.empty() : newValue.or(() -> currentValue);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand otherEditCommand)) {
            return false;
        }

        return index.equals(otherEditCommand.index)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person. The phone, email, address and organisation can
     * instead be cleared, which removes that field from the person.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Phone phone;
        private Email email;
        private Address address;
        private Organisation organisation;
        private Set<Tag> tags;
        private boolean isPhoneCleared;
        private boolean isEmailCleared;
        private boolean isAddressCleared;
        private boolean isOrganisationCleared;

        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            name = toCopy.name;
            phone = toCopy.phone;
            email = toCopy.email;
            address = toCopy.address;
            organisation = toCopy.organisation;
            setTags(toCopy.tags);
            isPhoneCleared = toCopy.isPhoneCleared;
            isEmailCleared = toCopy.isEmailCleared;
            isAddressCleared = toCopy.isAddressCleared;
            isOrganisationCleared = toCopy.isOrganisationCleared;
        }

        /**
         * Returns true if at least one field is edited or cleared.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, phone, email, address, organisation, tags)
                    || isPhoneCleared || isEmailCleared || isAddressCleared || isOrganisationCleared;
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
            isPhoneCleared = false;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        /**
         * Marks the phone to be removed from the person, replacing any new phone set earlier.
         */
        public void clearPhone() {
            phone = null;
            isPhoneCleared = true;
        }

        public boolean isPhoneCleared() {
            return isPhoneCleared;
        }

        public void setEmail(Email email) {
            this.email = email;
            isEmailCleared = false;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        /**
         * Marks the email to be removed from the person, replacing any new email set earlier.
         */
        public void clearEmail() {
            email = null;
            isEmailCleared = true;
        }

        public boolean isEmailCleared() {
            return isEmailCleared;
        }

        public void setAddress(Address address) {
            this.address = address;
            isAddressCleared = false;
        }

        public Optional<Address> getAddress() {
            return Optional.ofNullable(address);
        }

        /**
         * Marks the address to be removed from the person, replacing any new address set earlier.
         */
        public void clearAddress() {
            address = null;
            isAddressCleared = true;
        }

        public boolean isAddressCleared() {
            return isAddressCleared;
        }

        public void setOrganisation(Organisation organisation) {
            this.organisation = organisation;
            isOrganisationCleared = false;
        }

        public Optional<Organisation> getOrganisation() {
            return Optional.ofNullable(organisation);
        }

        /**
         * Marks the organisation to be removed from the person, replacing any new organisation set earlier.
         */
        public void clearOrganisation() {
            organisation = null;
            isOrganisationCleared = true;
        }

        public boolean isOrganisationCleared() {
            return isOrganisationCleared;
        }

        /**
         * Sets {@code tags} to this object's {@code tags}.
         * A defensive copy of {@code tags} is used internally.
         */
        public void setTags(Set<Tag> tags) {
            this.tags = (tags != null) ? new HashSet<>(tags) : null;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code tags} is null.
         */
        public Optional<Set<Tag>> getTags() {
            return (tags != null) ? Optional.of(Collections.unmodifiableSet(tags)) : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor otherEditPersonDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(email, otherEditPersonDescriptor.email)
                    && Objects.equals(address, otherEditPersonDescriptor.address)
                    && Objects.equals(organisation, otherEditPersonDescriptor.organisation)
                    && Objects.equals(tags, otherEditPersonDescriptor.tags)
                    && isPhoneCleared == otherEditPersonDescriptor.isPhoneCleared
                    && isEmailCleared == otherEditPersonDescriptor.isEmailCleared
                    && isAddressCleared == otherEditPersonDescriptor.isAddressCleared
                    && isOrganisationCleared == otherEditPersonDescriptor.isOrganisationCleared;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("phone", phone)
                    .add("email", email)
                    .add("address", address)
                    .add("organisation", organisation)
                    .add("tags", tags)
                    .add("isPhoneCleared", isPhoneCleared)
                    .add("isEmailCleared", isEmailCleared)
                    .add("isAddressCleared", isAddressCleared)
                    .add("isOrganisationCleared", isOrganisationCleared)
                    .toString();
        }
    }
}
