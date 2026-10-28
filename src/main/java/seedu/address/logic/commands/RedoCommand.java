package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Re-applies the most recently undone change to the supporter data.
 */
public class RedoCommand extends Command {

    public static final String COMMAND_WORD = "redo";

    public static final String MESSAGE_SUCCESS = "Redid the last undone change.";
    public static final String MESSAGE_REDID_CHANGE = "Redid the last undone change: %1$s";
    public static final String MESSAGE_NOTHING_TO_REDO = "There is no undone change to redo.";
    public static final String MESSAGE_NO_ARGUMENTS = "redo takes no arguments and redoes one change at a time. "
            + "To redo more changes, enter redo again for each one.";

    private static final Logger logger = LogsCenter.getLogger(RedoCommand.class);

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (!model.canRedoAddressBook()) {
            throw new CommandException(MESSAGE_NOTHING_TO_REDO);
        }

        String redoneChange = model.redoAddressBook();
        assert model.canUndoAddressBook() : "a redone change can always be undone";
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        logger.fine("Redid the last undone change and showed all supporters");
        return new CommandResult(redoneChange.isEmpty()
                ? MESSAGE_SUCCESS
                : String.format(MESSAGE_REDID_CHANGE, redoneChange));
    }
}
