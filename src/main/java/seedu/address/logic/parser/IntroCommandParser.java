package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTRODUCER;

import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.IntroCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new IntroCommand object
 */
public class IntroCommandParser implements Parser<IntroCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the {@code IntroCommand}
     * and returns an {@code IntroCommand} object for execution.
     * An empty value after {@code by/} means the introducer is to be removed.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public IntroCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_INTRODUCER);

        if (argMultimap.getPreamble().isEmpty() || argMultimap.getValue(PREFIX_INTRODUCER).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, IntroCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_INTRODUCER);

        Index targetIndex = parseIndex(argMultimap.getPreamble());
        String introducerValue = argMultimap.getValue(PREFIX_INTRODUCER).get().trim();
        if (introducerValue.isEmpty()) {
            return new IntroCommand(targetIndex, Optional.empty());
        }
        return new IntroCommand(targetIndex, Optional.of(parseIndex(introducerValue)));
    }

    /**
     * Parses {@code value} as an index, reporting a wrong command format if it is not a positive whole number.
     */
    private static Index parseIndex(String value) throws ParseException {
        try {
            return ParserUtil.parseIndex(value);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, IntroCommand.MESSAGE_USAGE), pe);
        }
    }
}
