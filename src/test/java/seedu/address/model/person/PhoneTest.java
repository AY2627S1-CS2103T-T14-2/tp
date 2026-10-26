package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone("")); // empty string
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("91")); // less than 3 numbers
        assertFalse(Phone.isValidPhone("phone")); // non-numeric
        assertFalse(Phone.isValidPhone("9011p041")); // alphabets within digits
        assertFalse(Phone.isValidPhone("+")); // plus sign without digits
        assertFalse(Phone.isValidPhone("++65 9123 4567")); // multiple plus signs
        assertFalse(Phone.isValidPhone("65+91234567")); // plus sign within number
        assertFalse(Phone.isValidPhone("+65  9123 4567")); // consecutive spaces
        assertFalse(Phone.isValidPhone("+65--9123-4567")); // consecutive hyphens
        assertFalse(Phone.isValidPhone("+65- 9123-4567")); // adjacent separators
        assertFalse(Phone.isValidPhone("+65 9123 ")); // trailing separator
        assertFalse(Phone.isValidPhone("1242938420331234")); // more than 15 numbers
        assertFalse(Phone.isValidPhone("+12 4293 8420 3312 34")); // more than 15 formatted digits
        assertFalse(Phone.isValidPhone("+12-4293 8420-3312 34")); // more than 15 mixed-format digits

        // valid phone numbers
        assertTrue(Phone.isValidPhone("911")); // exactly 3 numbers
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("+6591234567"));
        assertTrue(Phone.isValidPhone("+65 9123 4567"));
        assertTrue(Phone.isValidPhone("+65-9123-4567"));
        assertTrue(Phone.isValidPhone("+65 9123-4567")); // mixed separator types
        assertTrue(Phone.isValidPhone("+65-9123 4567")); // mixed separator types in reverse order
        assertTrue(Phone.isValidPhone("65 9123 4567"));
        assertTrue(Phone.isValidPhone("65-9123-4567"));
        assertTrue(Phone.isValidPhone("124293842033123")); // exactly 15 numbers
        assertTrue(Phone.isValidPhone("+12 4293 8420 3312 3")); // exactly 15 formatted digits
        assertTrue(Phone.isValidPhone("+12-4293 8420-3312 3")); // exactly 15 mixed-format digits
    }

    @Test
    public void equals() {
        Phone phone = new Phone("999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("999")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("995")));
    }

    @Test
    public void isValidPhone_commonAnnotations_acceptsAndPreservesFormatting() {
        for (String phone : List.of("9123 4567 (HP)", "(65) 9123 4567", "9123 4567 ext 12",
                "+65 (9123)-4567", "9123-4567 (Home)", "9123 4567 (Office Fax)",
                "9123 4567 EXT 123456 (Office)", "123 ext 1", "123456789012345 ext 123456")) {
            assertTrue(Phone.isValidPhone(phone), phone);
            assertEquals(phone, new Phone(phone).value);
        }
    }

    @Test
    public void isValidPhone_malformedAnnotations_rejects() {
        for (String phone : List.of("(65 9123 4567", "65) 9123 4567", "((65)) 9123 4567", "() 9123",
                "(65)9123 4567", "9123 4567 ()", "9123 4567 (HP2)", "9123 4567 (HP) (Office)",
                "9123 4567 HP", "9123 4567 ext", "9123 4567 ext ABC", "9123 4567 ext 1234567",
                "9123 4567 ext 1 ext 2", "9123 4567 (HP) ext 12", "12 ext 123456", "1234567890123456 ext 1",
                "9123 4567  (HP)", "9123 4567 ext  12", "9123 4567 ( Office )")) {
            assertFalse(Phone.isValidPhone(phone), phone);
        }
    }

    @Test
    public void isValidPhoneSearch_fragments_acceptsWithoutNumberLengthLimit() {
        assertThrows(NullPointerException.class, () -> Phone.isValidPhoneSearch(null));
        for (String query : List.of("9", "(65)", "+65 91-23", "9123 (HP)", "9 ext 1 (Office)",
                "1234567890123456")) {
            assertTrue(Phone.isValidPhoneSearch(query), query);
        }
        for (String query : List.of("", "+", "(HP)", "912a", "(65", "9 ext 1234567")) {
            assertFalse(Phone.isValidPhoneSearch(query), query);
        }
    }

    @Test
    public void normalizeForSearch_annotations_removesFormattingButKeepsExtensionDigits() {
        assertThrows(NullPointerException.class, () -> Phone.normalizeForSearch(null));
        assertEquals("6591234567", Phone.normalizeForSearch("+(65) 9123-4567 (HP)"));
        assertEquals("9123456712", Phone.normalizeForSearch("9123 4567 EXT 12 (Office Fax)"));
        assertEquals("65abc", Phone.normalizeForSearch("+65 abc"));
        assertEquals("", Phone.normalizeForSearch("+ -"));
    }
}
