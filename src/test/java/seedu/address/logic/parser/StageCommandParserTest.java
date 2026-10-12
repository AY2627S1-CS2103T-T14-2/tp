package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.StageCommand;
import seedu.address.model.person.Stage;

public class StageCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE);

    private StageCommandParser parser = new StageCommandParser();

    @Test
    public void parse_validArgs_returnsStageCommand() {
        assertParseSuccess(parser, " 2 cultivating", new StageCommand(INDEX_SECOND_PERSON, Stage.CULTIVATING));

        // case is ignored
        assertParseSuccess(parser, " 1 LAPSED", new StageCommand(INDEX_FIRST_PERSON, Stage.LAPSED));
        assertParseSuccess(parser, " 1 GiViNg", new StageCommand(INDEX_FIRST_PERSON, Stage.GIVING));

        // extra whitespace around and between values
        assertParseSuccess(parser, " \t 2   declined \n ", new StageCommand(INDEX_SECOND_PERSON, Stage.DECLINED));

        // leading zeros in index
        assertParseSuccess(parser, " 02 prospect", new StageCommand(INDEX_SECOND_PERSON, Stage.PROSPECT));
    }

    @Test
    public void parse_missingParts_failure() {
        // nothing given
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "   ", MESSAGE_INVALID_FORMAT);

        // stage missing
        assertParseFailure(parser, " 2", MESSAGE_INVALID_FORMAT);

        // index missing
        assertParseFailure(parser, " giving", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_tooManyParts_failure() {
        // more than one stage
        assertParseFailure(parser, " 2 giving lapsed", MESSAGE_INVALID_FORMAT);

        // more than one index
        assertParseFailure(parser, " 1 2 giving", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndex_failure() {
        // not a number
        assertParseFailure(parser, " x giving", MESSAGE_INVALID_FORMAT);

        // zero
        assertParseFailure(parser, " 0 giving", MESSAGE_INVALID_FORMAT);

        // negative
        assertParseFailure(parser, " -1 giving", MESSAGE_INVALID_FORMAT);

        // signed
        assertParseFailure(parser, " +1 giving", MESSAGE_INVALID_FORMAT);

        // decimal
        assertParseFailure(parser, " 1.5 giving", MESSAGE_INVALID_FORMAT);

        // larger than the maximum int
        assertParseFailure(parser, " 2147483648 giving", MESSAGE_INVALID_FORMAT);

        // invalid index is reported before invalid stage
        assertParseFailure(parser, " 0 gave", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidStage_failure() {
        // not one of the six stages
        assertParseFailure(parser, " 2 gave", Stage.MESSAGE_CONSTRAINTS);

        // abbreviations are not accepted
        assertParseFailure(parser, " 2 cult", Stage.MESSAGE_CONSTRAINTS);

        // prefixed stage is not accepted
        assertParseFailure(parser, " 2 st/giving", Stage.MESSAGE_CONSTRAINTS);
    }
}
