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
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.ArgumentTokenizer;
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

        Command command = addressBookParser.parseCommand(commandText);
        List<Person> personsBeforeCommand = List.copyOf(model.getAddressBook().getPersonList());
        CommandResult commandResult = command.execute(model);
        model.commitAddressBookIfChanged();
        if (!personsBeforeCommand.equals(model.getAddressBook().getPersonList())) {
            commandResult = addUnrecognisedPrefixNote(commandResult, commandText);
        }

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        return commandResult;
    }

    /**
     * Adds a note to {@code commandResult} if {@code commandText} contains words that look like prefixes but
     * are not, since the command has just saved them as part of a value (e.g. an address or a note).
     */
    private CommandResult addUnrecognisedPrefixNote(CommandResult commandResult, String commandText) {
        List<String> unrecognisedPrefixes = ArgumentTokenizer.findUnrecognisedPrefixes(commandText);
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
