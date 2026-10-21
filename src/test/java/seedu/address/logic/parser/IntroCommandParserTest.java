package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTRODUCER;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.IntroCommand;

public class IntroCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, IntroCommand.MESSAGE_USAGE);

    private IntroCommandParser parser = new IntroCommandParser();

    @Test
    public void parse_introducerIndexGiven_returnsSetCommand() {
        IntroCommand expected = new IntroCommand(INDEX_SECOND_PERSON, Optional.of(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, " 2 by/1", expected);

        // extra spaces and leading zeros
        assertParseSuccess(parser, "  02   by/  01  ", expected);
    }

    @Test
    public void parse_emptyIntroducerIndex_returnsRemoveCommand() {
        IntroCommand expected = new IntroCommand(INDEX_SECOND_PERSON, Optional.empty());
        assertParseSuccess(parser, " 2 by/", expected);
        assertParseSuccess(parser, " 2 by/   ", expected);
    }

    @Test
    public void parse_missingParts_failure() {
        // nothing given
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);

        // no index
        assertParseFailure(parser, " by/1", MESSAGE_INVALID_FORMAT);

        // no by/ prefix
        assertParseFailure(parser, " 2", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 2 1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndices_failure() {
        // target index not a positive whole number
        assertParseFailure(parser, " 0 by/1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " -1 by/1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " abc by/1", MESSAGE_INVALID_FORMAT);

        // introducer index not a positive whole number
        assertParseFailure(parser, " 2 by/0", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 2 by/1.5", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 2 by/Tan Wei Ming", MESSAGE_INVALID_FORMAT);

        // text before the prefix that is not a single index
        assertParseFailure(parser, " 2 3 by/1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_repeatedPrefix_failure() {
        assertParseFailure(parser, " 2 by/1 by/3",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_INTRODUCER));
    }
}
