package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    public static final String MESSAGE_CONSTRAINTS =
            "A supporter must have at least one way to reach them: a phone number, an email, or both.";
    public static final String MESSAGE_SELF_INTRODUCTION = "A supporter cannot be their own introducer.";

    // Identity fields
    private final Name name;
    private final Phone phone; // null if the supporter has no phone number
    private final Email email; // null if the supporter has no email

    // Data fields
    private final Address address; // null if the supporter has no address
    private final Organisation organisation; // null if the supporter has no organisation
    private final Name introducer; // null if no introducer is recorded for the supporter
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
        this(name, phone, email, address, organisation, Optional.empty(), stage, Collections.emptyList(), tags);
    }

    /**
     * Creates a {@code Person} with the given details, introducer and interaction history.
     * Every field must be present and not null. An empty optional field means the supporter does not have it,
     * but at least one of {@code phone} and {@code email} must be present.
     * {@code introducer} is the name of the supporter who introduced this one, and cannot be their own name.
     *
     * @throws IllegalArgumentException if both {@code phone} and {@code email} are empty,
     *         or if {@code introducer} is the supporter's own name.
     */
    public Person(Name name, Optional<Phone> phone, Optional<Email> email, Optional<Address> address,
            Optional<Organisation> organisation, Optional<Name> introducer, Stage stage,
            List<Interaction> interactions, Set<Tag> tags) {
        requireAllNonNull(name, phone, email, address, organisation, introducer, stage, tags);
        requireAllNonNull(interactions);
        checkArgument(isValidContact(phone, email), MESSAGE_CONSTRAINTS);
        checkArgument(isValidIntroducer(name, introducer), MESSAGE_SELF_INTRODUCTION);
        this.name = name;
        this.phone = phone.orElse(null);
        this.email = email.orElse(null);
        this.address = address.orElse(null);
        this.organisation = organisation.orElse(null);
        this.introducer = introducer.orElse(null);
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

    /**
     * Returns true if {@code introducer} is absent or names someone other than the supporter called {@code name}.
     * Names are compared ignoring case and extra spaces, as for duplicate supporters.
     */
    public static boolean isValidIntroducer(Name name, Optional<Name> introducer) {
        requireAllNonNull(name, introducer);
        return introducer.map(introducerName -> !introducerName.isSameName(name)).orElse(true);
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

    /**
     * Returns the name of the supporter who introduced this one, or an empty {@code Optional} if none is recorded.
     */
    public Optional<Name> getIntroducer() {
        return Optional.ofNullable(introducer);
    }

    /**
     * Returns true if the supporter called {@code name} is recorded as this supporter's introducer.
     * Names are compared ignoring case and extra spaces.
     */
    public boolean isIntroducedBy(Name name) {
        requireNonNull(name);
        return introducer != null && introducer.isSameName(name);
    }

    /**
     * Returns a copy of this supporter with {@code newIntroducer} as their introducer, or with none if it is empty.
     * Every other detail is unchanged.
     *
     * @throws IllegalArgumentException if {@code newIntroducer} is this supporter's own name.
     */
    public Person withIntroducer(Optional<Name> newIntroducer) {
        return new Person(name, getPhone(), getEmail(), getAddress(), getOrganisation(), newIntroducer, stage,
                interactions, tags);
    }

    /**
     * Returns a copy of this supporter with {@code newTags} as their tags. Every other detail is unchanged.
     */
    public Person withTags(Set<Tag> newTags) {
        return new Person(name, getPhone(), getEmail(), getAddress(), getOrganisation(), getIntroducer(), stage,
                interactions, newTags);
    }

    /**
     * Returns a copy of this supporter with {@code newInteractions} as their history.
     * Every other detail is unchanged. The supplied list is defensively copied in its given order.
     */
    public Person withInteractions(List<Interaction> newInteractions) {
        return new Person(name, getPhone(), getEmail(), getAddress(), getOrganisation(), getIntroducer(), stage,
                newInteractions, tags);
    }

    /**
     * Returns a copy of this supporter with {@code newStage} as their stage. Every other detail is unchanged.
     */
    public Person withStage(Stage newStage) {
        return new Person(name, getPhone(), getEmail(), getAddress(), getOrganisation(), getIntroducer(), newStage,
                interactions, tags);
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
     * Returns the number of interactions logged for this supporter.
     */
    public int getInteractionCount() {
        return interactions.size();
    }

    /**
     * Returns the latest interaction date, or an empty {@code Optional} if there are no interactions.
     * The result is independent of logging order and does not change the stored history.
     */
    public Optional<LocalDate> getLastInteractionDate() {
        return interactions.stream()
                .map(Interaction::getDate)
                .max(LocalDate::compareTo);
    }

    /**
     * Returns an immutable list of zero-based stored interaction indices in display order.
     * Interactions are ordered by date descending, then by logging position descending for equal dates.
     * Keeping indices rather than matching values distinguishes identical interactions.
     */
    public List<Integer> getInteractionIndicesInDisplayOrder() {
        return IntStream.range(0, interactions.size()).boxed()
                .sorted(Comparator.comparing((Integer index) -> interactions.get(index).getDate())
                        .thenComparingInt(Integer::intValue).reversed())
                .toList();
    }

    /**
     * Returns the zero-based stored position for a one-based chronological entry number.
     * Entry 1 is the oldest interaction; equal dates are numbered in logging order.
     *
     * @throws IndexOutOfBoundsException if {@code entryNumber} does not identify an existing entry.
     */
    public int getInteractionStoredIndex(int entryNumber) {
        Objects.checkIndex(entryNumber - 1, interactions.size());
        int storedIndex = getInteractionIndicesInDisplayOrder().get(interactions.size() - entryNumber);
        assert storedIndex >= 0 && storedIndex < interactions.size()
                : "chronological entry must map to an existing stored interaction";
        return storedIndex;
    }

    /**
     * Returns the one-based chronological entry number for a zero-based stored position.
     * The number reflects this supporter's current history, including any date edits.
     *
     * @throws IndexOutOfBoundsException if {@code storedIndex} does not identify an existing interaction.
     */
    public int getInteractionEntryNumber(int storedIndex) {
        Objects.checkIndex(storedIndex, interactions.size());
        int entryNumber = interactions.size() - getInteractionIndicesInDisplayOrder().indexOf(storedIndex);
        assert entryNumber >= 1 && entryNumber <= interactions.size()
                : "stored interaction must map to an existing chronological entry";
        return entryNumber;
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
                && Objects.equals(introducer, otherPerson.introducer)
                && stage.equals(otherPerson.stage)
                && interactions.equals(otherPerson.interactions)
                && tags.equals(otherPerson.tags);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, organisation, introducer, stage, interactions, tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("organisation", organisation)
                .add("introducer", introducer)
                .add("stage", stage)
                .add("interactions", interactions)
                .add("tags", tags)
                .toString();
    }

}
