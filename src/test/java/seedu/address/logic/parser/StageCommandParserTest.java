package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.IndexTestUtil.getInvalidIndexInputs;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import seedu.address.logic.commands.StageCommand;
import seedu.address.model.person.Stage;

public class StageCommandParserTest {

    private static final String MESSAGE_INVALID_INDEX =
            ParserUtil.MESSAGE_INVALID_INDEX + "\n" + StageCommand.MESSAGE_USAGE;
    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE);

    private final StageCommandParser parser = new StageCommandParser();

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
    @ResourceLock("java.util.Locale.default")
    public void parse_turkishDefaultLocale_returnsStageCommand() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertParseSuccess(parser, " 1 giving", new StageCommand(INDEX_FIRST_PERSON, Stage.GIVING));
            assertParseSuccess(parser, " 2 st/cultivating", new StageCommand(INDEX_SECOND_PERSON, Stage.CULTIVATING));
            assertParseFailure(parser, " 1 gave", Stage.MESSAGE_CONSTRAINTS);
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void parse_stagePrefix_returnsStageCommand() {
        // add and find take the stage as st/STAGE, so the same form is accepted here
        assertParseSuccess(parser, " 2 st/giving", new StageCommand(INDEX_SECOND_PERSON, Stage.GIVING));
        assertParseSuccess(parser, " 1 st/LAPSED", new StageCommand(INDEX_FIRST_PERSON, Stage.LAPSED));
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
        for (String invalidIndex : getInvalidIndexInputs("x")) {
            assertParseFailure(parser, " " + invalidIndex + " giving", MESSAGE_INVALID_INDEX);
        }

        // non-ASCII digit
        String fullWidthIndex = "２";
        assertParseFailure(parser, " " + fullWidthIndex + " giving", MESSAGE_INVALID_INDEX);

        // invalid index is reported before invalid stage
        assertParseFailure(parser, " 0 gave", MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_invalidStage_failure() {
        // not one of the six stages
        assertParseFailure(parser, " 2 gave", Stage.MESSAGE_CONSTRAINTS);

        // abbreviations are not accepted
        assertParseFailure(parser, " 2 cult", Stage.MESSAGE_CONSTRAINTS);

        // st/ with no stage after it
        assertParseFailure(parser, " 2 st/", Stage.MESSAGE_CONSTRAINTS);

        // only st/ is removed, as other prefixes are not part of the stage
        assertParseFailure(parser, " 2 t/giving", Stage.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " 2 ST/giving", Stage.MESSAGE_CONSTRAINTS);
    }
}
