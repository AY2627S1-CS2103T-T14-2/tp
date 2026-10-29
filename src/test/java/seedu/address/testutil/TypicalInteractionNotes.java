package seedu.address.testutil;

import java.util.List;

/**
 * Representative note text for checking validation and character preservation across application layers.
 */
public final class TypicalInteractionNotes {

    // keep the required boundary independent of the production limit
    public static final int NOTE_CODE_POINT_LIMIT = 500;

    public static final String VALID_NOTE = "Called to discuss the year-end appeal";
    public static final List<String> UNICODE_BLANK_NOTES = List.of(
            "\u0085", "\u2003", "\u00a0", " \t\u0085\u2003\u00a0\u202f\u3000 ");
    public static final List<String> NOTE_PADDING_SEQUENCES = List.of(" \t", "\u00a0", "\u2003", "\u0085");

    // woman + zero-width joiner + laptop (👩‍💻): three code points in one joined emoji
    public static final String JOINED_EMOJI = "\ud83d\udc69\u200d\ud83d\udcbb";
    public static final List<String> CHARACTER_PRESERVATION_NOTES = List.of(
            "Thanks! Pledged $50 (monthly); follow-up: Friday, 3pm? #appeal & friends.",
            // quotes and backslashes require escaping when written to JSON
            "Said \"yes\"; file C:\\notes\\appeal",
            "已联系，讨论捐款",
            "Discussed volunteering " + JOINED_EMOJI);

    // this note contains exactly 500 code points, but exceeds 500 UTF-16 code units
    public static final String ASCII_AND_EMOJI_AT_NOTE_LIMIT =
            "a".repeat(NOTE_CODE_POINT_LIMIT - 1) + "\ud83d\ude00";

    private TypicalInteractionNotes() {
    }
}
