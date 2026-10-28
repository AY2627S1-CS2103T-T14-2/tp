package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class CommandPrefixDetectionTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void findUnrecognisedPrefixes_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.findUnrecognisedPrefixes(null));
        assertThrows(NullPointerException.class, () -> ArgumentTokenizer.findUnrecognisedPrefixes(null, List.of()));
        assertThrows(NullPointerException.class, () -> ArgumentTokenizer.findUnrecognisedPrefixes("log", null));
    }

    @Test
    public void findUnrecognisedPrefixes_eachCommandsSupportedPrefixes_returnsEmpty() {
        for (String command : List.of("add n/Tan p/123 e/tan@example.com a/Office o/DBS st/giving t/vip",
                "edit 1 n/Tan p/123 e/tan@example.com a/Office o/DBS t/vip",
                "find tan o/DBS t/vip st/giving p/123 e/@example",
                "log 1 nt/Called dt/2024-01-01", "editlog 1 1 nt/Called dt/2024-01-01",
                "intro 2 by/1", "tag 1 t/vip t/donor", "untag 1 t/vip")) {
            assertEquals(List.of(), parser.findUnrecognisedPrefixes(command), command);
        }
    }

    @Test
    public void findUnrecognisedPrefixes_prefixesFromOtherCommands_returnsThemInOrder() {
        assertEquals(List.of("by/"), parser.findUnrecognisedPrefixes("add n/Lim p/91234567 by/1"));
        assertEquals(List.of("st/"), parser.findUnrecognisedPrefixes("edit 1 st/giving"));
        assertEquals(List.of("t/"), parser.findUnrecognisedPrefixes("log 1 nt/Called t/vip"));
        assertEquals(List.of("t/", "x/", "by/", "T/"), parser.findUnrecognisedPrefixes(
                "editlog 1 1 nt/Called t/vip x/one by/2 t/again T/vip"));
        assertEquals(List.of("x/"), parser.findUnrecognisedPrefixes("find n/Tan x/foo"));
        assertEquals(List.of("p/"), parser.findUnrecognisedPrefixes("intro 1 by/2 p/123"));
        assertEquals(List.of("st/"), parser.findUnrecognisedPrefixes("tag 1 t/vip st/giving"));
        assertEquals(List.of("o/"), parser.findUnrecognisedPrefixes("untag 1 t/vip o/DBS"));
    }

    @Test
    public void findUnrecognisedPrefixes_positionalCommands_doNotRecognisePrefixes() {
        for (String command : List.of("delete 1", "view 1", "stage 1", "unlog 1 1", "quiet")) {
            assertEquals(List.of("st/"), parser.findUnrecognisedPrefixes(command + " st/giving"), command);
        }
    }

    @Test
    public void findUnrecognisedPrefixes_findUnsupportedKnownFilters_leavesRejectionToFindParser() {
        for (String prefix : List.of("n/", "a/", "dt/", "nt/", "by/")) {
            assertEquals(List.of(), parser.findUnrecognisedPrefixes("find " + prefix + "value"), prefix);
            assertEquals(List.of(), parser.findUnrecognisedPrefixes("find o/DBS " + prefix + "value"), prefix);
        }
    }

    @Test
    public void findUnrecognisedPrefixes_ignoredArguments_returnsEmpty() {
        for (String command : List.of("clear", "list", "stats", "exit", "help", "undo", "redo")) {
            assertEquals(List.of(), parser.findUnrecognisedPrefixes(command + " n/Tan x/foo"), command);
        }
    }

    @Test
    public void findUnrecognisedPrefixes_upperCaseCommandWord_usesThatCommandsPrefixes() {
        // command words ignore case here too, matching parseCommand
        assertEquals(List.of("st/"), parser.findUnrecognisedPrefixes("STAGE 1 st/giving"));
        assertEquals(List.of("t/"), parser.findUnrecognisedPrefixes("Log 1 nt/Called t/vip"));
        assertEquals(List.of(), parser.findUnrecognisedPrefixes("UNDO n/Tan x/foo"));
    }

    @Test
    public void findUnrecognisedPrefixes_unknownOrMalformedCommand_retainsGlobalCheck() {
        assertEquals(List.of("x/"), parser.findUnrecognisedPrefixes("unknown n/Tan x/foo"));
        assertEquals(List.of(), parser.findUnrecognisedPrefixes("   "));
        assertEquals(List.of("x/"), parser.findUnrecognisedPrefixes("log 1 nt/Called\nx/foo"));
        assertEquals(List.of(), parser.findUnrecognisedPrefixes("  log\t1 nt/Called dt/2024-01-01 "));
    }

    @Test
    public void findUnrecognisedPrefixes_ordinarySlashWordsAndLinks_returnsEmpty() {
        assertEquals(List.of(), parser.findUnrecognisedPrefixes("add n/Ravi s/o Kumar p/123 a/Office c/o Mdm Tan"));
        assertEquals(List.of(), parser.findUnrecognisedPrefixes("log 1 nt/Met W/ him and/or his wife, w/o the kids; "
                + "https://example.com/a and 12/3"));
    }

    @Test
    public void findUnrecognisedPrefixes_explicitPrefixList_doesNotUseGlobalPrefixes() {
        assertEquals(List.of("t/", "n/"), ArgumentTokenizer.findUnrecognisedPrefixes(
                "log 1 nt/Called t/vip n/Tan t/again", List.of(PREFIX_NOTE)));
    }
}
