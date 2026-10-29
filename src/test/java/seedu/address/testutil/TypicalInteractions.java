package seedu.address.testutil;

import java.time.LocalDate;

import seedu.address.model.person.Interaction;

/**
 * Representative interactions with distinct dates for testing history ordering.
 */
public final class TypicalInteractions {

    public static final Interaction OLDEST = new Interaction(LocalDate.of(2026, 9, 6), "Earlier call");
    public static final Interaction MIDDLE = new Interaction(LocalDate.of(2026, 9, 16), "Meeting");
    public static final Interaction NEWEST = new Interaction(LocalDate.of(2026, 9, 20), "Follow-up");

    private TypicalInteractions() {
    }
}
