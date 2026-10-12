package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedInteraction.DATE_MESSAGE_CONSTRAINTS;
import static seedu.address.storage.JsonAdaptedInteraction.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Interaction;

public class JsonAdaptedInteractionTest {

    private static final String VALID_DATE = "2026-09-16";
    private static final String VALID_NOTE = "Called to discuss the year-end appeal";

    @Test
    public void toModelType_validInteractionDetails_returnsInteraction() throws Exception {
        Interaction interaction = new Interaction(LocalDate.parse(VALID_DATE), VALID_NOTE);
        JsonAdaptedInteraction adaptedInteraction = new JsonAdaptedInteraction(interaction);
        assertEquals(interaction, adaptedInteraction.toModelType());
    }

    @Test
    public void toModelType_invalidDate_throwsIllegalValueException() {
        JsonAdaptedInteraction interaction = new JsonAdaptedInteraction("16-09-2026", VALID_NOTE);
        assertThrows(IllegalValueException.class, DATE_MESSAGE_CONSTRAINTS, interaction::toModelType);
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
