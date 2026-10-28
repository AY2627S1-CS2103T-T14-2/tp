package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Selects a supporter identified using their displayed index and summarizes their interaction history.
 */
public class ViewCommand extends Command {

    public static final String COMMAND_WORD = "view";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows the details and interaction history of the supporter identified by the "
            + "index number used in the displayed supporter list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 2";

    public static final String MESSAGE_SUCCESS = "Showing %1$s: %2$d interaction(s) logged";
    public static final String MESSAGE_NO_INTERACTIONS = "Showing %1$s: no interactions logged yet";

    private final Index targetIndex;

    /**
     * Creates a {@code ViewCommand} to view the supporter at {@code targetIndex} in the displayed list.
     */
    public ViewCommand(Index targetIndex) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToView = lastShownList.get(targetIndex.getZeroBased());
        model.setViewedPerson(personToView);

        int interactionCount = personToView.getInteractionCount();
        if (interactionCount == 0) {
            return new CommandResult(String.format(MESSAGE_NO_INTERACTIONS, personToView.getName()));
        }

        return new CommandResult(String.format(MESSAGE_SUCCESS, personToView.getName(), interactionCount));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ViewCommand otherViewCommand)) {
            return false;
        }

        return targetIndex.equals(otherViewCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
