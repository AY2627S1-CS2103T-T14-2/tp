package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class OrganisationTest {

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
        assertEquals(new Organisation("DBS Bank"), new Organisation("DBS  Bank"));
    }

    @Test
    public void isValidOrganisation() {
        // null organisation
        assertThrows(NullPointerException.class, () -> Organisation.isValidOrganisation(null));

        // invalid organisations
        assertFalse(Organisation.isValidOrganisation("")); // empty string
        assertFalse(Organisation.isValidOrganisation("   ")); // spaces only
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
