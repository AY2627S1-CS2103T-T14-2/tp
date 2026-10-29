package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.IndexTestUtil.getInvalidIndexInputs;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteCommand;

/**
 * As we are only doing white-box testing, our test cases do not cover path variations
 * outside of the DeleteCommand code. For example, inputs "1" and "1 abc" take the
 * same path through the DeleteCommand, and therefore we test only one of them.
 * The path variation for those two cases occurs inside the ParserUtil, and
 * therefore should be covered by the ParserUtilTest.
 */
public class DeleteCommandParserTest {

    private static final String MESSAGE_INVALID_INDEX =
            ParserUtil.MESSAGE_INVALID_INDEX + "\n" + DeleteCommand.MESSAGE_USAGE;

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_invalidIndexValues_reportsIndexErrorAndUsage() {
        for (String invalidIndex : getInvalidIndexInputs("abc")) {
            assertParseFailure(parser, invalidIndex, MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_malformedArguments_reportsFormatAndUsage() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);
        for (String input : new String[]{"", "  ", "1 2", "1 extra"}) {
            assertParseFailure(parser, input, expectedMessage);
        }
    }

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, "1", new DeleteCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, "a", MESSAGE_INVALID_INDEX);
    }
}
