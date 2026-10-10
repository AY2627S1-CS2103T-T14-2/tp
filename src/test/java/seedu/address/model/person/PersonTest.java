package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ORGANISATION_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_STAGE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, all other attributes same -> returns false
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertFalse(BOB.isSamePerson(editedBob));

        // name has trailing spaces, all other attributes same -> returns false
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertFalse(BOB.isSamePerson(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));

        // different stage -> returns false
        editedAlice = new PersonBuilder(ALICE).withStage(VALID_STAGE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // phone removed -> returns false
        editedAlice = new PersonBuilder(ALICE).withoutPhone().build();
        assertFalse(ALICE.equals(editedAlice));

        // address removed -> returns false
        editedAlice = new PersonBuilder(ALICE).withoutAddress().build();
        assertFalse(ALICE.equals(editedAlice));

        // organisation added -> returns false
        editedAlice = new PersonBuilder(ALICE).withOrganisation(VALID_ORGANISATION_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different organisation -> returns false
        Person editedBenson = new PersonBuilder(BENSON).withOrganisation("OCBC Bank").build();
        assertFalse(BENSON.equals(editedBenson));
    }

    @Test
    public void hashCode_equalPersons_sameHashCode() {
        Person bensonCopy = new PersonBuilder(BENSON).build();
        assertEquals(BENSON.hashCode(), bensonCopy.hashCode());
    }

    @Test
    public void constructor_withoutOrganisation_hasNoOrganisation() {
        Person person = new Person(ALICE.getName(), ALICE.getPhone().get(), ALICE.getEmail().get(),
                ALICE.getAddress().get(), ALICE.getStage(), ALICE.getTags());
        assertTrue(person.getOrganisation().isEmpty());
    }

    @Test
    public void constructor_nullOrganisation_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Person(ALICE.getName(), ALICE.getPhone(),
                ALICE.getEmail(), ALICE.getAddress(), null, ALICE.getStage(), ALICE.getTags()));
    }

    @Test
    public void constructor_neitherPhoneNorEmail_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Person.MESSAGE_CONSTRAINTS, () -> new Person(ALICE.getName(),
                Optional.empty(), Optional.empty(), ALICE.getAddress(), Optional.empty(), ALICE.getStage(),
                ALICE.getTags()));
    }

    @Test
    public void constructor_onlyPhoneOrOnlyEmail_success() {
        Person phoneOnly = new PersonBuilder(ALICE).withoutEmail().withoutAddress().build();
        assertTrue(phoneOnly.getPhone().isPresent());
        assertTrue(phoneOnly.getEmail().isEmpty());
        assertTrue(phoneOnly.getAddress().isEmpty());

        Person emailOnly = new PersonBuilder(ALICE).withoutPhone().build();
        assertTrue(emailOnly.getPhone().isEmpty());
        assertTrue(emailOnly.getEmail().isPresent());
    }

    @Test
    public void constructor_withoutStage_defaultsToProspect() {
        Person person = new Person(ALICE.getName(), ALICE.getPhone().get(), ALICE.getEmail().get(),
                ALICE.getAddress().get(), ALICE.getTags());
        assertEquals(Stage.PROSPECT, person.getStage());
    }

    @Test
    public void constructor_nullStage_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Person(ALICE.getName(), ALICE.getPhone().get(),
                ALICE.getEmail().get(), ALICE.getAddress().get(), null, ALICE.getTags()));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName()
                + ", phone=" + ALICE.getPhone().orElse(null) + ", email=" + ALICE.getEmail().orElse(null)
                + ", address=" + ALICE.getAddress().orElse(null)
                + ", organisation=" + ALICE.getOrganisation().orElse(null) + ", stage=" + ALICE.getStage()
                + ", tags=" + ALICE.getTags() + "}";
        assertEquals(expected, ALICE.toString());
    }
}
