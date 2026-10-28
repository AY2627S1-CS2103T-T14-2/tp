package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class OrganisationTest {

    private static final List<String> WHITESPACE_SEQUENCES = List.of(" ", "\t", "\r\n", "\u0085", "\u00a0",
            "\u1680", "\u2003", "\u2007", "\u2028", "\u2029", "\u202f", "\u205f", "\u3000");

    @Test
    public void constructor_unicodeLengthBoundaries_checksCodePoints() {
        String organisationAtLimit = "a".repeat(99) + "😀";
        // the joined emoji contains three code points, despite appearing as one symbol
        for (String name : List.of(organisationAtLimit, "😀".repeat(100),
                "a".repeat(97) + "👩‍💻")) {
            assertTrue(Organisation.isValidOrganisation(name));
            assertEquals(name, new Organisation(name).value);
            assertFalse(Organisation.isValidOrganisation(name + "a"));
            assertThrows(IllegalArgumentException.class,
                    Organisation.MESSAGE_CONSTRAINTS, () -> new Organisation(name + "a"));
        }
    }

    @Test
    public void constructor_unicodeBoundaryWithExtraSpaces_countsNormalisedName() {
        // the single internal space counts towards the limit; extra spaces do not
        String normalised = "a".repeat(98) + " 😀";
        String padded = "  " + "a".repeat(98) + "   😀  ";

        assertEquals(normalised, new Organisation(padded).value);
        assertThrows(IllegalArgumentException.class,
                Organisation.MESSAGE_CONSTRAINTS, () -> new Organisation("  " + normalised + "a  "));
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Organisation(null));
    }

    @Test
    public void constructor_invalidOrganisation_throwsIllegalArgumentException() {
        String invalidOrganisation = "";
        assertThrows(IllegalArgumentException.class, () -> new Organisation(invalidOrganisation));
    }

    @Test
    public void constructor_extraSpaces_spacesNormalised() {
        assertEquals("DBS Bank", new Organisation("  DBS   Bank  ").value);
        assertEquals("DBS Bank", new Organisation(" DBS Bank ").value);
        assertEquals(new Organisation("DBS Bank"), new Organisation("DBS  Bank"));
    }

    @Test
    public void constructor_unicodeWhitespace_normalizesSpacesAndPreservesEquality() {
        Organisation expectedOrganisation = new Organisation("Keppel Land");
        for (String whitespace : WHITESPACE_SEQUENCES) {
            String name = whitespace + "Keppel" + whitespace.repeat(2) + "Land" + whitespace;
            Organisation organisation = new Organisation(name);

            assertEquals("Keppel Land", organisation.value, name);
            assertEquals(expectedOrganisation, organisation, name);
            assertEquals(expectedOrganisation.hashCode(), organisation.hashCode(), name);
        }
    }

    @Test
    public void constructor_unicodeWhitespaceLengthBoundary_countsNormalizedName() {
        String normalizedName = "a".repeat(Organisation.MAX_LENGTH - 2) + " X";
        for (String whitespace : WHITESPACE_SEQUENCES) {
            String paddedName = whitespace + normalizedName.replace(" ", whitespace.repeat(3)) + whitespace;
            String tooLongName = whitespace + (normalizedName + "a").replace(" ", whitespace.repeat(3)) + whitespace;

            assertTrue(Organisation.isValidOrganisation(paddedName), paddedName);
            assertEquals(normalizedName, new Organisation(paddedName).value, paddedName);
            assertFalse(Organisation.isValidOrganisation(tooLongName), tooLongName);
            assertThrows(IllegalArgumentException.class,
                    Organisation.MESSAGE_CONSTRAINTS, () -> new Organisation(tooLongName));
        }
    }

    @Test
    public void isValidOrganisation() {
        // null organisation
        assertThrows(NullPointerException.class, () -> Organisation.isValidOrganisation(null));

        // invalid organisations
        assertFalse(Organisation.isValidOrganisation("")); // empty string
        assertFalse(Organisation.isValidOrganisation("   ")); // spaces only
        assertFalse(Organisation.isValidOrganisation(" ")); // no-break space only
        assertFalse(Organisation.isValidOrganisation("     ")); // Unicode spaces only
        assertFalse(Organisation.isValidOrganisation("a".repeat(Organisation.MAX_LENGTH + 1))); // too long

        // valid organisations
        assertTrue(Organisation.isValidOrganisation("DBS Bank"));
        assertTrue(Organisation.isValidOrganisation("Procter & Gamble")); // symbols
        assertTrue(Organisation.isValidOrganisation("Ernst & Young LLP (S'pore), Inc.")); // punctuation
        assertTrue(Organisation.isValidOrganisation("a".repeat(Organisation.MAX_LENGTH))); // longest allowed
        assertTrue(Organisation.isValidOrganisation(" " + "a".repeat(Organisation.MAX_LENGTH) + " ")); // trimmed
        assertTrue(Organisation.isValidOrganisation("X")); // one character
    }

    @Test
    public void hashCode_equalOrganisations_sameHashCode() {
        assertEquals(new Organisation("DBS Bank").hashCode(), new Organisation("DBS  Bank").hashCode());
    }

    @Test
    public void equals() {
        Organisation organisation = new Organisation("DBS Bank");

        // same values -> returns true
        assertTrue(organisation.equals(new Organisation("DBS Bank")));

        // same object -> returns true
        assertTrue(organisation.equals(organisation));

        // null -> returns false
        assertFalse(organisation.equals(null));

        // different types -> returns false
        assertFalse(organisation.equals(5.0f));

        // different values -> returns false
        assertFalse(organisation.equals(new Organisation("OCBC Bank")));
    }
}
