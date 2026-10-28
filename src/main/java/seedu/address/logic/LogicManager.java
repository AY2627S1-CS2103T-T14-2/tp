package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    public static final String MESSAGE_CHANGE_NOT_SAVED_FORMAT = "This change was made but not saved, "
            + "so do not repeat the command. Once the problem is fixed, run any command, e.g. list, "
            + "to save it, or run %1$s to reverse it.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        Command command = parseCommand(commandText);
        List<Person> personsBeforeCommand = List.copyOf(model.getAddressBook().getPersonList());
        CommandResult commandResult = command.execute(model);
        model.commitAddressBookIfChanged();
        boolean hasChangedData = !personsBeforeCommand.equals(model.getAddressBook().getPersonList());
        if (hasChangedData) {
            commandResult = addUnrecognisedPrefixNote(commandResult, commandText);
        }

        saveAddressBook(command, commandResult, hasChangedData);
        return commandResult;
    }

    /**
     * Saves the address book. If saving fails after {@code command} changed the data, the error also shows
     * {@code commandResult} and says the change was kept, since repeating a command such as {@code delete 1}
     * would change another supporter. It names {@code redo} as the way to reverse an {@code undo}.
     */
    private void saveAddressBook(Command command, CommandResult commandResult, boolean hasChangedData)
            throws CommandException {
        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (IOException ioe) {
            String saveError = ioe instanceof AccessDeniedException
                    ? String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, ioe.getMessage())
                    : String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage());
            if (!hasChangedData) {
                throw new CommandException(saveError, ioe);
            }
            logger.warning("Kept a change that could not be saved: " + ioe.getMessage());
            String reversingCommandWord = command instanceof UndoCommand
                    ? RedoCommand.COMMAND_WORD
                    : UndoCommand.COMMAND_WORD;
            throw new CommandException(commandResult.getFeedbackToUser() + "\n" + saveError + "\n"
                    + String.format(MESSAGE_CHANGE_NOT_SAVED_FORMAT, reversingCommandWord), ioe);
        }
    }

    /**
     * Parses {@code commandText}. If it is rejected and contains words that look like prefixes but are not,
     * adds a note naming them to the error, since such a word is read as part of the value before it and
     * can make the error blame a field the user typed correctly.
     */
    private Command parseCommand(String commandText) throws ParseException {
        try {
            return addressBookParser.parseCommand(commandText);
        } catch (ParseException pe) {
            List<String> unrecognisedPrefixes = addressBookParser.findUnrecognisedPrefixes(commandText);
            if (unrecognisedPrefixes.isEmpty()) {
                throw pe;
            }
            logger.info("Rejected command containing unrecognised prefixes: " + unrecognisedPrefixes);
            throw new ParseException(pe.getMessage() + "\n"
                    + Messages.formatUnrecognisedPrefixErrorNote(unrecognisedPrefixes), pe);
        }
    }

    /**
     * Adds a note to {@code commandResult} if {@code commandText} contains words that look like prefixes but
     * are not, since the command has just saved them as part of a value (e.g. an address or a note).
     */
    private CommandResult addUnrecognisedPrefixNote(CommandResult commandResult, String commandText) {
        List<String> unrecognisedPrefixes = addressBookParser.findUnrecognisedPrefixes(commandText);
        if (unrecognisedPrefixes.isEmpty()) {
            return commandResult;
        }
        logger.info("Saved text containing unrecognised prefixes: " + unrecognisedPrefixes);
        return commandResult.withNote(Messages.formatUnrecognisedPrefixNote(unrecognisedPrefixes));
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public Optional<Person> getViewedPerson() {
        return model.getViewedPerson();
    }

    @Override
    public List<Person> getPersonsIntroducedBy(Name introducer) {
        return model.getPersonsIntroducedBy(introducer);
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
