package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.LogCommand;
import seedu.address.model.person.Interaction;

public class LogCommandParserTest {

    private static final String VALID_NOTE = "Called to discuss the year-end appeal";
    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, LogCommand.MESSAGE_USAGE);

    private static final String MESSAGE_INVALID_INDEX =
            ParserUtil.MESSAGE_INVALID_INDEX + "\n" + LogCommand.MESSAGE_USAGE;

    private LogCommandParser parser = new LogCommandParser();

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
        // not a number
        assertParseFailure(parser, " x nt/" + VALID_NOTE, MESSAGE_INVALID_INDEX);

        // zero
        assertParseFailure(parser, " 0 nt/" + VALID_NOTE, MESSAGE_INVALID_INDEX);

        // negative
        assertParseFailure(parser, " -1 nt/" + VALID_NOTE, MESSAGE_INVALID_INDEX);

        // signed
        assertParseFailure(parser, " +1 nt/" + VALID_NOTE, MESSAGE_INVALID_INDEX);

        // decimal
        assertParseFailure(parser, " 1.5 nt/" + VALID_NOTE, MESSAGE_INVALID_INDEX);

        // larger than the maximum int
        assertParseFailure(parser, " 2147483648 nt/" + VALID_NOTE, MESSAGE_INVALID_INDEX);
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
        assertParseFailure(parser, " 1 extra dt/2024-02-29 nt/" + VALID_NOTE, MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_invalidDate_failure() {
        for (String date : List.of("", "   ", "2024-2-29", "2024/02/29", "2024-02-30", "2023-02-29")) {
            assertParseFailure(parser, " 1 nt/" + VALID_NOTE + " dt/" + date,
                    ParserUtil.MESSAGE_INVALID_INTERACTION_DATE);
        }
    }

    @Test
    public void parse_futureDate_failure() {
        assertParseFailure(parser, " 1 nt/" + VALID_NOTE + " dt/" + LocalDate.now().plusYears(1),
                ParserUtil.MESSAGE_FUTURE_INTERACTION_DATE);
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
        // Index errors take precedence over duplicate prefixes and invalid parameter values.
        assertParseFailure(parser, " x nt/ dt/invalid dt/invalid", MESSAGE_INVALID_INDEX);
        // Duplicate prefixes take precedence over note/date validation.
        assertParseFailure(parser, " 1 nt/ dt/invalid dt/invalid",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DATE));
        // Note validation precedes date validation regardless of the order typed.
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
                ParserUtil.MESSAGE_EARLY_INTERACTION_DATE);
    }

    @Test
    public void parse_minimumDate_returnsDatedLogCommand() {
        assertParseSuccess(parser, " 1 nt/" + VALID_NOTE + " dt/1900-01-01",
                new LogCommand(INDEX_FIRST_PERSON, VALID_NOTE, Optional.of(LocalDate.of(1900, 1, 1))));
    }

}
