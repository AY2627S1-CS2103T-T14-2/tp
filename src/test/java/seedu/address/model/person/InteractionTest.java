package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class InteractionTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 16);

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
