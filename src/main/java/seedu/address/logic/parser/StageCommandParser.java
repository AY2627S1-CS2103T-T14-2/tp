package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STAGE;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.StageCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Stage;

/**
 * Parses input arguments and creates a new StageCommand object
 */
public class StageCommandParser implements Parser<StageCommand> {

    /** The command takes exactly an INDEX and a STAGE, which may have an optional {@code st/} prefix. */
    private static final int EXPECTED_ARGUMENT_COUNT = 2;

    /**
     * Parses the given {@code String} of arguments in the context of the StageCommand
     * and returns a StageCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public StageCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String[] arguments = args.trim().split("\\s+");
        if (arguments.length != EXPECTED_ARGUMENT_COUNT) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE));
        }

        Index index = ParserUtil.parseIndex(arguments[0], StageCommand.MESSAGE_USAGE);
        Stage stage = ParserUtil.parseStage(removeStagePrefix(arguments[1]));
        return new StageCommand(index, stage);
    }

    /**
     * Returns {@code stageArgument} without a leading {@code st/}, which {@code add} and {@code find} use,
     * so that {@code stage 1 st/giving} works the same as {@code stage 1 giving}.
     */
    private static String removeStagePrefix(String stageArgument) {
        String prefix = PREFIX_STAGE.getPrefix();
        return stageArgument.startsWith(prefix) ? stageArgument.substring(prefix.length()) : stageArgument;
    }

}
