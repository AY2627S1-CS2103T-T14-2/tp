package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a dated interaction with a supporter.
 * Guarantees: immutable; the note is valid as declared in {@link #isValidNote(String)}.
 */
public class Interaction {

    public static final int MAX_NOTE_LENGTH = 500;

    public static final String MESSAGE_CONSTRAINTS =
            "Interaction notes should not be blank and should be at most " + MAX_NOTE_LENGTH + " characters long";

    private final LocalDate date;
    private final String note;

    /**
     * Creates an {@code Interaction} on the given {@code date}, removing leading and trailing spaces from its note.
     */
    public Interaction(LocalDate date, String note) {
        requireNonNull(date);
        requireNonNull(note);
        checkArgument(isValidNote(note), MESSAGE_CONSTRAINTS);
        this.date = date;
        this.note = note.trim();
    }

    /**
     * Returns true if the note is not blank and is at most {@link #MAX_NOTE_LENGTH} characters after trimming.
     */
    public static boolean isValidNote(String test) {
        requireNonNull(test);
        String trimmed = test.trim();
        return !trimmed.isEmpty() && trimmed.length() <= MAX_NOTE_LENGTH;
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
