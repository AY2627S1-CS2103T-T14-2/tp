package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.UnlogCommand;

/**
 * Tests argument parsing and command-specific error messages for {@code UnlogCommandParser}.
 */
public class UnlogCommandParserTest {

    private final UnlogCommandParser parser = new UnlogCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_validArgs_returnsUnlogCommand() {
        // use different indices to catch accidentally swapping the supporter and interaction indices
        assertParseSuccess(parser, "1 2", new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(2)));
    }

    @Test
    public void parse_extraWhitespace_returnsUnlogCommand() {
        assertParseSuccess(parser, "  1 \t 2  ", new UnlogCommand(INDEX_FIRST_PERSON, Index.fromOneBased(2)));
    }

    @Test
    public void parse_missingOrExtraArguments_throwsParseException() {
        String expectedMessage = ParserUtil.MESSAGE_INVALID_INTERACTION_INDICES + "\n" + UnlogCommand.MESSAGE_USAGE;
        for (String args : new String[]{"", "  ", "1", "1 2 3", "1 2 nt/Note"}) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }

    @Test
    public void parse_invalidIndexInEitherPosition_throwsParseException() {
        String expectedMessage = ParserUtil.MESSAGE_INVALID_INTERACTION_INDICES + "\n" + UnlogCommand.MESSAGE_USAGE;
        // the shared helper validates the numbers; this parser must report unlog's usage for either failure
        for (String invalidIndex : new String[]{"0", "-1", "+1", "abc", "1.5", "2147483648"}) {
            assertParseFailure(parser, invalidIndex + " 2", expectedMessage);
            assertParseFailure(parser, "1 " + invalidIndex, expectedMessage);
        }
    }
}
