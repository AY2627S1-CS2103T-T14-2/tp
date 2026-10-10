package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents the organisation a supporter belongs to, e.g. the company a CSR contact speaks for.
 * Guarantees: immutable; is valid as declared in {@link #isValidOrganisation(String)}
 */
public class Organisation {

    public static final int MAX_LENGTH = 100;

    public static final String MESSAGE_CONSTRAINTS = "Organisation names should not be blank and should be at most "
            + MAX_LENGTH + " characters long. Leave out o/ if the supporter has no organisation.";

    public final String value;

    /**
     * Constructs an {@code Organisation}, removing leading and trailing spaces
     * and reducing each run of spaces inside the name to one.
     *
     * @param organisation A valid organisation name.
     */
    public Organisation(String organisation) {
        requireNonNull(organisation);
        checkArgument(isValidOrganisation(organisation), MESSAGE_CONSTRAINTS);
        value = normalise(organisation);
    }

    /**
     * Returns true if a given string is a valid organisation name once its spaces are normalised.
     * Any characters are allowed, as company names often contain symbols such as {@code &} and {@code '}.
     */
    public static boolean isValidOrganisation(String test) {
        requireNonNull(test);
        String normalised = normalise(test);
        return !normalised.isEmpty() && normalised.length() <= MAX_LENGTH;
    }

    private static String normalise(String organisation) {
        return organisation.trim().replaceAll("\\s+", " ");
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
        if (!(other instanceof Organisation otherOrganisation)) {
            return false;
        }

        return value.equals(otherOrganisation.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
