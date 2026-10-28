package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Reverts the most recent change to the supporter data.
 */
public class UndoCommand extends Command {

    public static final String COMMAND_WORD = "undo";

    public static final String MESSAGE_SUCCESS = "Undid the last change.";
    public static final String MESSAGE_NOTHING_TO_UNDO = "There is no change to undo.";
    public static final String MESSAGE_NO_ARGUMENTS = "undo takes no arguments and undoes one change at a time. "
            + "To undo more changes, enter undo again for each one.";

    private static final Logger logger = LogsCenter.getLogger(UndoCommand.class);

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (!model.canUndoAddressBook()) {
            throw new CommandException(MESSAGE_NOTHING_TO_UNDO);
        }

        model.undoAddressBook();
        assert model.canRedoAddressBook() : "an undone change can always be redone";
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        logger.fine("Undid the last change and showed all supporters");
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
