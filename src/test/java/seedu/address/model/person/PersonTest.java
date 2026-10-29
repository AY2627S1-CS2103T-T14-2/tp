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
    public void interactionNumberConversions_mixedDatesAndDuplicates_preserveStoredPositions() {
        Interaction earlier = new Interaction(LocalDate.of(2024, 2, 29), "Earlier meeting");
        Interaction later = new Interaction(LocalDate.of(2024, 3, 10), "Later meeting");
        Person person = new PersonBuilder().withInteractions(earlier, later, earlier).build();

        assertEquals(0, person.getInteractionStoredIndex(1));
        assertEquals(2, person.getInteractionStoredIndex(2));
        assertEquals(1, person.getInteractionStoredIndex(3));
        assertEquals(1, person.getInteractionEntryNumber(0));
        assertEquals(3, person.getInteractionEntryNumber(1));
        assertEquals(2, person.getInteractionEntryNumber(2));
        assertEquals(List.of(earlier, later, earlier), person.getInteractions());
    }

    @Test
    public void interactionNumberConversions_singleEntry_returnsOnlyPosition() {
        Person person = new PersonBuilder()
                .withInteractions(new Interaction(LocalDate.of(2024, 2, 29), "Called")).build();

        assertEquals(0, person.getInteractionStoredIndex(1));
        assertEquals(1, person.getInteractionEntryNumber(0));
    }

    @Test
    public void interactionNumberConversions_invalidPositions_throwsIndexOutOfBoundsException() {
        Person empty = new PersonBuilder().build();
        Person person = new PersonBuilder()
                .withInteractions(new Interaction(LocalDate.of(2024, 2, 29), "Called")).build();

        assertThrows(IndexOutOfBoundsException.class, () -> empty.getInteractionStoredIndex(1));
        assertThrows(IndexOutOfBoundsException.class, () -> empty.getInteractionEntryNumber(0));
        assertThrows(IndexOutOfBoundsException.class, () -> person.getInteractionStoredIndex(0));
        assertThrows(IndexOutOfBoundsException.class, () -> person.getInteractionStoredIndex(2));
        assertThrows(IndexOutOfBoundsException.class, () -> person.getInteractionEntryNumber(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> person.getInteractionEntryNumber(1));
    }

    @Test
    public void getInteractionStoredIndex_invalidMappedPosition_throwsAssertionError() {
        for (int invalidStoredIndex : List.of(-1, 1)) {
            Person person = new PersonWithDisplayOrderStub(List.of(invalidStoredIndex));

            assertThrows(AssertionError.class, () -> person.getInteractionStoredIndex(1));
        }
    }

    @Test
    public void getInteractionEntryNumber_missingStoredPosition_throwsAssertionError() {
        Person person = new PersonWithDisplayOrderStub(List.of());

        assertThrows(AssertionError.class, () -> person.getInteractionEntryNumber(0));
    }

    @Test
    public void getInteractionEntryNumber_outOfRangeDisplayPosition_throwsAssertionError() {
        Person person = new PersonWithDisplayOrderStub(List.of(1, 0));

        assertThrows(AssertionError.class, () -> person.getInteractionEntryNumber(0));
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
    public void isSamePerson_canonicallyEquivalentNames_returnsTrue() {
        Person composedPerson = new PersonBuilder(ALICE).withName("José Tan").build();
        Person decomposedPerson = new PersonBuilder(BOB).withName("Jose\u0301 Tan").build();
        Person decomposedPersonWithDifferentCaseAndSpaces = new PersonBuilder(BOB)
                .withName("  JOSE\u0301  TAN  ").build();
        Person personWithoutAccent = new PersonBuilder(ALICE).withName("Jose Tan").build();

        assertTrue(composedPerson.isSamePerson(decomposedPerson));
        assertTrue(decomposedPerson.isSamePerson(composedPerson));
        assertTrue(composedPerson.isSamePerson(decomposedPersonWithDifferentCaseAndSpaces));
        assertFalse(composedPerson.isSamePerson(personWithoutAccent));
    }

    @Test
    public void isSamePerson_differentApostropheStylesAndContactDetails_returnsTrue() {
        Person straightPerson = new PersonBuilder(ALICE).withName("Charlotte O'Brien").build();
        Person curlyPerson = new PersonBuilder(BOB).withName("Charlotte O’Brien").build();

        assertTrue(straightPerson.isSamePerson(curlyPerson));
        assertTrue(curlyPerson.isSamePerson(straightPerson));
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
    public void getLastInteractionDate_emptyHistory_returnsEmpty() {
        assertLastInteractionDateAndHistory(Optional.empty());
    }

    @Test
    public void getLastInteractionDate_singleInteraction_returnsDate() {
        LocalDate date = LocalDate.of(2026, 10, 3);
        assertLastInteractionDateAndHistory(Optional.of(date), new Interaction(date, "Called"));
    }

    @Test
    public void getLastInteractionDate_outOfOrderDates_returnsLatestDateAndPreservesHistory() {
        Interaction firstInteraction = new Interaction(LocalDate.of(2026, 9, 30), "Met");
        Interaction latestInteraction = new Interaction(LocalDate.of(2027, 1, 1), "Called");
        Interaction lastLoggedInteraction = new Interaction(LocalDate.of(2026, 10, 3), "Emailed");
        assertLastInteractionDateAndHistory(Optional.of(LocalDate.of(2027, 1, 1)),
                firstInteraction, latestInteraction, lastLoggedInteraction);
    }

    @Test
    public void getLastInteractionDate_duplicateLatestDates_returnsLatestDate() {
        LocalDate latestDate = LocalDate.of(2026, 10, 3);
        assertLastInteractionDateAndHistory(Optional.of(latestDate),
                new Interaction(latestDate, "Called"),
                new Interaction(latestDate, "Emailed"),
                new Interaction(LocalDate.of(2026, 9, 30), "Met"));
    }

    @Test
    public void getLastInteractionDate_leapDay_returnsLeapDayAndPreservesHistory() {
        LocalDate leapDay = LocalDate.of(2024, 2, 29);
        assertLastInteractionDateAndHistory(Optional.of(leapDay),
                new Interaction(leapDay, "Met on leap day"),
                new Interaction(LocalDate.of(2024, 2, 28), "Called before leap day"));
    }

    @Test
    public void getLastInteractionDate_acrossLeapDay_returnsMarchDateAndPreservesHistory() {
        LocalDate marchDate = LocalDate.of(2024, 3, 1);
        assertLastInteractionDateAndHistory(Optional.of(marchDate),
                new Interaction(LocalDate.of(2024, 2, 28), "Called before leap day"),
                new Interaction(marchDate, "Met after leap day"),
                new Interaction(LocalDate.of(2024, 2, 29), "Emailed on leap day"));
    }

    @Test
    public void getLastInteractionDate_yearBoundary_returnsJanuaryDateAndPreservesHistory() {
        LocalDate januaryDate = LocalDate.of(2024, 1, 1);
        assertLastInteractionDateAndHistory(Optional.of(januaryDate),
                new Interaction(LocalDate.of(2023, 12, 31), "Called at year end"),
                new Interaction(januaryDate, "Met in the new year"));
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
    public void withInteractions_keepsOtherDetailsAndOriginalHistory() {
        Interaction original = new Interaction(LocalDate.of(2024, 2, 20), "Called");
        Interaction replacement = new Interaction(LocalDate.of(2024, 3, 10), "Met");
        Person person = new PersonBuilder(ALICE).withIntroducer(BENSON.getName().fullName)
                .withInteractions(original).build();

        Person updated = person.withInteractions(List.of(replacement));

        assertEquals(new PersonBuilder(person).withInteractions(replacement).build(), updated);
        assertEquals(List.of(original), person.getInteractions());
        assertEquals(new PersonBuilder(person).withInteractions().build(), person.withInteractions(List.of()));
    }

    @Test
    public void withStage_keepsOtherDetails() {
        Person aliceByBenson = new PersonBuilder(ALICE).withIntroducer(BENSON.getName().fullName)
                .withInteractions(new Interaction(LocalDate.of(2026, 9, 16), "Called")).build();

        Person updated = aliceByBenson.withStage(Stage.GIVING);

        assertEquals(new PersonBuilder(aliceByBenson).withStage("giving").build(), updated);
        assertEquals(aliceByBenson.getInteractions(), updated.getInteractions());
    }

    @Test
    public void withInteractions_defensivelyCopiesHistoryAndPreservesOrder() {
        Interaction earlier = new Interaction(LocalDate.of(2024, 2, 20), "Called");
        Interaction later = new Interaction(LocalDate.of(2024, 3, 10), "Met");
        List<Interaction> interactions = new ArrayList<>(List.of(later, earlier, later));

        Person updated = ALICE.withInteractions(interactions);
        interactions.clear();

        assertEquals(List.of(later, earlier, later), updated.getInteractions());
        assertThrows(UnsupportedOperationException.class, () -> updated.getInteractions().clear());
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

    /**
     * Verifies the latest date and that reading it preserves every interaction in its original logging order.
     */
    private void assertLastInteractionDateAndHistory(Optional<LocalDate> expectedDate, Interaction... interactions) {
        List<Interaction> expectedInteractions = List.of(interactions);
        Person person = new PersonBuilder().withInteractions(interactions).build();

        assertEquals(expectedDate, person.getLastInteractionDate());
        assertEquals(expectedInteractions, person.getInteractions());
    }

    /**
     * A supporter with one interaction and a supplied display order for testing inconsistent mappings.
     */
    private static class PersonWithDisplayOrderStub extends Person {
        private final List<Integer> displayOrderIndices;

        PersonWithDisplayOrderStub(List<Integer> displayOrderIndices) {
            super(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                    ALICE.getOrganisation(), ALICE.getIntroducer(), ALICE.getStage(),
                    List.of(new Interaction(LocalDate.of(2024, 2, 29), "Called")), ALICE.getTags());
            this.displayOrderIndices = displayOrderIndices;
        }

        @Override
        public List<Integer> getInteractionIndicesInDisplayOrder() {
            return displayOrderIndices;
        }
    }
}
