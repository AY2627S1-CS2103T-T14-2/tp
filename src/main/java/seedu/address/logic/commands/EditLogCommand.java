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

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.DisplayDateFormatter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
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
            + "DATE must be a valid date in yyyy-MM-dd format, from 1900-01-01 through today.\n"
            + "Example: " + COMMAND_WORD + " 2 1 " + PREFIX_NOTE + "Updated meeting notes "
            + PREFIX_DATE + "2026-10-01";
    public static final String MESSAGE_SUCCESS = "Edited interaction #%1$d with %2$s (%3$s): %4$s.";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_NOT_CHANGED =
            "Interaction #%1$d with %2$s already has those details. Nothing was changed.";

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
        this.note = note.map(String::trim);
        this.date = date;
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
            throw new CommandException(Messages.MESSAGE_INVALID_INTERACTION_INDEX);
        }

        int storedIndex = personToUpdate.getInteractionStoredIndex(interactionIndex.getOneBased());
        Interaction originalInteraction = updatedInteractions.get(storedIndex);
        Interaction updatedInteraction = new Interaction(date.orElse(originalInteraction.getDate()),
                note.orElse(originalInteraction.getNote()));
        // retain the logging position for equal-date ordering and distinguish identical interactions
        updatedInteractions.set(storedIndex, updatedInteraction);
        Person updatedPerson = personToUpdate.withInteractions(updatedInteractions);
        if (updatedPerson.equals(personToUpdate)) {
            // say so, as no undo point is saved and a following undo reverts an earlier change
            model.setViewedPerson(personToUpdate);
            return new CommandResult(String.format(MESSAGE_NOT_CHANGED, interactionIndex.getOneBased(),
                    personToUpdate.getName()));
        }
        model.setPerson(personToUpdate, updatedPerson);
        model.setViewedPerson(updatedPerson);

        int displayedEntryNumber = updatedPerson.getInteractionEntryNumber(storedIndex);
        return new CommandResult(String.format(MESSAGE_SUCCESS, displayedEntryNumber,
                personToUpdate.getName(), DisplayDateFormatter.format(updatedInteraction.getDate()),
                updatedInteraction.getNote()));
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
