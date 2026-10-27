package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.Objects;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a dated interaction with a supporter.
 * Guarantees: immutable; the note is valid as declared in {@link #isValidNote(String)}.
 */
public class Interaction {

    public static final int MAX_NOTE_LENGTH = 500;
    public static final LocalDate MIN_DATE = LocalDate.of(1900, 1, 1);

    public static final String MESSAGE_INVALID_DATE_FORMAT =
            "Interaction dates must be valid dates in yyyy-MM-dd format, e.g. 2026-10-01.";
    public static final String MESSAGE_EARLY_DATE = "Interaction dates must be on or after " + MIN_DATE + ".";
    public static final String MESSAGE_FUTURE_DATE = "Interaction dates cannot be in the future.";

    public static final String MESSAGE_CONSTRAINTS =
            "Interaction notes should not be blank and should be at most " + MAX_NOTE_LENGTH
                    + " characters long, excluding spaces at the beginning and end.";

    private final LocalDate date;
    private final String note;

    /**
     * Creates an {@code Interaction} on the given {@code date}, removing leading and trailing spaces from its note.
     */
    public Interaction(LocalDate date, String note) {
        requireAllNonNull(date, note);
        checkArgument(isValidNote(note), MESSAGE_CONSTRAINTS);
        this.date = date;
        this.note = StringUtil.trimUnicodeWhitespace(note);
    }

    /**
     * Returns true if the note is not blank and is at most {@link #MAX_NOTE_LENGTH} Unicode code points after trimming.
     */
    public static boolean isValidNote(String test) {
        requireNonNull(test);
        String trimmed = StringUtil.trimUnicodeWhitespace(test);
        return !trimmed.isEmpty() && trimmed.codePointCount(0, trimmed.length()) <= MAX_NOTE_LENGTH;
    }

    /**
     * Returns true if {@code date} is from {@link #MIN_DATE} through {@code today}, inclusive.
     * The caller supplies today's local date so validation can use a controlled clock in tests.
     */
    public static boolean isValidDate(LocalDate date, LocalDate today) {
        requireAllNonNull(date, today);
        return !date.isBefore(MIN_DATE) && !date.isAfter(today);
    }

    public LocalDate getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Interaction otherInteraction)) {
            return false;
        }

        return date.equals(otherInteraction.date)
                && note.equals(otherInteraction.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, note);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("date", date)
                .add("note", note)
                .toString();
    }
}
