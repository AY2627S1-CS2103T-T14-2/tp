package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Organisation;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Stage;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Supporter's %s field is missing!";

    private final String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String phone;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String email;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String address;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String organisation;
    private final String stage;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("organisation") String organisation, @JsonProperty("stage") String stage,
            @JsonProperty("tags") List<JsonAdaptedTag> tags) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.organisation = organisation;
        this.stage = stage;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().map(p -> p.value).orElse(null);
        email = source.getEmail().map(e -> e.value).orElse(null);
        address = source.getAddress().map(a -> a.value).orElse(null);
        organisation = source.getOrganisation().map(org -> org.value).orElse(null);
        stage = source.getStage().toString();
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        // phone, email and address are optional, so a missing value means the supporter does not have it
        if (phone != null && !Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Optional<Phone> modelPhone = Optional.ofNullable(phone).map(Phone::new);

        if (email != null && !Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Optional<Email> modelEmail = Optional.ofNullable(email).map(Email::new);

        if (!Person.isValidContact(modelPhone, modelEmail)) {
            throw new IllegalValueException(Person.MESSAGE_CONSTRAINTS);
        }

        if (address != null && !Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Optional<Address> modelAddress = Optional.ofNullable(address).map(Address::new);

        // organisation is optional, so a missing value means the supporter has no organisation
        if (organisation != null && !Organisation.isValidOrganisation(organisation)) {
            throw new IllegalValueException(Organisation.MESSAGE_CONSTRAINTS);
        }
        final Optional<Organisation> modelOrganisation = Optional.ofNullable(organisation).map(Organisation::new);

        if (stage == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Stage.class.getSimpleName()));
        }
        if (!Stage.isValidStage(stage)) {
            throw new IllegalValueException(Stage.MESSAGE_CONSTRAINTS);
        }
        final Stage modelStage = Stage.fromString(stage);

        final Set<Tag> modelTags = new HashSet<>(personTags);
        return new Person(modelName, modelPhone, modelEmail, modelAddress, modelOrganisation, modelStage,
                modelTags);
    }

}
