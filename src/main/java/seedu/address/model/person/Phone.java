package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {

    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers may start with a plus sign and may use single spaces or hyphens "
                    + "between digit groups, including a mixture of both. "
                    + "They must contain between 3 and 15 digits";

    public static final String VALIDATION_REGEX = "\\+?\\d+(?:[ -]\\d+)*";

    /*
     * 3 digits allows short hotlines, and 15 digits is the longest a phone number can be internationally.
     */
    private static final int MIN_DIGITS = 3;
    private static final int MAX_DIGITS = 15;
    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = phone;
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        requireNonNull(test);
        long digitCount = test.chars().filter(Character::isDigit).count();
        return test.matches(VALIDATION_REGEX)
                && digitCount >= MIN_DIGITS
                && digitCount <= MAX_DIGITS;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
