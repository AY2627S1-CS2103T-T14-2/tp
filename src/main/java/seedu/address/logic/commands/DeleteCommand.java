package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Deletes a supporter and their logged interactions using the displayed index.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the supporter identified by the index number used in the displayed supporter list, "
            + "together with their logged interactions.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted supporter: %1$s\n"
            + "%2$d logged interaction(s) were deleted with them.";
    public static final String MESSAGE_INTRODUCTIONS_CLEARED =
            "\nThey were also removed as the introducer of %1$d supporter(s).";

    private final Index targetIndex;

    /**
     * Creates a command to delete the supporter at {@code targetIndex} in the displayed list.
     */
    public DeleteCommand(Index targetIndex) {
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToDelete = lastShownList.get(targetIndex.getZeroBased());
        int interactionCount = personToDelete.getInteractions().size();

        model.deletePerson(personToDelete);
        int clearedIntroductions = model.replaceIntroducer(personToDelete.getName(), Optional.empty());

        String message = String.format(MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete),
                interactionCount);
        if (clearedIntroductions > 0) {
            message += String.format(MESSAGE_INTRODUCTIONS_CLEARED, clearedIntroductions);
        }
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetIndex.equals(otherDeleteCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
