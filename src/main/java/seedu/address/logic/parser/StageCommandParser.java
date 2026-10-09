package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.StageCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Stage;

/**
 * Parses input arguments and creates a new StageCommand object
 */
public class StageCommandParser implements Parser<StageCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the StageCommand
     * and returns a StageCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public StageCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String[] parts = args.trim().split("\\s+");

        // exactly an INDEX and a STAGE, with no prefixes
        if (parts.length != 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE));
        }

        Index index;
        try {
            index = ParserUtil.parseIndex(parts[0]);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE), pe);
        }

        Stage stage = ParserUtil.parseStage(parts[1]);
        return new StageCommand(index, stage);
    }

}
