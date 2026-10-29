package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
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

    private static final Logger logger = LogsCenter.getLogger(ViewCommand.class);

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
        List<Person> displayedPersons = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= displayedPersons.size()) {
            logger.fine(() -> "Rejected view: displayed index=" + targetIndex.getOneBased()
                    + ", displayed supporters=" + displayedPersons.size());
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToView = displayedPersons.get(targetIndex.getZeroBased());
        model.setViewedPerson(personToView);
        assert model.getViewedPerson().filter(personToView::equals).isPresent()
                : "Viewed supporter must match the requested supporter";

        int interactionCount = personToView.getInteractionCount();
        logger.fine(() -> "Viewed supporter: displayed index=" + targetIndex.getOneBased()
                + ", interactions=" + interactionCount);
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
