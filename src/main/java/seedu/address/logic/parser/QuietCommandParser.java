package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.QuietCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses an optional positive day count into a quiet command.
 */
public class QuietCommandParser implements Parser<QuietCommand> {
    /**
     * Parses no arguments as the default threshold, or exactly one positive whole number as DAYS.
     *
     * @throws ParseException if DAYS is invalid, exceeds the integer limit, or extra arguments are supplied.
     */
    @Override
    public QuietCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            return new QuietCommand();
        }
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedArgs)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, QuietCommand.MESSAGE_USAGE));
        }
        return new QuietCommand(Integer.parseInt(trimmedArgs));
    }
}
