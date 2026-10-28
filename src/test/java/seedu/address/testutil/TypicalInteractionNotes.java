package seedu.address.testutil;

import java.util.List;

/**
 * Representative note text for checking character preservation across parsing and storage.
 */
public class TypicalInteractionNotes {
    // woman + zero-width joiner + laptop (👩‍💻): three code points in one joined emoji
    public static final String JOINED_EMOJI = "\ud83d\udc69\u200d\ud83d\udcbb";
    public static final List<String> CHARACTER_PRESERVATION_NOTES = List.of(
            "Thanks! Pledged $50 (monthly); follow-up: Friday, 3pm? #appeal & friends.",
            // Quotes and backslashes require escaping when written to JSON.
            "Said \"yes\"; file C:\\notes\\appeal",
            "已联系，讨论捐款",
            "Discussed volunteering " + JOINED_EMOJI);

    /**
     * Notes whose endings must be preserved when included in command feedback.
     */
    public static final List<String> NOTES_WITH_DIFFERENT_ENDINGS = List.of(
            "Called about the gala.",
            "Will she give?",
            "Called about the gala");

    // both notes contain exactly 500 code points, but exceed 500 UTF-16 code units
    public static final String ASCII_AND_EMOJI_AT_NOTE_LIMIT = "a".repeat(499) + "\ud83d\ude00";
    public static final String EMOJI_ONLY_AT_NOTE_LIMIT = "\ud83d\ude00".repeat(500);
    public static final List<String> SUPPLEMENTARY_NOTES_AT_LIMIT = List.of(
            ASCII_AND_EMOJI_AT_NOTE_LIMIT, EMOJI_ONLY_AT_NOTE_LIMIT);

    private TypicalInteractionNotes() {
    }
}
