package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.IndexTestUtil.getInvalidIndexInputs;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.UnlogCommand;

/**
 * Tests argument parsing and command-specific error messages for {@code UnlogCommandParser}.
 */
public class UnlogCommandParserTest {

    private static final String MESSAGE_INVALID_INDICES =
            ParserUtil.MESSAGE_INVALID_INTERACTION_INDICES + "\n" + UnlogCommand.MESSAGE_USAGE;

    private final UnlogCommandParser parser = new UnlogCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_validArgs_returnsUnlogCommand() {
        // use different indices to catch accidentally swapping the supporter and interaction indices
        assertParseSuccess(parser, "1 2", new UnlogCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON));
    }

    @Test
    public void parse_extraWhitespace_returnsUnlogCommand() {
        assertParseSuccess(parser, "  1 \t 2  ", new UnlogCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON));
    }

    @Test
    public void parse_missingOrExtraArguments_throwsParseException() {
        for (String args : List.of("", "  ", "1", "1 2 3", "1 2 nt/Note")) {
            assertParseFailure(parser, args, MESSAGE_INVALID_INDICES);
        }
    }

    @Test
    public void parse_invalidIndexInEitherPosition_throwsParseException() {
        // the shared index parser validates the numbers; this parser must report unlog's usage for either failure
        for (String invalidIndex : getInvalidIndexInputs("abc")) {
            assertParseFailure(parser, invalidIndex + " 2", MESSAGE_INVALID_INDICES);
            assertParseFailure(parser, "1 " + invalidIndex, MESSAGE_INVALID_INDICES);
        }
    }
}
