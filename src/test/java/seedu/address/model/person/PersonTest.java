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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void getInteractionIndicesInDisplayOrder_emptyHistory_returnsEmptyList() {
        assertTrue(new PersonBuilder().build().getInteractionIndicesInDisplayOrder().isEmpty());
    }

    @Test
    public void getInteractionIndicesInDisplayOrder_mixedDatesAndDuplicates_preservesStoredPositions() {
        Interaction earlier = new Interaction(LocalDate.of(2024, 2, 29), "Earlier meeting");
        Interaction later = new Interaction(LocalDate.of(2024, 3, 10), "Later meeting");
        Person person = new PersonBuilder().withInteractions(earlier, later, earlier).build();

        // equal entries remain distinct through their stored positions, with the later-logged entry first
        List<Integer> indices = person.getInteractionIndicesInDisplayOrder();
        assertEquals(List.of(1, 2, 0), indices);
        assertEquals(List.of(earlier, later, earlier), person.getInteractions());
        assertThrows(UnsupportedOperationException.class, () -> indices.add(3));
    }

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

        // name differs in case, all other attributes same -> returns true
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name has trailing spaces, all other attributes same -> returns true
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name has extra spaces between words, all other attributes same -> returns true
        editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.replace(" ", "   ")).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name differs by a letter -> returns false
        editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB + "e").build();
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

        // different interaction history -> returns false
        editedAlice = new PersonBuilder(ALICE)
                .withInteractions(new Interaction(LocalDate.of(2026, 9, 16), "Called"))
                .build();
        assertFalse(ALICE.equals(editedAlice));

        // introducer added -> returns false
        editedAlice = new PersonBuilder(ALICE).withIntroducer(BENSON.getName().fullName).build();
        assertFalse(ALICE.equals(editedAlice));

        // different introducer -> returns false
        Person introducedByBob = new PersonBuilder(editedAlice).withIntroducer(VALID_NAME_BOB).build();
        assertFalse(editedAlice.equals(introducedByBob));
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
    public void constructor_withoutInteractions_hasEmptyHistory() {
        Person person = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                ALICE.getOrganisation(), ALICE.getStage(), ALICE.getTags());
        assertTrue(person.getInteractions().isEmpty());
    }

    @Test
    public void constructor_withInteractions_defensivelyCopiesHistory() {
        Interaction interaction = new Interaction(LocalDate.of(2026, 9, 16), "Called");
        List<Interaction> interactions = new ArrayList<>(List.of(interaction));
        Person person = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                ALICE.getOrganisation(), Optional.empty(), ALICE.getStage(), interactions, ALICE.getTags());

        interactions.clear();
        assertEquals(List.of(interaction), person.getInteractions());
    }

    @Test
    public void getInteractions_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder(ALICE)
                .withInteractions(new Interaction(LocalDate.of(2026, 9, 16), "Called"))
                .build();
        assertThrows(UnsupportedOperationException.class, () -> person.getInteractions().clear());
    }

    @Test
    public void constructor_nullStage_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Person(ALICE.getName(), ALICE.getPhone().get(),
                ALICE.getEmail().get(), ALICE.getAddress().get(), null, ALICE.getTags()));
    }

    @Test
    public void constructor_withoutIntroducer_hasNoIntroducer() {
        Person person = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                ALICE.getOrganisation(), ALICE.getStage(), ALICE.getTags());
        assertTrue(person.getIntroducer().isEmpty());
    }

    @Test
    public void constructor_withIntroducer_hasIntroducer() {
        Person person = new PersonBuilder(ALICE).withIntroducer(BENSON.getName().fullName).build();
        assertEquals(Optional.of(BENSON.getName()), person.getIntroducer());
    }

    @Test
    public void constructor_nullIntroducer_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Person(ALICE.getName(), ALICE.getPhone(),
                ALICE.getEmail(), ALICE.getAddress(), ALICE.getOrganisation(), null, ALICE.getStage(),
                ALICE.getInteractions(), ALICE.getTags()));
    }

    @Test
    public void constructor_ownNameAsIntroducer_throwsIllegalArgumentException() {
        // names are compared ignoring case and extra spaces, as for duplicate supporters
        Name ownNameInOtherCase = new Name(ALICE.getName().fullName.toUpperCase());
        assertThrows(IllegalArgumentException.class, Person.MESSAGE_SELF_INTRODUCTION, () -> new Person(
                ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(), ALICE.getOrganisation(),
                Optional.of(ownNameInOtherCase), ALICE.getStage(), ALICE.getInteractions(), ALICE.getTags()));
    }

    @Test
    public void isIntroducedBy() {
        Person aliceByBenson = new PersonBuilder(ALICE).withIntroducer(BENSON.getName().fullName).build();

        // recorded introducer, in any case -> returns true
        assertTrue(aliceByBenson.isIntroducedBy(BENSON.getName()));
        assertTrue(aliceByBenson.isIntroducedBy(new Name(BENSON.getName().fullName.toLowerCase())));

        // someone else -> returns false
        assertFalse(aliceByBenson.isIntroducedBy(BOB.getName()));

        // no introducer -> returns false
        assertFalse(ALICE.isIntroducedBy(BENSON.getName()));
    }

    @Test
    public void withIntroducer_keepsOtherDetails() {
        Person aliceByBenson = ALICE.withIntroducer(Optional.of(BENSON.getName()));
        assertEquals(new PersonBuilder(ALICE).withIntroducer(BENSON.getName().fullName).build(), aliceByBenson);
        assertEquals(ALICE, aliceByBenson.withIntroducer(Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> ALICE.withIntroducer(Optional.of(ALICE.getName())));
    }

    @Test
    public void withTags_keepsOtherDetails() {
        Person aliceByBenson = new PersonBuilder(ALICE).withIntroducer(BENSON.getName().fullName)
                .withInteractions(new Interaction(LocalDate.of(2026, 9, 16), "Called")).build();
        Person retagged = aliceByBenson.withTags(Set.of(new Tag(VALID_TAG_HUSBAND)));
        assertEquals(new PersonBuilder(aliceByBenson).withTags(VALID_TAG_HUSBAND).build(), retagged);
        assertEquals(aliceByBenson.getInteractions(), retagged.getInteractions());
    }

    @Test
    public void isValidIntroducer() {
        // no introducer -> returns true
        assertTrue(Person.isValidIntroducer(ALICE.getName(), Optional.empty()));

        // another supporter -> returns true
        assertTrue(Person.isValidIntroducer(ALICE.getName(), Optional.of(BENSON.getName())));

        // own name, in any case -> returns false
        assertFalse(Person.isValidIntroducer(ALICE.getName(), Optional.of(ALICE.getName())));
        assertFalse(Person.isValidIntroducer(ALICE.getName(),
                Optional.of(new Name(ALICE.getName().fullName.toLowerCase()))));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName()
                + ", phone=" + ALICE.getPhone().orElse(null) + ", email=" + ALICE.getEmail().orElse(null)
                + ", address=" + ALICE.getAddress().orElse(null)
                + ", organisation=" + ALICE.getOrganisation().orElse(null)
                + ", introducer=" + ALICE.getIntroducer().orElse(null) + ", stage=" + ALICE.getStage()
                + ", interactions=" + ALICE.getInteractions() + ", tags=" + ALICE.getTags() + "}";
        assertEquals(expected, ALICE.toString());
    }
}
