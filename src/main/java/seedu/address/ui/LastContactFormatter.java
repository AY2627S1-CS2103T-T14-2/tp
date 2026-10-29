package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.DisplayDateFormatter;

/**
 * Formats the last-contact line shared by supporter list cards and the detail panel.
 */
public final class LastContactFormatter {

    private static final Logger logger = LogsCenter.getLogger(LastContactFormatter.class);

    private LastContactFormatter() {
    }

    /**
     * Returns {@code Last contact: dd MMM uuuu} using English month abbreviations, or
     * {@code No interactions yet} when {@code lastInteractionDate} is empty.
     *
     * @throws NullPointerException if {@code lastInteractionDate} is null.
     */
    public static String format(Optional<LocalDate> lastInteractionDate) {
        requireNonNull(lastInteractionDate);
        if (lastInteractionDate.isEmpty()) {
            logger.fine("Formatting last contact without an interaction date");
            return "No interactions yet";
        }

        String formattedDate = DisplayDateFormatter.format(lastInteractionDate.orElseThrow());
        assert !formattedDate.isBlank() : "Last-contact date text must not be blank";
        logger.fine("Formatted last contact with an interaction date");
        return "Last contact: " + formattedDate;
    }
}
