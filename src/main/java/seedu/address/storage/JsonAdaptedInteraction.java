package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Interaction;

/**
 * Jackson-friendly version of {@link Interaction}.
 */
class JsonAdaptedInteraction {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Interaction's %s field is missing!";
    public static final String DATE_MESSAGE_CONSTRAINTS = "Interaction dates should be in yyyy-MM-dd format";

    private final String date;
    private final String note;

    /**
     * Constructs a {@code JsonAdaptedInteraction} with the given details.
     */
    @JsonCreator
    public JsonAdaptedInteraction(@JsonProperty("date") String date, @JsonProperty("note") String note) {
        this.date = date;
        this.note = note;
    }

    /**
     * Converts a given {@code Interaction} into this class for Jackson use.
     */
    public JsonAdaptedInteraction(Interaction source) {
        date = source.getDate().toString();
        note = source.getNote();
    }

    /**
     * Converts this Jackson-friendly adapted interaction into the model's {@code Interaction}.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted interaction.
     */
    public Interaction toModelType() throws IllegalValueException {
        return toModelType(Clock.systemDefaultZone());
    }

    /**
     * Converts this interaction using {@code clock} to determine today's local date.
     *
     * @throws IllegalValueException if any stored interaction constraint is violated.
     */
    Interaction toModelType(Clock clock) throws IllegalValueException {
        requireNonNull(clock);
        if (date == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "date"));
        }

        final LocalDate modelDate;
        try {
            modelDate = LocalDate.parse(date);
        } catch (DateTimeParseException exception) {
            throw new IllegalValueException(DATE_MESSAGE_CONSTRAINTS, exception);
        }

        if (!Interaction.isValidDate(modelDate, LocalDate.now(clock))) {
            throw new IllegalValueException(modelDate.isBefore(Interaction.MIN_DATE)
                    ? Interaction.MESSAGE_EARLY_DATE : Interaction.MESSAGE_FUTURE_DATE);
        }

        if (note == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "note"));
        }
        if (!Interaction.isValidNote(note)) {
            throw new IllegalValueException(Interaction.MESSAGE_CONSTRAINTS);
        }

        return new Interaction(modelDate, note);
    }
}
