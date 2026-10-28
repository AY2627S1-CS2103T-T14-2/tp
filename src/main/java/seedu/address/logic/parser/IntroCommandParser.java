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
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, IntroCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_INTRODUCER);

        Index targetIndex = ParserUtil.parseIndex(argMultimap.getPreamble(), IntroCommand.MESSAGE_USAGE);
        String introducerValue = argMultimap.getValue(PREFIX_INTRODUCER).get().trim();
        if (introducerValue.isEmpty()) {
            return new IntroCommand(targetIndex, Optional.empty());
        }
        return new IntroCommand(targetIndex, Optional.of(
                ParserUtil.parseIndex(introducerValue, IntroCommand.MESSAGE_USAGE)));
    }
}
