package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.QuietCommand;

public class QuietCommandParserTest {
    private final QuietCommandParser parser = new QuietCommandParser();

    @Test
    public void parse_noDays_usesDefaultThreshold() {
        assertParseSuccess(parser, "", new QuietCommand(60));
        assertParseSuccess(parser, " \t \n ", new QuietCommand(60));
    }

    @Test
    public void parse_positiveDays_success() {
        assertParseSuccess(parser, "1", new QuietCommand(1));
        assertParseSuccess(parser, " 30 ", new QuietCommand(30));
        assertParseSuccess(parser, "00060", new QuietCommand(60));
        assertParseSuccess(parser, "2147483647", new QuietCommand(Integer.MAX_VALUE));
    }

    @Test
    public void parse_invalidDays_failure() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, QuietCommand.MESSAGE_USAGE);
        for (String input : List.of("0", "000", "-1", "+30", "1.5", "abc", "30 days", "30 60",
                "2147483648", "999999999999999999999999", "dt/30", "1e2")) {
            assertParseFailure(parser, input, message);
        }
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
