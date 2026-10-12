package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LogCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code LogCommand} object.
 */
public class LogCommandParser implements Parser<LogCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the {@code LogCommand}
     * and returns a {@code LogCommand} object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public LogCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_NOTE);

        if (argMultimap.getPreamble().isEmpty() || argMultimap.getValue(PREFIX_NOTE).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, LogCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NOTE);

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, LogCommand.MESSAGE_USAGE), pe);
        }

        String note = ParserUtil.parseInteractionNote(argMultimap.getValue(PREFIX_NOTE).get());
        return new LogCommand(index, note);
    }
}
