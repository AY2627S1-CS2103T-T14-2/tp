package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.storage.JsonAdaptedInteraction.MESSAGE_DATE_TOO_FAR_IN_FUTURE;
import static seedu.address.storage.JsonAdaptedInteraction.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalInteractionNotes.ASCII_AND_EMOJI_AT_NOTE_LIMIT;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Interaction;

public class JsonAdaptedInteractionTest {

    private static final String VALID_DATE = "2026-09-16";
    // today is 20 October locally, although the UTC date is still 19 October
    private static final Clock DATE_CLOCK = Clock.fixed(Instant.parse("2026-10-19T16:30:00Z"),
            ZoneId.of("Asia/Singapore"));
    private static final String VALID_NOTE = "Called to discuss the year-end appeal";

    @Test
    public void toModelType_unicodeBlankNote_throwsIllegalValueException() {
        for (String note : List.of("\u0085", "\u2003", "\u00a0", " \t\u0085\u2003\u00a0 ")) {
            JsonAdaptedInteraction interaction = new JsonAdaptedInteraction(VALID_DATE, note);
            assertThrows(IllegalValueException.class, Interaction.MESSAGE_CONSTRAINTS, interaction::toModelType);
        }
    }

    @Test
    public void toModelType_unicodePaddedNote_trimsNote() throws Exception {
        JsonAdaptedInteraction interaction = new JsonAdaptedInteraction(VALID_DATE, "\u2003" + VALID_NOTE + "\u00a0");
        assertEquals(VALID_NOTE, interaction.toModelType().getNote());
    }

    @Test
    public void toModelType_supplementaryCharactersAtLengthBoundary_checksCodePoints() throws Exception {
        // 499 ASCII characters + one emoji: 500 code points, but 501 UTF-16 code units
        String note = ASCII_AND_EMOJI_AT_NOTE_LIMIT;
        Interaction interaction = new JsonAdaptedInteraction(VALID_DATE, note).toModelType();
        assertEquals(note, interaction.getNote());
        assertEquals(interaction, new JsonAdaptedInteraction(interaction).toModelType());
        JsonAdaptedInteraction tooLong = new JsonAdaptedInteraction(VALID_DATE, note + "a");
        assertThrows(IllegalValueException.class, Interaction.MESSAGE_CONSTRAINTS, tooLong::toModelType);
    }

    @Test
    public void toModelType_validInteractionDetails_returnsInteraction() throws Exception {
        Interaction interaction = new Interaction(LocalDate.parse(VALID_DATE), VALID_NOTE);
        JsonAdaptedInteraction adaptedInteraction = new JsonAdaptedInteraction(interaction);
        assertEquals(interaction, adaptedInteraction.toModelType());
    }

    @Test
    public void toModelType_invalidDate_throwsIllegalValueException() {
        JsonAdaptedInteraction interaction = new JsonAdaptedInteraction("16-09-2026", VALID_NOTE);
        assertThrows(IllegalValueException.class, Interaction.MESSAGE_INVALID_DATE_FORMAT, () -> {
            try {
                interaction.toModelType();
            } catch (IllegalValueException exception) {
                DateTimeParseException cause = assertInstanceOf(DateTimeParseException.class, exception.getCause());
                assertEquals("16-09-2026", cause.getParsedString());
                throw exception;
            }
        });
    }

    @Test
    public void toModelType_nullDate_throwsIllegalValueException() {
        JsonAdaptedInteraction interaction = new JsonAdaptedInteraction(null, VALID_NOTE);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "date");
        assertThrows(IllegalValueException.class, expectedMessage, interaction::toModelType);
    }

    @Test
    public void toModelType_dateRangeBoundaries_returnsInteraction() throws Exception {
        // loading accepts today and the one-day allowance, as well as the lower boundary
        for (String date : List.of("1900-01-01", "2026-10-20", "2026-10-21")) {
            Interaction restored = new JsonAdaptedInteraction(date, VALID_NOTE).toModelType(DATE_CLOCK);
            assertEquals(LocalDate.parse(date), restored.getDate());
            assertEquals(VALID_NOTE, restored.getNote());
        }
    }

    @Test
    public void toModelType_timeZoneMovesDateBackOneDay_preservesInteraction() throws Exception {
        Interaction original = new Interaction(LocalDate.now(DATE_CLOCK), VALID_NOTE);
        JsonAdaptedInteraction saved = new JsonAdaptedInteraction(original);
        Clock loadingClock = DATE_CLOCK.withZone(ZoneId.of("America/Los_Angeles"));

        // the saved date is tomorrow in the loading zone, but must be preserved without adjustment
        assertEquals(LocalDate.now(loadingClock).plusDays(1), original.getDate());
        assertEquals(original, saved.toModelType(loadingClock));
    }

    @Test
    public void toModelType_outOfRangeDates_throwsSpecificIllegalValueException() {
        // each invalid date is paired with a valid note so only the date causes rejection
        assertThrows(IllegalValueException.class, Interaction.MESSAGE_EARLY_DATE, () ->
                new JsonAdaptedInteraction("1899-12-31", VALID_NOTE).toModelType(DATE_CLOCK));
        assertThrows(IllegalValueException.class, MESSAGE_DATE_TOO_FAR_IN_FUTURE, () ->
                new JsonAdaptedInteraction("2026-10-22", VALID_NOTE).toModelType(DATE_CLOCK));
    }

    @Test
    public void toModelType_nullClock_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new JsonAdaptedInteraction(VALID_DATE, VALID_NOTE).toModelType(null));
    }

    @Test
    public void toModelType_invalidNote_throwsIllegalValueException() {
        JsonAdaptedInteraction interaction = new JsonAdaptedInteraction(VALID_DATE, " ");
        assertThrows(IllegalValueException.class, Interaction.MESSAGE_CONSTRAINTS, interaction::toModelType);
    }

    @Test
    public void toModelType_nullNote_throwsIllegalValueException() {
        JsonAdaptedInteraction interaction = new JsonAdaptedInteraction(VALID_DATE, null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "note");
        assertThrows(IllegalValueException.class, expectedMessage, interaction::toModelType);
    }
}
