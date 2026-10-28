package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTRODUCER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORGANISATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STAGE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditLogCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.IntroCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.LogCommand;
import seedu.address.logic.commands.QuietCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.StageCommand;
import seedu.address.logic.commands.StatsCommand;
import seedu.address.logic.commands.TagCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.UnlogCommand;
import seedu.address.logic.commands.UntagCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class AddressBookParser {

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");
    private static final Logger logger = LogsCenter.getLogger(AddressBookParser.class);
    private static final Set<String> COMMANDS_WITHOUT_ARGUMENTS = Set.of(ClearCommand.COMMAND_WORD,
            ListCommand.COMMAND_WORD, StatsCommand.COMMAND_WORD, ExitCommand.COMMAND_WORD, HelpCommand.COMMAND_WORD,
            UndoCommand.COMMAND_WORD, RedoCommand.COMMAND_WORD);

    /**
     * Returns prefix-like words not supported by the command's own format, without changing parsing.
     * Find's parser rejects unsupported known filters itself, so its advisory scan checks only unknown words.
     * Commands that take no arguments produce no notes, as they ignore or reject any.
     * Unknown command words retain the global prefix check.
     *
     * @throws NullPointerException if {@code userInput} is null.
     */
    public List<String> findUnrecognisedPrefixes(String userInput) {
        requireNonNull(userInput);
        Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            return ArgumentTokenizer.findUnrecognisedPrefixes(userInput);
        }
        String commandWord = getCommandWord(matcher);
        if (COMMANDS_WITHOUT_ARGUMENTS.contains(commandWord)) {
            return List.of();
        }
        List<Prefix> recognisedPrefixes = switch (commandWord) {
            case AddCommand.COMMAND_WORD -> List.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                    PREFIX_ORGANISATION, PREFIX_STAGE, PREFIX_TAG);
            case EditCommand.COMMAND_WORD -> List.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                    PREFIX_ORGANISATION, PREFIX_TAG);
            // Find's parser rejects unsupported known filters; do not describe them as ordinary text.
            case FindCommand.COMMAND_WORD -> CliSyntax.ALL_PREFIXES;
            case LogCommand.COMMAND_WORD, EditLogCommand.COMMAND_WORD -> List.of(PREFIX_NOTE, PREFIX_DATE);
            case IntroCommand.COMMAND_WORD -> List.of(PREFIX_INTRODUCER);
            case TagCommand.COMMAND_WORD, UntagCommand.COMMAND_WORD -> List.of(PREFIX_TAG);
            case DeleteCommand.COMMAND_WORD, ViewCommand.COMMAND_WORD, StageCommand.COMMAND_WORD,
                    UnlogCommand.COMMAND_WORD, QuietCommand.COMMAND_WORD -> List.of();
            default -> CliSyntax.ALL_PREFIXES;
        };
        return ArgumentTokenizer.findUnrecognisedPrefixes(userInput, recognisedPrefixes);
    }

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string.
     * @return the command based on the user input.
     * @throws ParseException if the user input does not conform to the expected format.
     */
    public Command parseCommand(String userInput) throws ParseException {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = getCommandWord(matcher);
        final String arguments = matcher.group("arguments");

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Command word: " + commandWord + "; Arguments: " + arguments);

        return switch (commandWord) {
            case AddCommand.COMMAND_WORD -> new AddCommandParser().parse(arguments);
            case EditCommand.COMMAND_WORD -> new EditCommandParser().parse(arguments);
            case DeleteCommand.COMMAND_WORD -> new DeleteCommandParser().parse(arguments);
            case EditLogCommand.COMMAND_WORD -> new EditLogCommandParser().parse(arguments);
            case LogCommand.COMMAND_WORD -> new LogCommandParser().parse(arguments);
            case UnlogCommand.COMMAND_WORD -> new UnlogCommandParser().parse(arguments);
            case StageCommand.COMMAND_WORD -> new StageCommandParser().parse(arguments);
            case ViewCommand.COMMAND_WORD -> new ViewCommandParser().parse(arguments);
            case IntroCommand.COMMAND_WORD -> new IntroCommandParser().parse(arguments);
            case TagCommand.COMMAND_WORD -> new TagCommandParser().parse(arguments);
            case UntagCommand.COMMAND_WORD -> new UntagCommandParser().parse(arguments);
            case ClearCommand.COMMAND_WORD -> new ClearCommand();
            case FindCommand.COMMAND_WORD -> new FindCommandParser().parse(arguments);
            case QuietCommand.COMMAND_WORD -> new QuietCommandParser().parse(arguments);
            case ListCommand.COMMAND_WORD -> new ListCommand();
            case StatsCommand.COMMAND_WORD -> new StatsCommand();
            case ExitCommand.COMMAND_WORD -> new ExitCommand();
            case HelpCommand.COMMAND_WORD -> new HelpCommand();
            case UndoCommand.COMMAND_WORD -> {
                requireNoArguments(arguments, UndoCommand.MESSAGE_NO_ARGUMENTS);
                yield new UndoCommand();
            }
            case RedoCommand.COMMAND_WORD -> {
                requireNoArguments(arguments, RedoCommand.MESSAGE_NO_ARGUMENTS);
                yield new RedoCommand();
            }
            default -> {
                logger.finer("This user input caused a ParseException: " + userInput);
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }

    /**
     * Throws a {@code ParseException} with {@code message} if {@code arguments} contains anything but spaces.
     * Used by commands where extra text, e.g. a count in {@code undo 3}, suggests the user expects something else.
     */
    private static void requireNoArguments(String arguments, String message) throws ParseException {
        if (!arguments.isBlank()) {
            throw new ParseException(message);
        }
    }

    /**
     * Returns the command word matched by {@code matcher} in lower case, so that command words ignore case.
     */
    private static String getCommandWord(Matcher matcher) {
        return matcher.group("commandWord").toLowerCase(Locale.ROOT);
    }

}
