package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {

    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers must contain 3 to 15 main-number digits. They may start with +, "
                    + "use single spaces or hyphens between digit groups, and enclose digit groups in parentheses. "
                    + "An optional 'ext' followed by 1 to 6 digits and an optional alphabetic label in parentheses "
                    + "may follow, separated by single spaces, e.g. (65) 9123-4567 ext 12 (Office)";

    public static final String VALIDATION_REGEX =
            "(" + Phone.MAIN_NUMBER_REGEX + ")" + Phone.EXTENSION_REGEX + Phone.LABEL_REGEX;

    /*
     * 3 digits allows short hotlines, and 15 digits is the longest a phone number can be internationally.
     */
    private static final int MIN_DIGITS = 3;
    private static final int MAX_DIGITS = 15;
    private static final int MAX_EXTENSION_DIGITS = 6;
    private static final String DIGIT_GROUP_REGEX = "(?:[0-9]+|\\([0-9]+\\))";
    private static final String MAIN_NUMBER_REGEX = "\\+?" + DIGIT_GROUP_REGEX + "(?:[ -]" + DIGIT_GROUP_REGEX + ")*";
    private static final String EXTENSION_REGEX = "(?: (?i:ext) [0-9]{1," + MAX_EXTENSION_DIGITS + "})?";
    private static final String TYPE_LABEL_REGEX = " \\([A-Za-z]+(?: [A-Za-z]+)*\\)";
    private static final String LABEL_REGEX = "(?:" + TYPE_LABEL_REGEX + ")?";

    private static final Pattern VALIDATION_PATTERN = Pattern.compile(VALIDATION_REGEX);
    private static final String TRAILING_LABEL_REGEX = TYPE_LABEL_REGEX + "$";

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
        Matcher matcher = VALIDATION_PATTERN.matcher(test);
        if (!matcher.matches()) {
            return false;
        }
        long digitCount = matcher.group(1).chars().filter(Character::isDigit).count();
        return digitCount >= MIN_DIGITS && digitCount <= MAX_DIGITS;
    }

    /**
     * Returns whether {@code test} is a formatted phone search fragment.
     * Fragments follow the phone syntax but do not need 3 to 15 main-number digits.
     */
    public static boolean isValidPhoneSearch(String test) {
        requireNonNull(test);
        return VALIDATION_PATTERN.matcher(test).matches();
    }

    /**
     * Returns search digits, ignoring separators, parentheses, extension markers and a trailing type label.
     * Extension digits remain searchable. This method does not validate the supplied text.
     */
    public static String normalizeForSearch(String phone) {
        requireNonNull(phone);
        return phone.replaceAll(TRAILING_LABEL_REGEX, "")
                .replaceAll("(?i) ext ", "")
                .replaceAll("[+ ()-]", "");
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
