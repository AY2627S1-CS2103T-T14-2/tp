package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.DisplayDateFormatter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;

/**
 * Deletes an interaction identified by its displayed number from a supporter.
 */
public class UnlogCommand extends Command {

    public static final String COMMAND_WORD = "unlog";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes an interaction from the supporter identified by the index "
            + "number used in the displayed supporter list.\n"
            + "Parameters: INDEX ENTRY (both must be positive integers)\n"
            + "ENTRY is the interaction number shown in the detail panel.\n"
            + "Example: " + COMMAND_WORD + " 2 1";
    public static final String MESSAGE_SUCCESS = "Deleted interaction #%1$d with %2$s (%3$s): %4$s.";

    private static final Logger logger = LogsCenter.getLogger(UnlogCommand.class);

    private final Index targetIndex;
    private final Index interactionIndex;

    /**
     * Creates a command using the displayed supporter index and chronological interaction number.
     */
    public UnlogCommand(Index targetIndex, Index interactionIndex) {
        requireAllNonNull(targetIndex, interactionIndex);
        this.targetIndex = targetIndex;
        this.interactionIndex = interactionIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person personToUpdate = InteractionCommandSupport.getDisplayedPerson(
                model.getFilteredPersonList(), targetIndex);
        int storedIndex = InteractionCommandSupport.getStoredInteractionIndex(personToUpdate, interactionIndex);

        List<Interaction> updatedInteractions = new ArrayList<>(personToUpdate.getInteractions());
        // remove by position so identical interactions remain distinguishable
        Interaction removedInteraction = updatedInteractions.remove(storedIndex);

        Person updatedPerson = personToUpdate.withInteractions(updatedInteractions);
        assert updatedPerson.getInteractionCount() == personToUpdate.getInteractionCount() - 1
                : "removing must delete exactly one interaction";

        model.setPerson(personToUpdate, updatedPerson);
        model.setViewedPerson(updatedPerson);

        logger.fine("Removed interaction #" + interactionIndex.getOneBased() + " from displayed supporter "
                + targetIndex.getOneBased() + " (interaction count: " + updatedPerson.getInteractionCount() + ")");

        return new CommandResult(String.format(MESSAGE_SUCCESS, interactionIndex.getOneBased(),
                personToUpdate.getName(), DisplayDateFormatter.format(removedInteraction.getDate()),
                removedInteraction.getNote()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UnlogCommand otherUnlogCommand)) {
            return false;
        }

        return targetIndex.equals(otherUnlogCommand.targetIndex)
                && interactionIndex.equals(otherUnlogCommand.interactionIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("interactionIndex", interactionIndex)
                .toString();
    }
}
