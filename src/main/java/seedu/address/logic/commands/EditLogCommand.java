package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.DisplayDateFormatter;
import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;

/**
 * Edits an interaction identified by its displayed number for a supporter.
 */
public class EditLogCommand extends Command {

    public static final String COMMAND_WORD = "editlog";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Edits an interaction for the supporter identified by the index "
            + "number used in the displayed supporter list.\n"
            + "Parameters: INDEX ENTRY (both must be positive integers) "
            + "[" + PREFIX_NOTE + "NOTE] [" + PREFIX_DATE + "DATE]\n"
            + "ENTRY is the interaction number shown in the detail panel. At least one field is required.\n"
            + "DATE must be a valid date in yyyy-MM-dd format, from " + Interaction.MIN_DATE
            + " through today.\n"
            + "Example: " + COMMAND_WORD + " 2 1 " + PREFIX_NOTE + "Updated meeting notes "
            + PREFIX_DATE + "2026-10-01";
    public static final String MESSAGE_SUCCESS = "Edited interaction #%1$d with %2$s (%3$s): %4$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_NOT_CHANGED =
            "Interaction #%1$d with %2$s already has those details. Nothing was changed.";

    private static final Logger logger = LogsCenter.getLogger(EditLogCommand.class);

    private final Index targetIndex;
    private final Index interactionIndex;
    private final Optional<String> note;
    private final Optional<LocalDate> date;

    /**
     * Creates a command with an optional note and validated date. Omitted fields remain unchanged.
     */
    public EditLogCommand(Index targetIndex, Index interactionIndex, Optional<String> note, Optional<LocalDate> date) {
        requireAllNonNull(targetIndex, interactionIndex, note, date);
        checkArgument(note.isPresent() || date.isPresent(), MESSAGE_NOT_EDITED);
        note.ifPresent(value -> checkArgument(Interaction.isValidNote(value), Interaction.MESSAGE_CONSTRAINTS));
        this.targetIndex = targetIndex;
        this.interactionIndex = interactionIndex;
        this.note = note.map(StringUtil::trimUnicodeWhitespace);
        this.date = date;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person personToUpdate = InteractionCommandSupport.getDisplayedPerson(
                model.getFilteredPersonList(), targetIndex);
        int storedIndex = InteractionCommandSupport.getStoredInteractionIndex(personToUpdate, interactionIndex);

        Person updatedPerson = createEditedPerson(personToUpdate, storedIndex);
        assert updatedPerson.getInteractionCount() == personToUpdate.getInteractionCount()
                : "editing must preserve the number of interactions";

        boolean hasChanges = !updatedPerson.equals(personToUpdate);
        if (hasChanges) {
            model.setPerson(personToUpdate, updatedPerson);
        }
        model.setViewedPerson(hasChanges ? updatedPerson : personToUpdate);

        int displayedEntryNumber = updatedPerson.getInteractionEntryNumber(storedIndex);
        logger.fine("Processed editlog for displayed supporter " + targetIndex.getOneBased()
                + ": interaction #" + interactionIndex.getOneBased() + " -> #" + displayedEntryNumber
                + ", changed: " + hasChanges + ", interaction count: " + updatedPerson.getInteractionCount());

        if (!hasChanges) {
            // say so, as no undo point is saved and a following undo reverts an earlier change
            return new CommandResult(String.format(MESSAGE_NOT_CHANGED, interactionIndex.getOneBased(),
                    personToUpdate.getName()));
        }

        Interaction updatedInteraction = updatedPerson.getInteractions().get(storedIndex);
        return new CommandResult(String.format(MESSAGE_SUCCESS, displayedEntryNumber,
                personToUpdate.getName(), DisplayDateFormatter.format(updatedInteraction.getDate()),
                updatedInteraction.getNote()));
    }

    /**
     * Creates a copy of {@code personToUpdate} with the interaction at the zero-based {@code storedIndex} edited.
     * Omitted fields and the entry's stored position are preserved.
     */
    private Person createEditedPerson(Person personToUpdate, int storedIndex) {
        List<Interaction> updatedInteractions = new ArrayList<>(personToUpdate.getInteractions());
        Interaction originalInteraction = updatedInteractions.get(storedIndex);
        Interaction updatedInteraction = new Interaction(date.orElse(originalInteraction.getDate()),
                note.orElse(originalInteraction.getNote()));
        // retain the logging position for equal-date ordering and distinguish identical interactions
        updatedInteractions.set(storedIndex, updatedInteraction);
        return personToUpdate.withInteractions(updatedInteractions);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditLogCommand otherEditLogCommand)) {
            return false;
        }

        return targetIndex.equals(otherEditLogCommand.targetIndex)
                && interactionIndex.equals(otherEditLogCommand.interactionIndex)
                && note.equals(otherEditLogCommand.note)
                && date.equals(otherEditLogCommand.date);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("interactionIndex", interactionIndex)
                .add("note", note)
                .add("date", date)
                .toString();
    }
}
