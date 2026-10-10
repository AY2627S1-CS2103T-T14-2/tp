package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    public static final String MESSAGE_CONSTRAINTS =
            "A supporter must have at least one way to reach them: a phone number, an email, or both.";

    // Identity fields
    private final Name name;
    private final Phone phone; // null if the supporter has no phone number
    private final Email email; // null if the supporter has no email

    // Data fields
    private final Address address; // null if the supporter has no address
    private final Organisation organisation; // null if the supporter has no organisation
    private final Stage stage;
    private final List<Interaction> interactions = new ArrayList<>();
    private final Set<Tag> tags = new HashSet<>();

    /**
     * Creates a {@code Person} with no organisation, at the default stage, {@link Stage#DEFAULT_STAGE}.
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(name, phone, email, address, Stage.DEFAULT_STAGE, tags);
    }

    /**
     * Creates a {@code Person} with a phone number, an email and an address, but no organisation.
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Stage stage, Set<Tag> tags) {
        this(name, Optional.of(phone), Optional.of(email), Optional.of(address), Optional.empty(), stage, tags);
    }

    /**
     * Creates a {@code Person} with the given details.
     * Every field must be present and not null. An empty optional field means the supporter does not have it,
     * but at least one of {@code phone} and {@code email} must be present.
     *
     * @throws IllegalArgumentException if both {@code phone} and {@code email} are empty.
     */
    public Person(Name name, Optional<Phone> phone, Optional<Email> email, Optional<Address> address,
            Optional<Organisation> organisation, Stage stage, Set<Tag> tags) {
        this(name, phone, email, address, organisation, stage, Collections.emptyList(), tags);
    }

    /**
     * Creates a {@code Person} with the given details and interaction history.
     * Every field must be present and not null. An empty optional field means the supporter does not have it,
     * but at least one of {@code phone} and {@code email} must be present.
     *
     * @throws IllegalArgumentException if both {@code phone} and {@code email} are empty.
     */
    public Person(Name name, Optional<Phone> phone, Optional<Email> email, Optional<Address> address,
            Optional<Organisation> organisation, Stage stage, List<Interaction> interactions, Set<Tag> tags) {
        requireAllNonNull(name, phone, email, address, organisation, stage, tags);
        requireAllNonNull(interactions);
        checkArgument(isValidContact(phone, email), MESSAGE_CONSTRAINTS);
        this.name = name;
        this.phone = phone.orElse(null);
        this.email = email.orElse(null);
        this.address = address.orElse(null);
        this.organisation = organisation.orElse(null);
        this.stage = stage;
        this.interactions.addAll(interactions);
        this.tags.addAll(tags);
    }

    /**
     * Returns true if there is a way to reach the supporter, i.e. at least one of {@code phone} and {@code email}.
     */
    public static boolean isValidContact(Optional<Phone> phone, Optional<Email> email) {
        return phone.isPresent() || email.isPresent();
    }

    public Name getName() {
        return name;
    }

    /**
     * Returns the supporter's phone number, or an empty {@code Optional} if they have none.
     */
    public Optional<Phone> getPhone() {
        return Optional.ofNullable(phone);
    }

    /**
     * Returns the supporter's email, or an empty {@code Optional} if they have none.
     */
    public Optional<Email> getEmail() {
        return Optional.ofNullable(email);
    }

    /**
     * Returns the supporter's address, or an empty {@code Optional} if they have none.
     */
    public Optional<Address> getAddress() {
        return Optional.ofNullable(address);
    }

    /**
     * Returns the supporter's organisation, or an empty {@code Optional} if they have none.
     */
    public Optional<Organisation> getOrganisation() {
        return Optional.ofNullable(organisation);
    }

    public Stage getStage() {
        return stage;
    }

    /**
     * Returns the supporter's interactions in the order they were logged.
     */
    public List<Interaction> getInteractions() {
        return Collections.unmodifiableList(interactions);
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns true if both persons have the same name, ignoring case and extra spaces.
     * This defines a weaker notion of equality between two persons, used to detect duplicate supporters.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().isSameName(getName());
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && Objects.equals(phone, otherPerson.phone)
                && Objects.equals(email, otherPerson.email)
                && Objects.equals(address, otherPerson.address)
                && Objects.equals(organisation, otherPerson.organisation)
                && stage.equals(otherPerson.stage)
                && interactions.equals(otherPerson.interactions)
                && tags.equals(otherPerson.tags);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, organisation, stage, interactions, tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("organisation", organisation)
                .add("stage", stage)
                .add("interactions", interactions)
                .add("tags", tags)
                .toString();
    }

}
