package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalInteractionNotes.ASCII_AND_EMOJI_AT_NOTE_LIMIT;
import static seedu.address.testutil.TypicalInteractionNotes.JOINED_EMOJI;
import static seedu.address.testutil.TypicalInteractionNotes.NOTE_CODE_POINT_LIMIT;
import static seedu.address.testutil.TypicalInteractionNotes.UNICODE_BLANK_NOTES;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests interaction validation, note preservation and value equality.
 */
public class InteractionTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 16);

    @Test
    public void constructor_unicodeBlankNote_throwsIllegalArgumentException() {
        for (String note : UNICODE_BLANK_NOTES) {
            assertFalse(Interaction.isValidNote(note));
            assertThrows(IllegalArgumentException.class, Interaction.MESSAGE_CONSTRAINTS, () ->
                    new Interaction(DATE, note));
        }
    }

    @Test
    public void constructor_unicodePaddedNote_preservesContent() {
        String note = "Called  about\u00a0the\u2003appeal \ud83d\ude00";
        assertEquals(note, new Interaction(DATE, "\u2003\u00a0" + note + "\u202f\u3000").getNote());
    }

    @Test
    public void constructor_unicodePaddingAtLengthLimit_acceptsTrimmedNote() {
        String note = "a".repeat(Interaction.MAX_NOTE_LENGTH);
        assertEquals(note, new Interaction(DATE, "\u2003" + note + "\u00a0").getNote());
        assertFalse(Interaction.isValidNote("\u2003" + note + "a\u00a0"));
    }

    @Test
    public void constructor_joinedEmojiAtLengthBoundary_countsJoiner() {
        // woman + zero-width joiner + laptop occupy three code points in one joined emoji
        int joinedEmojiCodePointCount = 3;
        String note = "a".repeat(NOTE_CODE_POINT_LIMIT - joinedEmojiCodePointCount) + JOINED_EMOJI;

        assertTrue(Interaction.isValidNote(note));
        assertEquals(note, new Interaction(DATE, note).getNote());
        assertFalse(Interaction.isValidNote("a" + note));
        assertThrows(IllegalArgumentException.class, Interaction.MESSAGE_CONSTRAINTS, () ->
                new Interaction(DATE, "a" + note));
    }

    @Test
    public void constructor_codePointLengthBoundary_acceptsLimitAndRejectsOverflow() {
        String emojiOnlyNote = "\ud83d\ude00".repeat(NOTE_CODE_POINT_LIMIT);
        // e + combining acute accent occupy two code points per repetition
        int combiningAccentCodePointCount = 2;
        String combiningAccentNote = "e\u0301".repeat(NOTE_CODE_POINT_LIMIT / combiningAccentCodePointCount);

        for (String note : List.of(ASCII_AND_EMOJI_AT_NOTE_LIMIT, emojiOnlyNote, combiningAccentNote)) {
            assertTrue(Interaction.isValidNote(note));
            assertEquals(note, new Interaction(DATE, " " + note + " ").getNote());
            assertFalse(Interaction.isValidNote(note + "a"));
            assertThrows(IllegalArgumentException.class, Interaction.MESSAGE_CONSTRAINTS, () ->
                    new Interaction(DATE, " " + note + "a "));
        }
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Interaction(null, "Called"));
        assertThrows(NullPointerException.class, () -> new Interaction(DATE, null));
    }

    @Test
    public void constructor_invalidNote_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Interaction.MESSAGE_CONSTRAINTS, () ->
                new Interaction(DATE, ""));
        assertThrows(IllegalArgumentException.class, Interaction.MESSAGE_CONSTRAINTS, () ->
                new Interaction(DATE, "   "));
        assertThrows(IllegalArgumentException.class, Interaction.MESSAGE_CONSTRAINTS, () ->
                new Interaction(DATE, "a".repeat(Interaction.MAX_NOTE_LENGTH + 1)));
    }

    @Test
    public void constructor_surroundingSpaces_noteTrimmed() {
        Interaction interaction = new Interaction(DATE, "  Discussed year-end appeal  ");
        assertEquals("Discussed year-end appeal", interaction.getNote());
    }

    @Test
    public void isValidNote() {
        assertThrows(NullPointerException.class, () -> Interaction.isValidNote(null));

        assertFalse(Interaction.isValidNote(""));
        assertFalse(Interaction.isValidNote("   "));
        assertFalse(Interaction.isValidNote("a".repeat(Interaction.MAX_NOTE_LENGTH + 1)));

        assertTrue(Interaction.isValidNote("Called"));
        assertTrue(Interaction.isValidNote("Coffee at Raffles Place; keen on Q4."));
        assertTrue(Interaction.isValidNote("a".repeat(Interaction.MAX_NOTE_LENGTH)));
        assertTrue(Interaction.isValidNote(" " + "a".repeat(Interaction.MAX_NOTE_LENGTH) + " "));
    }

    @Test
    public void isValidDate_withinRange_returnsTrue() {
        // include both limits, their inner neighbors and an ordinary historical date
        for (LocalDate date : List.of(LocalDate.of(1900, 1, 1), LocalDate.of(1900, 1, 2),
                LocalDate.of(2024, 2, 29), DATE.minusDays(1), DATE)) {
            assertTrue(Interaction.isValidDate(date, DATE), date.toString());
        }
    }

    @Test
    public void isValidDate_outsideRange_returnsFalse() {
        // test just outside each limit with an otherwise valid calendar date
        assertFalse(Interaction.isValidDate(LocalDate.of(1899, 12, 31), DATE));
        assertFalse(Interaction.isValidDate(DATE.plusDays(1), DATE));
    }

    @Test
    public void isValidDate_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Interaction.isValidDate(null, DATE));
        assertThrows(NullPointerException.class, () -> Interaction.isValidDate(DATE, null));
    }

    @Test
    public void equals() {
        Interaction interaction = new Interaction(DATE, "Called");

        assertTrue(interaction.equals(new Interaction(DATE, "Called")));
        assertTrue(interaction.equals(interaction));
        assertFalse(interaction.equals(null));
        assertFalse(interaction.equals(5));
        assertFalse(interaction.equals(new Interaction(DATE.plusDays(1), "Called")));
        assertFalse(interaction.equals(new Interaction(DATE, "Emailed")));
    }

    @Test
    public void hashCode_equalInteractions_sameHashCode() {
        assertEquals(new Interaction(DATE, "Called").hashCode(),
                new Interaction(DATE, "Called").hashCode());
    }

    @Test
    public void toStringMethod() {
        Interaction interaction = new Interaction(DATE, "Called");
        String expected = Interaction.class.getCanonicalName() + "{date=2026-09-16, note=Called}";
        assertEquals(expected, interaction.toString());
    }
}
