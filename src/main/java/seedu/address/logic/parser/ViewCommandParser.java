package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a {@code ViewCommand}.
 */
public class ViewCommandParser implements Parser<ViewCommand> {

    /**
     * Parses {@code args} as a single positive integer index and returns a {@code ViewCommand} for execution.
     *
     * @throws ParseException if the arguments do not conform to the expected format.
     */
    @Override
    public ViewCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Index index = ParserUtil.parseIndex(args, ViewCommand.MESSAGE_USAGE);
        return new ViewCommand(index);
    }
}
