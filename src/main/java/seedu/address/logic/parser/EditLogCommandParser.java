package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;

import java.time.LocalDate;
import java.util.Optional;

import seedu.address.logic.commands.EditLogCommand;
import seedu.address.logic.parser.ParserUtil.InteractionIndices;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code EditLogCommand} object.
 */
public class EditLogCommandParser implements Parser<EditLogCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the {@code EditLogCommand}
     * and returns a {@code EditLogCommand} object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    @Override
    public EditLogCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_NOTE, PREFIX_DATE);

        InteractionIndices indices = ParserUtil.parseInteractionIndices(
                argMultimap.getPreamble(), EditLogCommand.MESSAGE_USAGE);

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NOTE, PREFIX_DATE);

        if (argMultimap.getValue(PREFIX_NOTE).isEmpty() && argMultimap.getValue(PREFIX_DATE).isEmpty()) {
            throw new ParseException(EditLogCommand.MESSAGE_NOT_EDITED);
        }

        Optional<String> note = Optional.empty();
        if (argMultimap.getValue(PREFIX_NOTE).isPresent()) {
            note = Optional.of(ParserUtil.parseInteractionNote(argMultimap.getValue(PREFIX_NOTE).get()));
        }
        Optional<LocalDate> date = ParserUtil.parseOptionalInteractionDate(argMultimap.getValue(PREFIX_DATE));
        return new EditLogCommand(indices.supporterIndex(), indices.interactionIndex(), note, date);
    }
}
