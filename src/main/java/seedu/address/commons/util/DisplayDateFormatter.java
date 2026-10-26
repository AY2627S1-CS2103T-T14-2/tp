package seedu.address.commons.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formats dates for display to the user, independently of input and storage formats.
 */
public final class DisplayDateFormatter {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.ENGLISH);

    private DisplayDateFormatter() {
    }

    /**
     * Returns the date formatted as {@code dd MMM uuuu} using English month abbreviations.
     */
    public static String format(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
}
