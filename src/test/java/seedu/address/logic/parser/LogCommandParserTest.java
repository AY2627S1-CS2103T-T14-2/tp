package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.LogCommand;
import seedu.address.model.person.Interaction;

public class LogCommandParserTest {

    private static final String VALID_NOTE = "Called to discuss the year-end appeal";
    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, LogCommand.MESSAGE_USAGE);

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
        assertParseFailure(parser, " x nt/" + VALID_NOTE, MESSAGE_INVALID_FORMAT);

        // zero
        assertParseFailure(parser, " 0 nt/" + VALID_NOTE, MESSAGE_INVALID_FORMAT);

        // negative
        assertParseFailure(parser, " -1 nt/" + VALID_NOTE, MESSAGE_INVALID_FORMAT);

        // signed
        assertParseFailure(parser, " +1 nt/" + VALID_NOTE, MESSAGE_INVALID_FORMAT);

        // decimal
        assertParseFailure(parser, " 1.5 nt/" + VALID_NOTE, MESSAGE_INVALID_FORMAT);

        // larger than the maximum int
        assertParseFailure(parser, " 2147483648 nt/" + VALID_NOTE, MESSAGE_INVALID_FORMAT);
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
}
