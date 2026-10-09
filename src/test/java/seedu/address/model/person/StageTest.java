package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class StageTest {

    @Test
    public void fromString_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> Stage.fromString(null));
    }

    @Test
    public void fromString_invalidStage_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Stage.fromString("gave"));
    }

    @Test
    public void isValidStage() {
        // null stage
        assertThrows(NullPointerException.class, () -> Stage.isValidStage(null));

        // invalid stages
        assertFalse(Stage.isValidStage("")); // empty string
        assertFalse(Stage.isValidStage(" ")); // spaces only
        assertFalse(Stage.isValidStage("givng")); // typo
        assertFalse(Stage.isValidStage("cult")); // abbreviation
        assertFalse(Stage.isValidStage("giving lapsed")); // two stages
        assertFalse(Stage.isValidStage("DEFAULT_STAGE")); // not a stage name

        // valid stages
        assertTrue(Stage.isValidStage("prospect"));
        assertTrue(Stage.isValidStage("contacted"));
        assertTrue(Stage.isValidStage("cultivating"));
        assertTrue(Stage.isValidStage("giving"));
        assertTrue(Stage.isValidStage("lapsed"));
        assertTrue(Stage.isValidStage("declined"));
        assertTrue(Stage.isValidStage("GIVING")); // upper case
        assertTrue(Stage.isValidStage("CuLtIvAtInG")); // mixed case
        assertTrue(Stage.isValidStage("  lapsed  ")); // surrounding spaces
    }

    @Test
    public void fromString_validStage_returnsStage() {
        assertEquals(Stage.PROSPECT, Stage.fromString("prospect"));
        assertEquals(Stage.CONTACTED, Stage.fromString("Contacted"));
        assertEquals(Stage.CULTIVATING, Stage.fromString("CULTIVATING"));
        assertEquals(Stage.GIVING, Stage.fromString(" giving "));
        assertEquals(Stage.LAPSED, Stage.fromString("lApSeD"));
        assertEquals(Stage.DECLINED, Stage.fromString("declined"));
    }

    @Test
    public void defaultStage_isProspect() {
        assertEquals(Stage.PROSPECT, Stage.DEFAULT_STAGE);
    }

    @Test
    public void toStringMethod() {
        assertEquals("Prospect", Stage.PROSPECT.toString());
        assertEquals("Contacted", Stage.CONTACTED.toString());
        assertEquals("Cultivating", Stage.CULTIVATING.toString());
        assertEquals("Giving", Stage.GIVING.toString());
        assertEquals("Lapsed", Stage.LAPSED.toString());
        assertEquals("Declined", Stage.DECLINED.toString());
    }

    @Test
    public void toString_roundTripsThroughFromString() {
        for (Stage stage : Stage.values()) {
            assertEquals(stage, Stage.fromString(stage.toString()));
        }
    }
}
