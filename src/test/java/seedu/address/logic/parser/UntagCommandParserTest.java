package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.UntagCommand;
import seedu.address.model.tag.Tag;

public class UntagCommandParserTest {

    private static final String MESSAGE_INVALID_INDEX =
            ParserUtil.MESSAGE_INVALID_INDEX + "\n" + UntagCommand.MESSAGE_USAGE;
    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, UntagCommand.MESSAGE_USAGE);

    private UntagCommandParser parser = new UntagCommandParser();

    @Test
    public void parse_invalidIndexValues_reportsIndexErrorAndUsage() {
        String expectedMessage = ParserUtil.MESSAGE_INVALID_INDEX + "\n" + UntagCommand.MESSAGE_USAGE;
        for (String index : new String[]{"0", "-1", "+1", "abc", "1.5", "2147483648"}) {
            assertParseFailure(parser, index + " t/vip", expectedMessage);
        }
    }

    @Test
    public void parse_oneTag_success() {
        UntagCommand expected = new UntagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("vip")));
        assertParseSuccess(parser, " 1 t/vip", expected);

        // extra spaces and leading zeros
        assertParseSuccess(parser, "  01   t/  vip  ", expected);
    }

    @Test
    public void parse_severalTags_success() {
        UntagCommand expected = new UntagCommand(INDEX_SECOND_PERSON,
                Set.of(new Tag("vip"), new Tag("boardIntro")));
        assertParseSuccess(parser, " 2 t/vip t/boardIntro", expected);

        // a repeated tag counts once
        assertParseSuccess(parser, " 2 t/vip t/boardIntro t/vip", expected);
    }

    @Test
    public void parse_missingParts_failure() {
        // nothing given
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);

        // no index
        assertParseFailure(parser, " t/vip", MESSAGE_INVALID_FORMAT);

        // no tag
        assertParseFailure(parser, " 1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 1 vip", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndex_failure() {
        assertParseFailure(parser, " 0 t/vip", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " -1 t/vip", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, " abc t/vip", MESSAGE_INVALID_INDEX);

        // text before the prefix that is not a single index
        assertParseFailure(parser, " 1 2 t/vip", MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_invalidTag_failure() {
        // not alphanumeric
        assertParseFailure(parser, " 1 t/hubby*", Tag.MESSAGE_CONSTRAINTS);

        // empty, alone or with valid tags
        assertParseFailure(parser, " 1 t/", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " 1 t/vip t/", Tag.MESSAGE_CONSTRAINTS);

        // tags cannot contain spaces
        assertParseFailure(parser, " 1 t/board intro", Tag.MESSAGE_CONSTRAINTS);
    }
}
