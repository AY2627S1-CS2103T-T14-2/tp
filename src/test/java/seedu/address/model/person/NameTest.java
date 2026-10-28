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
    public void isValidName_accentedLetters_returnsTrue() {
        assertTrue(Name.isValidName("José Tan"));
        assertTrue(Name.isValidName("Émilie Tan"));
    }

    @Test
    public void isValidName_combiningMarksAfterLetters_returnsTrue() {
        assertTrue(Name.isValidName("கவி")); // nonspacing vowel mark
        assertTrue(Name.isValidName("கா")); // spacing vowel mark
        assertTrue(Name.isValidName("Jose\u0301 Tan")); // decomposed acute accent
        assertTrue(Name.isValidName("E\u0301milie Tan")); // mark attached to the first letter
        assertTrue(Name.isValidName("A\u20dd Tan")); // enclosing circle mark
        assertTrue(Name.isValidName("q\u0301\u0323 Tan")); // multiple marks attached to one letter
        assertTrue(Name.isValidName("2nd கவி")); // digit at the start remains allowed
        assertTrue(Name.isValidName("கவி s/o Jose\u0301"));
        assertTrue(Name.isValidName("Jose\u0301-Tan d/o கவி"));
    }

    @Test
    public void isValidName_combiningMarksWithoutLetter_returnsFalse() {
        assertFalse(Name.isValidName("\u0301")); // mark alone
        assertFalse(Name.isValidName("\u0301Tan")); // leading mark
        assertFalse(Name.isValidName("  \u0301Tan  ")); // leading mark after space normalization
        assertFalse(Name.isValidName("1\u0301Tan")); // mark after an initial digit
        assertFalse(Name.isValidName("Tan2\u0301")); // mark after a later digit
        assertFalse(Name.isValidName("Tan \u0301Lim")); // mark after a space
        assertFalse(Name.isValidName("Tan\t\u0301Lim")); // mark after a normalized space
        assertFalse(Name.isValidName("Tan'\u0301Lim")); // mark after a straight apostrophe
        assertFalse(Name.isValidName("Tan’\u0301Lim")); // mark after a curly apostrophe
        assertFalse(Name.isValidName("Tan-\u0301Lim")); // mark after a hyphen
        assertFalse(Name.isValidName("Tan.\u0301Lim")); // mark after a period
        assertFalse(Name.isValidName("Tan \u0301\u0323Lim")); // unattached sequence of marks
        assertFalse(Name.isValidName("Ravi s/o\u0301 Kumar")); // slash word must remain a whole word
        assertFalse(Name.isValidName("Priya d/o\u0301 Kumar"));
    }

    @Test
    public void constructor_unattachedCombiningMark_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Name("Tan \u0301Lim"));
    }

    @Test
    public void constructor_decomposedName_normalizesUnicodeAndSpaces() {
        assertEquals("José Tan", new Name("  Jose\u0301\t Tan  ").fullName);
        assertEquals("José Tan", new Name("\u00a0Jose\u0301\u00a0\u00a0Tan\u00a0").fullName);
        assertEquals("José Tan", new Name("Jose\u0301 Tan").toString());
        assertEquals("கவி", new Name("கவி").fullName);
    }

    @Test
    public void constructor_longCombiningSequence_preservesNameWithoutStackOverflow() {
        String longName = "q" + "\u0301".repeat(50_000);

        assertEquals(longName, new Name(longName).fullName);
    }

    @Test
    public void isValidName_longNameWithCombiningMarks_returnsTrue() {
        assertTrue(Name.isValidName("கவி".repeat(20_000)));
    }

    @Test
    public void isValidName_longInvalidCombiningSequence_returnsFalse() {
        assertFalse(Name.isValidName("q" + "\u0301".repeat(50_000) + "*"));
        assertFalse(Name.isValidName("கவி".repeat(20_000) + " \u0301"));
    }

    @Test
    public void isSameName_canonicallyEquivalentNames_returnsTrue() {
        Name composedName = new Name("José Tan");
        Name decomposedName = new Name("Jose\u0301 Tan");

        assertTrue(composedName.isSameName(decomposedName));
        assertTrue(decomposedName.isSameName(composedName));
        assertTrue(composedName.isSameName(new Name("  JOSE\u0301  TAN  ")));
        assertFalse(composedName.isSameName(new Name("Jose Tan"))); // accents remain significant
    }

    @Test
    public void equals_canonicallyEquivalentNames_returnsTrue() {
        Name composedName = new Name("José Tan");
        Name decomposedName = new Name("Jose\u0301 Tan");

        assertEquals(composedName, decomposedName);
        assertEquals(decomposedName, composedName);
        assertFalse(composedName.equals(new Name("Jose Tan")));
        assertFalse(composedName.equals(new Name("JOSE\u0301 TAN"))); // equality remains case-sensitive
    }

    @Test
    public void hashCode_canonicallyEquivalentNames_sameHashCode() {
        Name composedName = new Name("José Tan");
        Name decomposedName = new Name("Jose\u0301 Tan");

        assertEquals(composedName.hashCode(), decomposedName.hashCode());
    }

    @Test
    public void isValidName_nonLatinLetters_returnsTrue() {
        assertTrue(Name.isValidName("李伟"));
        assertTrue(Name.isValidName("Αλέξανδρος"));
        assertTrue(Name.isValidName("Иван"));
    }

    @Test
    public void isValidName_curlyApostrophe_returnsTrue() {
        assertTrue(Name.isValidName("Charlotte O’Brien"));
    }

    @Test
    public void isValidName_curlyApostropheStart_returnsFalse() {
        assertFalse(Name.isValidName("’Tan"));
        assertFalse(Name.isValidName("  ’Tan  "));
    }

    @Test
    public void isValidName_unicodeSymbols_returnsFalse() {
        assertFalse(Name.isValidName("Tan☀"));
        assertFalse(Name.isValidName("☀Tan"));
    }

    @Test
    public void isValidName_nonBreakingSpaces_returnsTrue() {
        String[] names = {"Tan\u00a0Wei Ming", "\u00a0Tan Wei Ming", "Tan Wei Ming\u00a0",
            "Tan\u00a0\u00a0Wei Ming", " \u00a0Tan \t\u00a0Wei\u00a0 Ming\u00a0 ", "Ravi\u00a0s/o\u00a0Kumar"};

        for (String name : names) {
            assertTrue(Name.isValidName(name));
        }
    }

    @Test
    public void isValidName_onlyNonBreakingSpaces_returnsFalse() {
        assertFalse(Name.isValidName("\u00a0"));
        assertFalse(Name.isValidName(" \t\u00a0\u00a0 "));
    }

    @Test
    public void constructor_onlyNonBreakingSpaces_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Name("\u00a0"));
        assertThrows(IllegalArgumentException.class, () -> new Name(" \t\u00a0\u00a0 "));
    }

    @Test
    public void isValidName_supportedPunctuation_returnsTrue() {
        assertTrue(Name.isValidName("O'Brien"));
        assertTrue(Name.isValidName("Lee-Tan"));
        assertTrue(Name.isValidName("J. Tan"));
        assertTrue(Name.isValidName("J. O'Brien-Tan"));
        assertTrue(Name.isValidName("2nd Lee-Tan"));
        assertTrue(Name.isValidName("Tan'-.."));
        assertTrue(Name.isValidName("Ahmad bin Ali @ Ah Mat"));
        assertTrue(Name.isValidName("Tan Ah Kow (Chen Ah Kow)"));
        assertTrue(Name.isValidName("Tan, Ah Kow"));
        assertTrue(Name.isValidName("Tan(Ah Kow)@Chen,Ah Kow"));
        assertTrue(Name.isValidName("Tan (Ah Kow"));
        assertTrue(Name.isValidName("Tan Ah Kow)"));
    }

    @Test
    public void isValidName_nonAlphanumericStart_returnsFalse() {
        assertFalse(Name.isValidName("'Tan"));
        assertFalse(Name.isValidName("-Tan"));
        assertFalse(Name.isValidName(".Tan"));
        assertFalse(Name.isValidName("  .Tan  "));
        assertFalse(Name.isValidName("'-."));
        assertFalse(Name.isValidName(",Tan"));
        assertFalse(Name.isValidName("(Tan)"));
        assertFalse(Name.isValidName(")Tan"));
        assertFalse(Name.isValidName("@Tan"));
        assertFalse(Name.isValidName("  @Tan  "));
        assertFalse(Name.isValidName(",()@"));
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
        assertEquals("Ahmad bin Ali @ Ah Mat", new Name("Ahmad bin Ali @ Ah Mat").fullName);
        assertEquals("Tan Ah Kow (Chen Ah Kow)", new Name("Tan Ah Kow (Chen Ah Kow)").fullName);
        assertEquals("Tan, Ah Kow", new Name("Tan, Ah Kow").fullName);
    }

    @Test
    public void constructor_supportedNameWithExtraSpaces_spacesNormalised() {
        assertEquals("Ravi s/o Kumar", new Name("  Ravi   s/o\tKumar  ").fullName);
        assertEquals("J. O'Brien-Tan", new Name("  J.   O'Brien-Tan  ").fullName);
        assertEquals("Ahmad bin Ali @ Ah Mat", new Name("  Ahmad   bin Ali\t@   Ah Mat  ").fullName);
        assertEquals("Tan Ah Kow (Chen Ah Kow)", new Name("  Tan  Ah Kow   (Chen\tAh Kow)  ").fullName);
        assertEquals("Tan, Ah Kow", new Name("  Tan,\tAh   Kow  ").fullName);
    }

    @Test
    public void constructor_extraSpaces_spacesNormalised() {
        assertEquals("Tan Wei Ming", new Name("  Tan   Wei Ming ").fullName);
        assertEquals(new Name("Tan Wei Ming"), new Name("Tan  Wei  Ming"));
    }

    @Test
    public void constructor_nonBreakingSpaces_spacesNormalised() {
        String[] names = {"Tan\u00a0Wei Ming", "\u00a0Tan Wei Ming", "Tan Wei Ming\u00a0",
            "Tan\u00a0\u00a0Wei Ming", " \u00a0Tan \t\u00a0Wei\u00a0 Ming\u00a0 "};
        Name expectedName = new Name("Tan Wei Ming");

        for (String name : names) {
            Name normalizedName = new Name(name);

            assertEquals("Tan Wei Ming", normalizedName.fullName);
            assertEquals(expectedName, normalizedName);
        }
    }

    @Test
    public void isSameName_nonBreakingSpaces_returnsTrue() {
        Name name = new Name("Tan Wei Ming");

        assertTrue(name.isSameName(new Name("\u00a0tan\u00a0\u00a0WEI ming\u00a0")));
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
    public void isSameName_differentApostropheStyles_returnsTrue() {
        Name straightName = new Name("O'Brien");
        Name curlyName = new Name("O’Brien");

        assertTrue(straightName.isSameName(curlyName));
        assertTrue(curlyName.isSameName(straightName));
    }

    @Test
    public void isSameName_differentApostropheStylesWithCaseAndSpaces_returnsTrue() {
        Name straightName = new Name("Charlotte O'Brien");
        Name curlyName = new Name("  CHARLOTTE   O’BRIEN  ");

        assertTrue(straightName.isSameName(curlyName));
        assertTrue(curlyName.isSameName(straightName));
    }

    @Test
    public void isSameName_differentPunctuation_returnsFalse() {
        Name name = new Name("O'Brien");

        assertFalse(name.isSameName(new Name("OBrien")));
        assertFalse(name.isSameName(new Name("O-Brien")));
    }

    @Test
    public void constructor_differentApostropheStyles_preservesOriginalSpelling() {
        Name straightName = new Name("Charlotte O'Brien");
        Name curlyName = new Name("Charlotte O’Brien");

        assertEquals("Charlotte O'Brien", straightName.fullName);
        assertEquals("Charlotte O'Brien", straightName.toString());
        assertEquals("Charlotte O’Brien", curlyName.fullName);
        assertEquals("Charlotte O’Brien", curlyName.toString());
    }

    @Test
    public void equals_differentApostropheStyles_returnsFalse() {
        Name straightName = new Name("O'Brien");
        Name curlyName = new Name("O’Brien");

        assertFalse(straightName.equals(curlyName));
        assertFalse(curlyName.equals(straightName));
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
