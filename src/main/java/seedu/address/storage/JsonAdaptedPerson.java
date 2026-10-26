package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Interaction;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String introducer;
    private final String stage;
    private final List<JsonAdaptedInteraction> interactions = new ArrayList<>();
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("organisation") String organisation, @JsonProperty("introducer") String introducer,
            @JsonProperty("stage") String stage,
            @JsonProperty("interactions") List<JsonAdaptedInteraction> interactions,
            @JsonProperty("tags") List<JsonAdaptedTag> tags) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.organisation = organisation;
        this.introducer = introducer;
        this.stage = stage;
        if (interactions != null) {
            this.interactions.addAll(interactions);
        }
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
        introducer = source.getIntroducer().map(introducerName -> introducerName.fullName).orElse(null);
        stage = source.getStage().toString();
        interactions.addAll(source.getInteractions().stream()
                .map(JsonAdaptedInteraction::new)
                .collect(Collectors.toList()));
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
        final List<Interaction> modelInteractions = toModelInteractions();
        final Set<Tag> modelTags = toModelTags();

        final Name modelName = toRequiredField(name, Name.class, Name::isValidName, Name.MESSAGE_CONSTRAINTS,
                Name::new);
        // the other contact details are optional, so a missing value means the supporter does not have it
        final Optional<Phone> modelPhone = toOptionalField(phone, Phone::isValidPhone,
                Phone.MESSAGE_CONSTRAINTS, Phone::new);
        final Optional<Email> modelEmail = toOptionalField(email, Email::isValidEmail,
                Email.MESSAGE_CONSTRAINTS, Email::new);
        if (!Person.isValidContact(modelPhone, modelEmail)) {
            throw new IllegalValueException(Person.MESSAGE_CONSTRAINTS);
        }
        final Optional<Address> modelAddress = toOptionalField(address, Address::isValidAddress,
                Address.MESSAGE_CONSTRAINTS, Address::new);
        final Optional<Organisation> modelOrganisation = toOptionalField(organisation,
                Organisation::isValidOrganisation, Organisation.MESSAGE_CONSTRAINTS, Organisation::new);

        // whether the introducer names an existing supporter is checked for the whole address book
        final Optional<Name> modelIntroducer = toOptionalField(introducer, Name::isValidName,
                Name.MESSAGE_CONSTRAINTS, Name::new);
        if (!Person.isValidIntroducer(modelName, modelIntroducer)) {
            throw new IllegalValueException(Person.MESSAGE_SELF_INTRODUCTION);
        }

        final Stage modelStage = toRequiredField(stage, Stage.class, Stage::isValidStage,
                Stage.MESSAGE_CONSTRAINTS, Stage::fromString);
        return new Person(modelName, modelPhone, modelEmail, modelAddress, modelOrganisation, modelIntroducer,
                modelStage, modelInteractions, modelTags);
    }

    private List<Interaction> toModelInteractions() throws IllegalValueException {
        final List<Interaction> modelInteractions = new ArrayList<>();
        for (JsonAdaptedInteraction interaction : interactions) {
            if (interaction == null) {
                throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                        Interaction.class.getSimpleName()));
            }
            modelInteractions.add(interaction.toModelType());
        }
        return modelInteractions;
    }

    private Set<Tag> toModelTags() throws IllegalValueException {
        final Set<Tag> modelTags = new HashSet<>();
        for (JsonAdaptedTag tag : tags) {
            modelTags.add(tag.toModelType());
        }
        return modelTags;
    }

    /**
     * Converts the value of a field every supporter must have, e.g. their name.
     *
     * @throws IllegalValueException if {@code value} is missing or fails {@code isValid}.
     */
    private static <T> T toRequiredField(String value, Class<T> fieldClass, Predicate<String> isValid,
            String constraintsMessage, Function<String, T> converter) throws IllegalValueException {
        if (value == null) {
            throw new IllegalValueException(
                    String.format(MISSING_FIELD_MESSAGE_FORMAT, fieldClass.getSimpleName()));
        }
        return toOptionalField(value, isValid, constraintsMessage, converter).get();
    }

    /**
     * Converts the value of an optional field, or returns an empty {@code Optional} if it is missing.
     *
     * @throws IllegalValueException if {@code value} is present but fails {@code isValid}.
     */
    private static <T> Optional<T> toOptionalField(String value, Predicate<String> isValid,
            String constraintsMessage, Function<String, T> converter) throws IllegalValueException {
        if (value == null) {
            return Optional.empty();
        }
        if (!isValid.test(value)) {
            throw new IllegalValueException(constraintsMessage);
        }
        return Optional.of(converter.apply(value));
    }

}
