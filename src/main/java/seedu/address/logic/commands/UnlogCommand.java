package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
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
    public static final String MESSAGE_INVALID_INTERACTION_INDEX = "The interaction number provided is invalid.";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.ENGLISH);

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
        List<Person> lastShownList = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUpdate = lastShownList.get(targetIndex.getZeroBased());
        List<Interaction> updatedInteractions = new ArrayList<>(personToUpdate.getInteractions());
        if (interactionIndex.getZeroBased() >= updatedInteractions.size()) {
            throw new CommandException(MESSAGE_INVALID_INTERACTION_INDEX);
        }

        // entry numbers descend in display order, so size minus entry number gives the display position
        int storedIndex = personToUpdate.getInteractionIndicesInDisplayOrder()
                .get(updatedInteractions.size() - interactionIndex.getOneBased());
        // remove by position so identical interactions remain distinguishable
        Interaction removedInteraction = updatedInteractions.remove(storedIndex);
        Person updatedPerson = new Person(personToUpdate.getName(), personToUpdate.getPhone(),
                personToUpdate.getEmail(), personToUpdate.getAddress(), personToUpdate.getOrganisation(),
                personToUpdate.getIntroducer(), personToUpdate.getStage(), updatedInteractions,
                personToUpdate.getTags());
        model.setPerson(personToUpdate, updatedPerson);
        model.setViewedPerson(updatedPerson);

        return new CommandResult(String.format(MESSAGE_SUCCESS, interactionIndex.getOneBased(),
                personToUpdate.getName(), removedInteraction.getDate().format(DATE_FORMATTER),
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
