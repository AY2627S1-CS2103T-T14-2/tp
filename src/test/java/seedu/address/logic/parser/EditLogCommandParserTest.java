package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalInteractionNotes.CHARACTER_PRESERVATION_NOTES;
import static seedu.address.testutil.TypicalInteractionNotes.SUPPLEMENTARY_NOTES_AT_LIMIT;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditLogCommand;
import seedu.address.model.person.Interaction;

/**
 * Tests optional edit fields, shared validation and error messages for {@code EditLogCommandParser}.
 */
public class EditLogCommandParserTest {

    private static final String MESSAGE_INVALID_INDICES =
            ParserUtil.MESSAGE_INVALID_INTERACTION_INDICES + "\n" + EditLogCommand.MESSAGE_USAGE;
    private final EditLogCommandParser parser = new EditLogCommandParser();

    @Test
    public void parse_unicodeBlankNote_failure() {
        for (String note : List.of("\u0085", "\u2003", "\u00a0", " \t\u0085\u2003\u00a0\u202f\u3000 ")) {
            assertParseFailure(parser, " 1 2 nt/" + note, Interaction.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_unicodePaddedNote_preservesInternalSpacing() {
        String note = "Called  about\u00a0the\u2003appeal";
        assertParseSuccess(parser, " 1 2 nt/\u2003\u00a0" + note + "\u202f\u3000",
                new EditLogCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                        Optional.of(note), Optional.empty()));
    }

    @Test
    public void parse_specialCharacters_preservesNote() {
        for (String note : CHARACTER_PRESERVATION_NOTES) {
            assertParseSuccess(parser, " 1 2 nt/" + note,
                    new EditLogCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                            Optional.of(note), Optional.empty()));
        }
    }

    @Test
    public void parse_supplementaryCharactersAtLengthBoundary_checksCodePoints() {
        // both notes contain 500 code points, but exceed 500 UTF-16 code units
        for (String note : SUPPLEMENTARY_NOTES_AT_LIMIT) {
            // surrounding spaces are trimmed before checking the limit
            assertParseSuccess(parser, " 1 2 nt/ " + note + " ",
                    new EditLogCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                            Optional.of(note), Optional.empty()));
            // one additional ASCII character takes the note to 501 code points
            assertParseFailure(parser, " 1 2 nt/ " + note + "a ",
                    Interaction.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_noteOnly_returnsEditLogCommand() {
        EditLogCommand expected = new EditLogCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                Optional.of("Corrected note"), Optional.empty());
        assertParseSuccess(parser, " 1 2 nt/Corrected note", expected);
        // whitespace and leading zeros are accepted without changing the index order
        assertParseSuccess(parser, " 01 \t 02 nt/  Corrected note  ", expected);
    }

    @Test
    public void parse_dateOnly_returnsEditLogCommand() {
        assertParseSuccess(parser, " 1 2 dt/2024-02-29", new EditLogCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                Optional.empty(), Optional.of(LocalDate.of(2024, 2, 29))));
    }

    @Test
    public void parse_bothFieldsEitherOrder_returnsEditLogCommand() {
        EditLogCommand expected = new EditLogCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                Optional.of("Corrected note"), Optional.of(LocalDate.of(2024, 2, 29)));
        assertParseSuccess(parser, " 1 2 nt/Corrected note dt/2024-02-29", expected);
        assertParseSuccess(parser, " 1 2 dt/ 2024-02-29  nt/ Corrected note  ", expected);
    }

    @Test
    public void parse_missingOrExtraIndices_failure() {
        for (String args : List.of("", " ", "1", "nt/Note", "1 nt/Note", "1 2 3 nt/Note", "1 2 extra dt/2024-02-29")) {
            assertParseFailure(parser, args, MESSAGE_INVALID_INDICES);
        }
    }

    @Test
    public void parse_invalidIndexInEitherPosition_failure() {
        for (String invalidIndex : List.of("0", "-1", "+1", "abc", "1.5", "2147483648")) {
            assertParseFailure(parser, invalidIndex + " 2 nt/Note", MESSAGE_INVALID_INDICES);
            assertParseFailure(parser, "1 " + invalidIndex + " nt/Note", MESSAGE_INVALID_INDICES);
        }
    }

    @Test
    public void parse_noFields_failure() {
        assertParseFailure(parser, " 1 2 ", EditLogCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void parse_invalidNote_failure() {
        for (String note : List.of("", "   ", "a".repeat(Interaction.MAX_NOTE_LENGTH + 1))) {
            assertParseFailure(parser, " 1 2 nt/" + note, Interaction.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidDate_failure() {
        for (String date : List.of("", "   ", "2024-2-29", "2024/02/29", "2024-02-30", "2023-02-29")) {
            assertParseFailure(parser, " 1 2 dt/" + date, Interaction.MESSAGE_INVALID_DATE_FORMAT);
        }
        assertParseFailure(parser, " 1 2 dt/1899-12-31", Interaction.MESSAGE_EARLY_DATE);
        assertParseFailure(parser, " 1 2 dt/" + LocalDate.now().plusYears(1),
                Interaction.MESSAGE_FUTURE_DATE);
    }

    @Test
    public void parse_dateBoundaries_success() {
        for (LocalDate date : List.of(LocalDate.of(1900, 1, 1), LocalDate.now())) {
            assertParseSuccess(parser, " 1 2 dt/" + date, new EditLogCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                    Optional.empty(), Optional.of(date)));
        }
    }

    @Test
    public void parse_repeatedPrefixes_failure() {
        assertParseFailure(parser, " 1 2 nt/First nt/Second",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NOTE));
        assertParseFailure(parser, " 1 2 dt/2024-02-29 dt/2024-02-29",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DATE));
        assertParseFailure(parser, " 1 2 nt/First nt/Second dt/2024-02-29 dt/2024-02-29",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NOTE, PREFIX_DATE));
    }

    @Test
    public void parse_multipleErrors_reportsInValidationOrder() {
        // malformed indices precede duplicate prefixes, which precede field validation
        assertParseFailure(parser, " 1 abc nt/ dt/invalid dt/invalid", MESSAGE_INVALID_INDICES);
        assertParseFailure(parser, " 1 2 nt/ dt/invalid dt/invalid",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DATE));
        // note validation precedes date validation regardless of prefix order
        assertParseFailure(parser, " 1 2 nt/ dt/invalid", Interaction.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " 1 2 dt/invalid nt/", Interaction.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unrecognisedPrefix_keptInNote() {
        String note = "Discussed d/o and x/details";
        assertParseSuccess(parser, " 1 2 nt/" + note, new EditLogCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                Optional.of(note), Optional.empty()));
    }
}
