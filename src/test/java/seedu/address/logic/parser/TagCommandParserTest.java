package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.TagCommand;
import seedu.address.model.tag.Tag;

public class TagCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE);

    private TagCommandParser parser = new TagCommandParser();

    @Test
    public void parse_oneTag_success() {
        TagCommand expected = new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("vip")));
        assertParseSuccess(parser, " 1 t/vip", expected);

        // extra spaces and leading zeros
        assertParseSuccess(parser, "  01   t/  vip  ", expected);
    }

    @Test
    public void parse_severalTags_success() {
        TagCommand expected = new TagCommand(INDEX_SECOND_PERSON,
                Set.of(new Tag("vip"), new Tag("boardIntro")));
        assertParseSuccess(parser, " 2 t/vip t/boardIntro", expected);

        // a repeated tag counts once
        assertParseSuccess(parser, " 2 t/vip t/boardIntro t/vip", expected);
    }

    @Test
    public void parse_tagsDifferingOnlyInCase_keepsFirstTyped() {
        assertParseSuccess(parser, " 1 t/vip t/VIP",
                new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("vip"))));
        assertParseSuccess(parser, " 1 t/VIP t/vip",
                new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("VIP"))));
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
        assertParseFailure(parser, " 0 t/vip", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " -1 t/vip", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " abc t/vip", MESSAGE_INVALID_FORMAT);

        // text before the prefix that is not a single index
        assertParseFailure(parser, " 1 2 t/vip", MESSAGE_INVALID_FORMAT);
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
