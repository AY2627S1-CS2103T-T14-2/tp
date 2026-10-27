package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.storage.JsonAdaptedInteraction.DATE_MESSAGE_CONSTRAINTS;
import static seedu.address.storage.JsonAdaptedInteraction.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Interaction;

public class JsonAdaptedInteractionTest {

    private static final String VALID_DATE = "2026-09-16";
    private static final String VALID_NOTE = "Called to discuss the year-end appeal";

    @Test
    public void toModelType_unicodeBlankNote_throwsIllegalValueException() {
        for (String note : List.of("\u2003", "\u00a0", " \t\u2003\u00a0 ")) {
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
    public void toModelType_validInteractionDetails_returnsInteraction() throws Exception {
        Interaction interaction = new Interaction(LocalDate.parse(VALID_DATE), VALID_NOTE);
        JsonAdaptedInteraction adaptedInteraction = new JsonAdaptedInteraction(interaction);
        assertEquals(interaction, adaptedInteraction.toModelType());
    }

    @Test
    public void toModelType_invalidDate_throwsIllegalValueException() {
        JsonAdaptedInteraction interaction = new JsonAdaptedInteraction("16-09-2026", VALID_NOTE);
        assertThrows(IllegalValueException.class, DATE_MESSAGE_CONSTRAINTS, () -> {
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
