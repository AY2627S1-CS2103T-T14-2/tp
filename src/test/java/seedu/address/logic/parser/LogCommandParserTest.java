package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.IndexTestUtil.getInvalidIndexInputs;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalInteractionDates.INVALID_DATE_INPUTS;
import static seedu.address.testutil.TypicalInteractionNotes.ASCII_AND_EMOJI_AT_NOTE_LIMIT;
import static seedu.address.testutil.TypicalInteractionNotes.CHARACTER_PRESERVATION_NOTES;
import static seedu.address.testutil.TypicalInteractionNotes.UNICODE_BLANK_NOTES;
import static seedu.address.testutil.TypicalInteractionNotes.VALID_NOTE;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.LogCommand;
import seedu.address.model.person.Interaction;

/**
 * Tests log arguments, note preservation and validation error precedence.
 */
public class LogCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, LogCommand.MESSAGE_USAGE);

    private static final String MESSAGE_INVALID_INDEX =
            ParserUtil.MESSAGE_INVALID_INDEX + "\n" + LogCommand.MESSAGE_USAGE;

    private final LogCommandParser parser = new LogCommandParser();

    @Test
    public void parse_unicodeBlankNote_failure() {
        for (String note : UNICODE_BLANK_NOTES) {
            assertParseFailure(parser, " 1 nt/" + note, Interaction.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_unicodePaddedNote_preservesInternalSpacing() {
        String note = "Called  about\u00a0the\u2003appeal";
        assertParseSuccess(parser, " 1 nt/\u2003\u00a0" + note + "\u202f\u3000",
                new LogCommand(INDEX_FIRST_PERSON, note));
    }

    @Test
    public void parse_specialCharacters_preservesNote() {
        for (String note : CHARACTER_PRESERVATION_NOTES) {
            assertParseSuccess(parser, " 1 nt/" + note,
                    new LogCommand(INDEX_FIRST_PERSON, note));
        }
    }

    @Test
    public void parse_supplementaryCharactersAtLengthBoundary_checksCodePoints() {
        String note = ASCII_AND_EMOJI_AT_NOTE_LIMIT;

        // surrounding spaces are trimmed before checking the code-point limit
        assertParseSuccess(parser, " 1 nt/ " + note + " ", new LogCommand(INDEX_FIRST_PERSON, note));

        assertParseFailure(parser, " 1 nt/ " + note + "a ", Interaction.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_validArgs_returnsLogCommand() {
        assertParseSuccess(parser, " 1 nt/" + VALID_NOTE, new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE));

        // leading and trailing whitespace around the note is removed
        assertParseSuccess(parser, " 2 nt/  " + VALID_NOTE + "  ",
                new LogCommand(INDEX_SECOND_PERSON, VALID_NOTE));

        // leading zeros in index
        assertParseSuccess(parser, " 02 nt/" + VALID_NOTE, new LogCommand(INDEX_SECOND_PERSON, VALID_NOTE));
    }

    @Test
    public void parse_missingParts_failure() {
        // nothing given
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "   ", MESSAGE_INVALID_FORMAT);

        // note prefix missing
        assertParseFailure(parser, " 1 " + VALID_NOTE, MESSAGE_INVALID_FORMAT);

        // index missing
        assertParseFailure(parser, " nt/" + VALID_NOTE, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndex_failure() {
        for (String invalidIndex : getInvalidIndexInputs("x")) {
            assertParseFailure(parser, " " + invalidIndex + " nt/" + VALID_NOTE, MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_invalidNote_failure() {
        // empty note
        assertParseFailure(parser, " 1 nt/", Interaction.MESSAGE_CONSTRAINTS);

        // blank note
        assertParseFailure(parser, " 1 nt/   ", Interaction.MESSAGE_CONSTRAINTS);

        // note exceeds the maximum length
        assertParseFailure(parser, " 1 nt/" + "a".repeat(Interaction.MAX_NOTE_LENGTH + 1),
                Interaction.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedNotePrefix_failure() {
        String userInput = " 1 nt/Called nt/Emailed";
        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NOTE));
    }

    @Test
    public void parse_explicitDate_returnsDatedLogCommand() {
        LogCommand expected = new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE,
                Optional.of(LocalDate.of(2024, 2, 29)));
        assertParseSuccess(parser, " 1 nt/" + VALID_NOTE + " dt/2024-02-29", expected);
        assertParseSuccess(parser, " 1 dt/2024-02-29 nt/" + VALID_NOTE, expected);
        assertParseSuccess(parser, " 1 dt/  2024-02-29  nt/  " + VALID_NOTE + "  ", expected);
    }

    @Test
    public void parse_dateWithoutRequiredParts_failure() {
        assertParseFailure(parser, " 1 dt/2024-02-29", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " dt/2024-02-29 nt/" + VALID_NOTE, MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 1 extra dt/2024-02-29 nt/" + VALID_NOTE, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidDate_failure() {
        for (String date : INVALID_DATE_INPUTS) {
            assertParseFailure(parser, " 1 nt/" + VALID_NOTE + " dt/" + date,
                    Interaction.MESSAGE_INVALID_DATE_FORMAT);
        }
    }

    @Test
    public void parse_futureDate_failure() {
        assertParseFailure(parser, " 1 nt/" + VALID_NOTE + " dt/" + LocalDate.now().plusYears(1),
                Interaction.MESSAGE_FUTURE_DATE);
    }

    @Test
    public void parse_repeatedDatePrefix_failure() {
        assertParseFailure(parser, " 1 nt/" + VALID_NOTE + " dt/2024-02-29 dt/2024-02-29",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DATE));
        assertParseFailure(parser, " 1 nt/Called nt/Emailed dt/2024-02-29 dt/2024-02-28",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NOTE, PREFIX_DATE));
    }

    @Test
    public void parse_multipleErrors_reportsInValidationOrder() {
        // index errors take precedence over duplicate prefixes and invalid parameter values
        assertParseFailure(parser, " x nt/ dt/invalid dt/invalid", MESSAGE_INVALID_INDEX);

        // duplicate prefixes take precedence over note/date validation
        assertParseFailure(parser, " 1 nt/ dt/invalid dt/invalid",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DATE));

        // note validation precedes date validation regardless of the order typed
        assertParseFailure(parser, " 1 nt/ dt/invalid", Interaction.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " 1 dt/invalid nt/", Interaction.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unrecognisedPrefix_keptInNote() {
        String note = "Discussed d/o and x/details";
        assertParseSuccess(parser, " 1 nt/" + note, new LogCommand(INDEX_FIRST_PERSON, note));
    }

    @Test
    public void parse_dateBeforeMinimum_failure() {
        assertParseFailure(parser, " 1 nt/" + VALID_NOTE + " dt/1899-12-31",
                Interaction.MESSAGE_EARLY_DATE);
    }

    @Test
    public void parse_minimumDate_returnsDatedLogCommand() {
        assertParseSuccess(parser, " 1 nt/" + VALID_NOTE + " dt/1900-01-01",
                new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, Optional.of(LocalDate.of(1900, 1, 1))));
    }

}
