package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ViewCommand;

/**
 * Tests parsing of the single displayed index required by {@code ViewCommand}.
 */
public class ViewCommandParserTest {

    private static final String INVALID_INDEX_MESSAGE =
            ParserUtil.MESSAGE_INVALID_INDEX + "\n" + ViewCommand.MESSAGE_USAGE;

    private static final String INVALID_FORMAT_MESSAGE =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);

    private final ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_validIndices_returnsViewCommand() {
        assertParseSuccess(parser, "1", new ViewCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "2", new ViewCommand(INDEX_SECOND_PERSON));
        assertParseSuccess(parser, Integer.toString(Integer.MAX_VALUE),
                new ViewCommand(Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_surroundingWhitespace_returnsViewCommand() {
        assertParseSuccess(parser, "  2  ", new ViewCommand(INDEX_SECOND_PERSON));
        assertParseSuccess(parser, "\t\r\n1\t\r\n", new ViewCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_missingIndex_throwsParseException() {
        assertParseFailure(parser, "", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, " \t\r\n ", INVALID_FORMAT_MESSAGE);
    }

    @Test
    public void parse_nonnumericIndex_throwsParseException() {
        assertParseFailure(parser, "abc", INVALID_INDEX_MESSAGE);
        assertParseFailure(parser, "1abc", INVALID_INDEX_MESSAGE);
    }

    @Test
    public void parse_nonAsciiIndex_throwsParseException() {
        assertParseFailure(parser, "١", INVALID_INDEX_MESSAGE);
    }

    @Test
    public void parse_zeroIndex_throwsParseException() {
        assertParseFailure(parser, "0", INVALID_INDEX_MESSAGE);
    }

    @Test
    public void parse_negativeIndex_throwsParseException() {
        assertParseFailure(parser, "-1", INVALID_INDEX_MESSAGE);
    }

    @Test
    public void parse_decimalIndex_throwsParseException() {
        assertParseFailure(parser, "1.5", INVALID_INDEX_MESSAGE);
        assertParseFailure(parser, "1.0", INVALID_INDEX_MESSAGE);
    }

    @Test
    public void parse_oversizedIndex_throwsParseException() {
        assertParseFailure(parser, "2147483648", INVALID_INDEX_MESSAGE);
        assertParseFailure(parser, "999999999999999999999", INVALID_INDEX_MESSAGE);
    }

    @Test
    public void parse_multipleIndices_throwsParseException() {
        assertParseFailure(parser, "1 2", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, "1\t2", INVALID_FORMAT_MESSAGE);
    }

    @Test
    public void parse_extraArguments_throwsParseException() {
        assertParseFailure(parser, "1 extra", INVALID_FORMAT_MESSAGE);
        assertParseFailure(parser, "1 nt/Note", INVALID_FORMAT_MESSAGE);
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
