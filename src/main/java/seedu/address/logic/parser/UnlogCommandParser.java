package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.UnlogCommand;
import seedu.address.logic.parser.ParserUtil.InteractionIndices;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code UnlogCommand} object.
 */
public class UnlogCommandParser implements Parser<UnlogCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the {@code UnlogCommand}
     * and returns a {@code UnlogCommand} object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    @Override
    public UnlogCommand parse(String args) throws ParseException {
        requireNonNull(args);
        InteractionIndices indices = ParserUtil.parseInteractionIndices(args, UnlogCommand.MESSAGE_USAGE);
        return new UnlogCommand(indices.supporterIndex(), indices.interactionIndex());
    }
}
