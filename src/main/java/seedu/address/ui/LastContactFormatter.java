package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/**
 * Formats the last-contact line shared by supporter list cards and the detail panel.
 */
public final class LastContactFormatter {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.ENGLISH);

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
        return lastInteractionDate.map(date -> "Last contact: " + date.format(DATE_FORMATTER))
                .orElse("No interactions yet");
    }
}
