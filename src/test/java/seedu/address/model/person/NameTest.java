package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void constructor_longName_preservesNameWithoutStackOverflow() {
        String longName = "A".repeat(50_000);

        assertEquals(longName, new Name(longName).fullName);
    }

    @Test
    public void isValidName_longNameWithSlashWords_returnsTrue() {
        assertTrue(Name.isValidName("s/o " + "A".repeat(5_000) + " D/o " + "O'Brien-Tan. ".repeat(5_000)));
        assertTrue(Name.isValidName("A".repeat(5_000) + " s/o"));
    }

    @Test
    public void isValidName_longInvalidName_returnsFalse() {
        assertFalse(Name.isValidName("A".repeat(50_000) + "*"));
        assertFalse(Name.isValidName("A".repeat(5_000) + " s/oKumar"));
        assertFalse(Name.isValidName("A".repeat(5_000) + " s/o.Kumar"));
        assertFalse(Name.isValidName("A".repeat(5_000) + "s/o"));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains non-alphanumeric characters

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
        assertTrue(Name.isValidName("  Tan   Wei Ming ")); // extra spaces, removed when saved
    }

    @Test
    public void isValidName_supportedPunctuation_returnsTrue() {
        assertTrue(Name.isValidName("O'Brien"));
        assertTrue(Name.isValidName("Lee-Tan"));
        assertTrue(Name.isValidName("J. Tan"));
        assertTrue(Name.isValidName("J. O'Brien-Tan"));
        assertTrue(Name.isValidName("2nd Lee-Tan"));
        assertTrue(Name.isValidName("Tan'-.."));
    }

    @Test
    public void isValidName_nonAlphanumericStart_returnsFalse() {
        assertFalse(Name.isValidName("'Tan"));
        assertFalse(Name.isValidName("-Tan"));
        assertFalse(Name.isValidName(".Tan"));
        assertFalse(Name.isValidName("  .Tan  "));
        assertFalse(Name.isValidName("'-."));
    }

    @Test
    public void isValidName_supportedSlashWords_returnsTrue() {
        assertTrue(Name.isValidName("Ravi s/o Kumar"));
        assertTrue(Name.isValidName("Priya d/o Kumar"));
        assertTrue(Name.isValidName("Ravi S/O Kumar"));
        assertTrue(Name.isValidName("Priya D/O Kumar"));
        assertTrue(Name.isValidName("Ravi s/O Kumar"));
        assertTrue(Name.isValidName("Priya D/o Kumar"));
        assertTrue(Name.isValidName("s/o Kumar"));
        assertTrue(Name.isValidName("d/o Kumar"));
        assertTrue(Name.isValidName("Ravi s/o"));
        assertTrue(Name.isValidName("Priya d/o"));
        assertTrue(Name.isValidName("s/o"));
        assertTrue(Name.isValidName("d/o"));
        assertTrue(Name.isValidName("  Ravi   s/o\tKumar  "));
        assertTrue(Name.isValidName("J. O'Brien-Tan d/o Kumar"));
    }

    @Test
    public void isValidName_unsupportedSlashForms_returnsFalse() {
        assertFalse(Name.isValidName("Tan/Lim"));
        assertFalse(Name.isValidName("Ravi a/l Kumar"));
        assertFalse(Name.isValidName("Priya a/p Kumar"));
        assertFalse(Name.isValidName("Ravi s/ Kumar"));
        assertFalse(Name.isValidName("Priya d/ Kumar"));
        assertFalse(Name.isValidName("Ravi s/ o Kumar"));
        assertFalse(Name.isValidName("Ravis/o Kumar"));
        assertFalse(Name.isValidName("Priyad/o Kumar"));
        assertFalse(Name.isValidName("Ravi s/oKumar"));
        assertFalse(Name.isValidName("Priya d/oKumar"));
        assertFalse(Name.isValidName("Ravi-s/o Kumar"));
        assertFalse(Name.isValidName("Priya d/o.Kumar"));
        assertFalse(Name.isValidName("Ravi s//o Kumar"));
        assertFalse(Name.isValidName("Ravi / Kumar"));
    }

    @Test
    public void constructor_supportedName_preservesPunctuationAndCase() {
        assertEquals("J. O'Brien-Tan D/O Kumar", new Name("J. O'Brien-Tan D/O Kumar").fullName);
    }

    @Test
    public void constructor_supportedNameWithExtraSpaces_spacesNormalised() {
        assertEquals("Ravi s/o Kumar", new Name("  Ravi   s/o\tKumar  ").fullName);
        assertEquals("J. O'Brien-Tan", new Name("  J.   O'Brien-Tan  ").fullName);
    }

    @Test
    public void constructor_extraSpaces_spacesNormalised() {
        assertEquals("Tan Wei Ming", new Name("  Tan   Wei Ming ").fullName);
        assertEquals(new Name("Tan Wei Ming"), new Name("Tan  Wei  Ming"));
    }

    @Test
    public void isSameName() {
        Name name = new Name("Tan Wei Ming");

        // same name -> returns true
        assertTrue(name.isSameName(new Name("Tan Wei Ming")));

        // different case -> returns true
        assertTrue(name.isSameName(new Name("tan WEI ming")));

        // extra spaces -> returns true
        assertTrue(name.isSameName(new Name(" Tan  Wei   Ming ")));

        // null -> returns false
        assertFalse(name.isSameName(null));

        // different name -> returns false
        assertFalse(name.isSameName(new Name("Tan Wei Min")));
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
