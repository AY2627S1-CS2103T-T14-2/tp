package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.regex.Pattern;

/**
 * Represents a Person's email in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmail(String)}
 */
public class Email {

    private static final String SPECIAL_CHARACTERS = "+_.-";
    public static final String MESSAGE_CONSTRAINTS = "Emails should be of the format local-part@domain "
            + "and adhere to the following constraints:\n"
            + "1. The local-part should only contain alphanumeric characters and these special characters, excluding "
            + "the parentheses, (" + SPECIAL_CHARACTERS + "). The local-part may not start or end with a period, "
            + "or have two periods in a row.\n"
            + "2. The local-part is followed by an '@' and then a domain name. The domain name is made up of at "
            + "least two domain labels separated by periods, e.g. example.com.\n"
            + "The domain name must:\n"
            + "    - end with a domain label of at least 2 letters only, e.g. com or sg\n"
            + "    - have each domain label start and end with alphanumeric characters\n"
            + "    - have each domain label consist of alphanumeric characters, separated only by hyphens, if any.";
    private static final String ALPHANUMERIC_NO_UNDERSCORE = "[A-Za-z0-9]++";
    // dot-atom form (RFC 5322): words of alphanumeric characters, '+', '_' and '-', joined by single periods
    // Separators cannot occur in words, so possessive repetition keeps long inputs stack safe.
    private static final String LOCAL_PART_WORD = "[\\w+-]++";
    private static final String LOCAL_PART_REGEX = "^" + LOCAL_PART_WORD + "(?:\\." + LOCAL_PART_WORD + ")*+";
    // Consecutive internal hyphens are valid, including the xn-- prefix of ASCII IDN labels.
    private static final String DOMAIN_PART_REGEX = ALPHANUMERIC_NO_UNDERSCORE
            + "(?:-++" + ALPHANUMERIC_NO_UNDERSCORE + ")*+";
    // the top-level domain, e.g. com or sg, has letters only, so 123@145.67 and a@bc are not accepted
    private static final String DOMAIN_LAST_PART_REGEX = "[A-Za-z]{2,}+$";
    private static final String DOMAIN_REGEX = "(?:" + DOMAIN_PART_REGEX + "\\.)++" + DOMAIN_LAST_PART_REGEX;
    public static final String VALIDATION_REGEX = LOCAL_PART_REGEX + "@" + DOMAIN_REGEX;

    private static final Pattern VALIDATION_PATTERN = Pattern.compile(VALIDATION_REGEX);

    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        requireNonNull(email);
        checkArgument(isValidEmail(email), MESSAGE_CONSTRAINTS);
        value = email;
    }

    /**
     * Returns true if a given string is a valid email.
     */
    public static boolean isValidEmail(String test) {
        requireNonNull(test);
        return VALIDATION_PATTERN.matcher(test).matches();
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
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
