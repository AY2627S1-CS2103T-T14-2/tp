package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.person.Person;

/**
 * Resolves supporters and interaction entries selected by interaction commands.
 */
final class InteractionCommandSupport {

    private InteractionCommandSupport() {
    }

    /**
     * Returns the supporter at {@code supporterIndex} in the currently displayed supporter list.
     * The supplied list reflects the current filter and ordering.
     *
     * @throws CommandException if the index does not identify a displayed supporter.
     */
    static Person getDisplayedPerson(List<Person> displayedPersons, Index supporterIndex) throws CommandException {
        requireAllNonNull(displayedPersons, supporterIndex);
        if (supporterIndex.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        return displayedPersons.get(supporterIndex.getZeroBased());
    }

    /**
     * Returns the zero-based position in the stored interaction list
     * for the chronological entry identified by {@code entryIndex}.
     * User-facing entry numbers start at 1 for the oldest interaction;
     * equal dates are numbered in logging order.
     * Delegates the conversion to {@link Person#getInteractionStoredIndex(int)}.
     *
     * @throws CommandException if the entry does not identify an existing interaction.
     */
    static int getStoredInteractionIndex(Person person, Index entryIndex) throws CommandException {
        requireAllNonNull(person, entryIndex);
        if (entryIndex.getZeroBased() >= person.getInteractionCount()) {
            throw new CommandException(Messages.MESSAGE_INVALID_INTERACTION_INDEX);
        }
        return person.getInteractionStoredIndex(entryIndex.getOneBased());
    }
}
