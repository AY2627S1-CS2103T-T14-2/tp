package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
 * Logs an interaction with a supporter identified using their displayed index.
 */
public class LogCommand extends Command {

    public static final String COMMAND_WORD = "log";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Logs an interaction with the supporter identified by the index "
            + "number used in the displayed supporter list.\n"
            + "Parameters: INDEX (must be a positive integer) " + PREFIX_NOTE + "NOTE [" + PREFIX_DATE + "DATE]\n"
            + "DATE must be a valid date in yyyy-MM-dd format, from 1900-01-01 through today; defaults to today.\n"
            + "Example: " + COMMAND_WORD + " 2 " + PREFIX_NOTE + "Coffee at Raffles Place "
            + PREFIX_DATE + "2026-10-01";

    public static final String MESSAGE_SUCCESS = "Logged interaction #%1$d with %2$s (%3$s): %4$s.";

    private static final Logger logger = LogsCenter.getLogger(LogCommand.class);

    private final Index targetIndex;
    private final String note;
    private final Optional<LocalDate> date;
    private final Clock clock;

    /**
     * Creates a {@code LogCommand} that dates the interaction using the computer's clock.
     */
    public LogCommand(Index targetIndex, String note) {
        this(targetIndex, note, Clock.systemDefaultZone());
    }

    /**
     * Creates a {@code LogCommand} with a validated explicit date, or today's date at execution if absent.
     */
    public LogCommand(Index targetIndex, String note, Optional<LocalDate> date) {
        this(targetIndex, note, date, Clock.systemDefaultZone());
    }

    /**
     * Creates a {@code LogCommand} that dates the interaction using {@code clock}.
     */
    LogCommand(Index targetIndex, String note, Clock clock) {
        this(targetIndex, note, Optional.empty(), clock);
    }

    /**
     * Creates a {@code LogCommand} with a validated explicit date, falling back to {@code clock} at execution.
     */
    LogCommand(Index targetIndex, String note, Optional<LocalDate> date, Clock clock) {
        requireAllNonNull(targetIndex, note, date, clock);
        checkArgument(Interaction.isValidNote(note), Interaction.MESSAGE_CONSTRAINTS);
        this.targetIndex = targetIndex;
        this.note = note.trim();
        this.date = date;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person personToUpdate = InteractionCommandSupport.getDisplayedPerson(
                model.getFilteredPersonList(), targetIndex);

        Interaction interaction = new Interaction(date.orElseGet(() -> LocalDate.now(clock)), note);
        List<Interaction> updatedInteractions = new ArrayList<>(personToUpdate.getInteractions());
        updatedInteractions.add(interaction);

        Person updatedPerson = personToUpdate.withInteractions(updatedInteractions);
        assert updatedPerson.getInteractionCount() == personToUpdate.getInteractionCount() + 1
                : "logging must add exactly one interaction";

        model.setPerson(personToUpdate, updatedPerson);
        model.setViewedPerson(updatedPerson);

        int displayedEntryNumber = updatedPerson.getInteractionEntryNumber(updatedInteractions.size() - 1);
        logger.fine("Added interaction #" + displayedEntryNumber + " to displayed supporter "
                + targetIndex.getOneBased() + " (interaction count: " + updatedPerson.getInteractionCount() + ")");

        return new CommandResult(String.format(MESSAGE_SUCCESS, displayedEntryNumber, personToUpdate.getName(),
                DisplayDateFormatter.format(interaction.getDate()), interaction.getNote()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof LogCommand otherLogCommand)) {
            return false;
        }

        return targetIndex.equals(otherLogCommand.targetIndex)
                && note.equals(otherLogCommand.note)
                && date.equals(otherLogCommand.date);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("note", note)
                .add("date", date)
                .toString();
    }
}
