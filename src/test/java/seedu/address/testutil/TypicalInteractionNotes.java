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

    private TypicalInteractionNotes() {
    }
}
