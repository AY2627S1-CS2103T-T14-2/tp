package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

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
}
