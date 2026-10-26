package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

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
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+65 123--4";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";
    private static final String INVALID_STAGE = "gave";
    private static final String INVALID_ORGANISATION = " ";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().get().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().get().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().get().toString();
    private static final String VALID_ORGANISATION = BENSON.getOrganisation().get().toString();
    private static final String VALID_STAGE = BENSON.getStage().toString();
    private static final List<JsonAdaptedInteraction> VALID_INTERACTIONS = List.of(
            new JsonAdaptedInteraction("2026-09-16", "Called"));
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_ORGANISATION, null, VALID_STAGE, VALID_INTERACTIONS, VALID_TAGS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_ORGANISATION, null, VALID_STAGE, VALID_INTERACTIONS, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_ORGANISATION, null, VALID_STAGE, VALID_INTERACTIONS, VALID_TAGS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_formattedPhone_preservesFormatting() throws Exception {
        String formattedPhone = "+65 9123-4567";
        Person person = new PersonBuilder(BENSON).withPhone(formattedPhone).build();

        assertEquals(person, new JsonAdaptedPerson(person).toModelType());
    }

    @Test
    public void toModelType_annotatedPhone_preservesFormatting() throws Exception {
        for (String phone : List.of("9123 4567 (HP)", "(65) 9123 4567", "9123 4567 ext 12")) {
            Person person = new PersonBuilder(BENSON).withPhone(phone).build();
            assertEquals(person, new JsonAdaptedPerson(person).toModelType());
        }
    }

    @Test
    public void toModelType_nullPhone_returnsPersonWithoutPhone() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_ADDRESS,
                        VALID_ORGANISATION, null, VALID_STAGE, VALID_INTERACTIONS, VALID_TAGS);
        assertTrue(person.toModelType().getPhone().isEmpty());
    }

    @Test
    public void toModelType_nullPhoneAndEmail_throwsIllegalValueException() {
        // a supporter must have at least one way to reach them
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, null, null, VALID_ADDRESS,
                        VALID_ORGANISATION, null, VALID_STAGE, VALID_INTERACTIONS, VALID_TAGS);
        assertThrows(IllegalValueException.class, Person.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_ADDRESS,
                        VALID_ORGANISATION, null, VALID_STAGE, VALID_INTERACTIONS, VALID_TAGS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_returnsPersonWithoutEmail() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_ADDRESS,
                        VALID_ORGANISATION, null, VALID_STAGE, VALID_INTERACTIONS, VALID_TAGS);
        assertTrue(person.toModelType().getEmail().isEmpty());
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, INVALID_ADDRESS,
                        VALID_ORGANISATION, null, VALID_STAGE, VALID_INTERACTIONS, VALID_TAGS);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullAddress_returnsPersonWithoutAddress() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, null,
                        VALID_ORGANISATION, null, VALID_STAGE, VALID_INTERACTIONS, VALID_TAGS);
        assertTrue(person.toModelType().getAddress().isEmpty());
    }

    @Test
    public void toModelType_personWithOnlyEmail_returnsSamePerson() throws Exception {
        Person emailOnly = new PersonBuilder(BENSON).withoutPhone().withoutAddress().build();
        assertEquals(emailOnly, new JsonAdaptedPerson(emailOnly).toModelType());
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_ORGANISATION, null, VALID_STAGE, VALID_INTERACTIONS, invalidTags);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

    @Test
    public void toModelType_nullTag_throwsIllegalValueException() {
        List<JsonAdaptedTag> tagsWithNull = new ArrayList<>(VALID_TAGS);
        tagsWithNull.add(null);
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_ORGANISATION, null, VALID_STAGE, VALID_INTERACTIONS, tagsWithNull);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Tag.class.getSimpleName());

        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_emptyTags_returnsPersonWithoutTags() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_ORGANISATION, null, VALID_STAGE, VALID_INTERACTIONS, List.of());

        assertTrue(person.toModelType().getTags().isEmpty());
    }

    @Test
    public void toModelType_nullTags_returnsPersonWithoutTags() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_ORGANISATION, null, VALID_STAGE, VALID_INTERACTIONS, null);

        assertTrue(person.toModelType().getTags().isEmpty());
    }

    @Test
    public void toModelType_invalidStage_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_ORGANISATION, null, INVALID_STAGE, VALID_INTERACTIONS, VALID_TAGS);
        String expectedMessage = Stage.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullStage_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_ORGANISATION, null, null, VALID_INTERACTIONS, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Stage.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_lowerCaseStage_returnsPersonWithStage() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_ORGANISATION, null, "giving", VALID_INTERACTIONS, VALID_TAGS);
        assertEquals(Stage.GIVING, person.toModelType().getStage());
    }

    @Test
    public void toModelType_nullOrganisation_returnsPersonWithoutOrganisation() throws Exception {
        // organisation is optional, so a data file without it still loads
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                null, null, VALID_STAGE, VALID_INTERACTIONS, VALID_TAGS);
        assertTrue(person.toModelType().getOrganisation().isEmpty());
    }

    @Test
    public void toModelType_invalidOrganisation_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                INVALID_ORGANISATION, null, VALID_STAGE, VALID_INTERACTIONS, VALID_TAGS);
        String expectedMessage = Organisation.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_personWithoutOrganisation_returnsSamePerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(ALICE);
        assertEquals(ALICE, person.toModelType());
    }

    @Test
    public void toModelType_personWithIntroducer_returnsSamePerson() throws Exception {
        Person introducedBenson = new PersonBuilder(BENSON).withIntroducer(ALICE.getName().fullName).build();
        assertEquals(introducedBenson, new JsonAdaptedPerson(introducedBenson).toModelType());
    }

    @Test
    public void toModelType_invalidIntroducer_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_ORGANISATION, INVALID_NAME, VALID_STAGE, VALID_INTERACTIONS, VALID_TAGS);
        assertThrows(IllegalValueException.class, Name.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_ownNameAsIntroducer_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_ORGANISATION, VALID_NAME.toLowerCase(), VALID_STAGE, VALID_INTERACTIONS, VALID_TAGS);
        assertThrows(IllegalValueException.class, Person.MESSAGE_SELF_INTRODUCTION, person::toModelType);
    }

    @Test
    public void toModelType_nullInteractions_returnsPersonWithEmptyHistory() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_ORGANISATION, null, VALID_STAGE, null, VALID_TAGS);
        assertTrue(person.toModelType().getInteractions().isEmpty());
    }

    @Test
    public void toModelType_validInteractions_returnsPersonWithInteractions() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_ORGANISATION, null, VALID_STAGE, VALID_INTERACTIONS, VALID_TAGS);
        assertEquals(new Interaction(LocalDate.of(2026, 9, 16), "Called"),
                person.toModelType().getInteractions().get(0));
    }

    @Test
    public void toModelType_nullInteraction_throwsIllegalValueException() {
        List<JsonAdaptedInteraction> interactionsWithNull = Collections.singletonList(null);
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_ORGANISATION, null, VALID_STAGE, interactionsWithNull, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Interaction.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

}
