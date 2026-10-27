package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names must start with a letter or digit, and may only contain Unicode letters, digits, spaces, "
                    + "apostrophes (' or ’), hyphens (-), periods (.), and whole words s/o or d/o (case-insensitive)";

    /*
     * The first character must be a letter or digit after space normalization.
     * Unicode letters and straight or curly apostrophes are supported; digits remain 0-9.
     * Slashes are allowed only in whole words s/o or d/o, ignoring case.
     * The lookahead checks the first character without consuming it, allowing s/o or d/o at the start.
     * Match slash words before individual letters. Possessive repetition avoids recursive backtracking
     * on long names without changing the accepted characters or imposing a length limit.
     */
    public static final String VALIDATION_REGEX =
            "(?=[\\p{L}0-9])(?:(?<!\\S)(?i:[sd]/o)(?!\\S)|[\\p{L}0-9 '’.-])++";

    public final String fullName;

    /**
     * Constructs a {@code Name}, removing leading and trailing spaces
     * and reducing each run of spaces inside the name to one.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = normalise(name);
    }

    /**
     * Returns true if a given string is a valid name once its spaces are normalized.
     */
    public static boolean isValidName(String test) {
        return normalise(test).matches(VALIDATION_REGEX);
    }

    private static String normalise(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }

    /**
     * Returns true if both names are the same, ignoring case.
     * Spaces need no special handling, as every {@code Name} has its spaces normalized.
     */
    public boolean isSameName(Name otherName) {
        return otherName != null && fullName.equalsIgnoreCase(otherName.fullName);
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
