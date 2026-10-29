package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Locale;

/**
 * Represents a supporter's cultivation stage, i.e. how far the relationship has progressed.
 * Guarantees: immutable; is valid as declared in {@link #isValidStage(String)}
 */
public enum Stage {
    PROSPECT,
    CONTACTED,
    CULTIVATING,
    GIVING,
    LAPSED,
    DECLINED;

    public static final String MESSAGE_CONSTRAINTS =
            "Stage should be one of: prospect, contacted, cultivating, giving, lapsed, declined";

    /** The stage given to a new supporter when none is specified. */
    public static final Stage DEFAULT_STAGE = PROSPECT;

    /**
     * Returns true if a given string is one of the stages, ignoring case and surrounding whitespace.
     */
    public static boolean isValidStage(String test) {
        requireNonNull(test);
        String trimmed = test.trim();
        return Arrays.stream(values()).anyMatch(stage -> stage.name().equalsIgnoreCase(trimmed));
    }

    /**
     * Returns the {@code Stage} matching the given string, ignoring case and surrounding whitespace.
     *
     * @param stage A valid stage.
     */
    public static Stage fromString(String stage) {
        requireNonNull(stage);
        String trimmed = stage.trim();
        return Arrays.stream(values()).filter(value -> value.name().equalsIgnoreCase(trimmed)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(MESSAGE_CONSTRAINTS));
    }

    /**
     * Returns the stage with a capital first letter, e.g. {@code Cultivating}.
     */
    @Override
    public String toString() {
        String lowerCase = name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lowerCase.charAt(0)) + lowerCase.substring(1);
    }
}
