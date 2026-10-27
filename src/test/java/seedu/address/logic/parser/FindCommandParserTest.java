package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.PersonMatchesSearchPredicate;
import seedu.address.model.person.Stage;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new PersonMatchesSearchPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_prefixOnly_success() {
        assertParseSuccess(parser, "o/DBS Bank", new FindCommand(new PersonMatchesSearchPredicate(List.of(),
                List.of("DBS Bank"), List.of(), List.of(), List.of(), List.of())));
        assertParseSuccess(parser, "t/board", new FindCommand(new PersonMatchesSearchPredicate(List.of(),
                List.of(), List.of("board"), List.of(), List.of(), List.of())));
        assertParseSuccess(parser, "st/GIVING", new FindCommand(new PersonMatchesSearchPredicate(List.of(),
                List.of(), List.of(), List.of(Stage.GIVING), List.of(), List.of())));
        assertParseSuccess(parser, "p/9", new FindCommand(new PersonMatchesSearchPredicate(List.of(),
                List.of(), List.of(), List.of(), List.of("9"), List.of())));
        assertParseSuccess(parser, "e/@gmail", new FindCommand(new PersonMatchesSearchPredicate(List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of("@gmail"))));
    }

    @Test
    public void parse_allFieldsAndRepeatedPrefixes_success() {
        FindCommand expectedCommand = new FindCommand(new PersonMatchesSearchPredicate(List.of("tan", "lim"),
                List.of("DBS Bank", "OCBC"), List.of("board", "vip"), List.of(Stage.GIVING, Stage.LAPSED),
                List.of("9123", "8111"), List.of("@dbs", "@ocbc")));

        assertParseSuccess(parser, "tan lim o/DBS Bank o/OCBC t/board t/vip st/GIVING st/lapsed"
                + " p/9123 p/8111 e/@dbs e/@ocbc", expectedCommand);
        assertParseSuccess(parser, "tan lim e/@dbs p/9123 st/giving t/board o/DBS Bank"
                + " e/@ocbc p/8111 st/LAPSED t/vip o/OCBC", expectedCommand);
    }

    @Test
    public void parse_whitespaceAroundPrefixes_success() {
        FindCommand expectedCommand = new FindCommand(new PersonMatchesSearchPredicate(List.of("tan", "wei"),
                List.of("DBS Bank"), List.of(), List.of(Stage.GIVING), List.of(), List.of()));

        assertParseSuccess(parser, " \t tan\nwei\to/ DBS   Bank \tst/ GIVING\t ", expectedCommand);
    }

    @Test
    public void parse_textAfterPrefix_belongsToPrefixValue() {
        assertParseSuccess(parser, "o/DBS Bank tan", new FindCommand(new PersonMatchesSearchPredicate(List.of(),
                List.of("DBS Bank tan"), List.of(), List.of(), List.of(), List.of())));
    }

    @Test
    public void parse_unsupportedPrefixes_failure() {
        for (String prefix : List.of("n/", "a/", "dt/", "nt/", "by/")) {
            String expectedMessage = "Unsupported search filter(s): " + prefix + ".\n"
                    + "find supports only o/, t/, st/, p/ and e/.\n" + FindCommand.MESSAGE_USAGE;

            assertParseFailure(parser, prefix + "alex", expectedMessage);
            assertParseFailure(parser, prefix, expectedMessage);
            assertParseFailure(parser, "tan " + prefix + "value", expectedMessage);
            assertParseFailure(parser, "o/dbs " + prefix + "geylang", expectedMessage);
            assertParseFailure(parser, " \t o/dbs\n" + prefix + "geylang \t", expectedMessage);
            assertParseFailure(parser, prefix + "first " + prefix + "second", expectedMessage);
        }
        String expectedMessage = "Unsupported search filter(s): a/.\n"
                + "find supports only o/, t/, st/, p/ and e/.\n" + FindCommand.MESSAGE_USAGE;
        assertParseFailure(parser, "a/geylang", expectedMessage);
        assertParseFailure(parser, "o/dbs a/geylang", expectedMessage);
    }

    @Test
    public void parse_multipleUnsupportedPrefixes_listsEachPrefixOnce() {
        String expectedMessage = "Unsupported search filter(s): n/, a/, dt/, nt/, by/.\n"
                + "find supports only o/, t/, st/, p/ and e/.\n" + FindCommand.MESSAGE_USAGE;

        assertParseFailure(parser, "n/alex a/geylang dt/today nt/note by/1 n/bob", expectedMessage);
        assertParseFailure(parser, "o/dbs by/1 nt/note dt/today a/geylang n/alex a/bedok", expectedMessage);
    }

    @Test
    public void parse_slashedWords_success() {
        FindCommand expectedCommand = new FindCommand(new PersonMatchesSearchPredicate(
                List.of("Ravi", "s/o", "Kumar", "d/o", "Priya")));
        assertParseSuccess(parser, "Ravi s/o Kumar d/o Priya", expectedCommand);

        assertParseSuccess(parser, "o/DBS and/or OCBC", new FindCommand(new PersonMatchesSearchPredicate(List.of(),
                List.of("DBS and/or OCBC"), List.of(), List.of(), List.of(), List.of())));
        assertParseSuccess(parser, "e/alex@example.com/path", new FindCommand(new PersonMatchesSearchPredicate(
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of("alex@example.com/path"))));
    }

    @Test
    public void parse_formattedPartialPhone_success() {
        FindCommand expectedCommand = new FindCommand(new PersonMatchesSearchPredicate(List.of(), List.of(),
                List.of(), List.of(), List.of("659123"), List.of()));

        assertParseSuccess(parser, "p/+65 91-23", expectedCommand);
        assertParseSuccess(parser, "p/65-91 23", expectedCommand);
    }

    @Test
    public void parse_annotatedPhoneFragments_success() {
        for (String query : List.of("(65)", "9123 (HP)", "9 ext 12", "(65) 9123 EXT 12 (Office)")) {
            FindCommand expectedCommand = new FindCommand(new PersonMatchesSearchPredicate(List.of(), List.of(),
                    List.of(), List.of(), List.of(query), List.of()));
            assertParseSuccess(parser, "p/" + query, expectedCommand);
        }
    }

    @Test
    public void parse_emptyPrefixValue_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        for (String prefix : List.of("o/", "t/", "p/", "e/")) {
            assertParseFailure(parser, prefix, expectedMessage);
            assertParseFailure(parser, "tan " + prefix + "   ", expectedMessage);
            assertParseFailure(parser, prefix + "valid " + prefix, expectedMessage);
            assertParseFailure(parser, prefix + " " + prefix + "valid", expectedMessage);
        }
    }

    @Test
    public void parse_invalidStage_failure() {
        for (String value : List.of("", "giv", "donating", "giving lapsed")) {
            assertParseFailure(parser, "st/" + value, Stage.MESSAGE_CONSTRAINTS);
        }
        assertParseFailure(parser, "st/giving st/donating", Stage.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidPhoneQuery_failure() {
        for (String value : List.of("+", "-", "+ -", "abc", "912a", "(65)9123", "(HP)",
                "(65", "9123 ext", "9123 ext 1234567", "9123 (HP2)")) {
            assertParseFailure(parser, "p/" + value, FindCommandParser.MESSAGE_INVALID_PHONE_SEARCH);
        }
        assertParseFailure(parser, "p/9123 p/+", FindCommandParser.MESSAGE_INVALID_PHONE_SEARCH);
    }

}
