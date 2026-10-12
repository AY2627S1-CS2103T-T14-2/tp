package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Stage;

/**
 * Sets the cultivation stage of a supporter identified using its displayed index.
 */
public class StageCommand extends Command {

    public static final String COMMAND_WORD = "stage";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Sets the cultivation stage of the supporter identified by the index number "
            + "used in the displayed supporter list.\n"
            + "Parameters: INDEX (must be a positive integer) STAGE\n"
            + "Stages: prospect, contacted, cultivating, giving, lapsed, declined\n"
            + "Example: " + COMMAND_WORD + " 2 cultivating";

    public static final String MESSAGE_STAGE_CHANGED = "Changed stage of %1$s: %2$s -> %3$s.";
    public static final String MESSAGE_ALREADY_AT_STAGE = "%1$s is already at stage %2$s. Nothing was changed.";

    private final Index targetIndex;
    private final Stage stage;

    /**
     * Creates a StageCommand to set the supporter at {@code targetIndex} to {@code stage}.
     */
    public StageCommand(Index targetIndex, Stage stage) {
        requireAllNonNull(targetIndex, stage);
        this.targetIndex = targetIndex;
        this.stage = stage;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUpdate = lastShownList.get(targetIndex.getZeroBased());
        Stage oldStage = personToUpdate.getStage();

        // the user's intention is already true, so this is reported as a normal result rather than an error
        if (oldStage == stage) {
            return new CommandResult(String.format(MESSAGE_ALREADY_AT_STAGE, personToUpdate.getName(), stage));
        }

        Person updatedPerson = new Person(personToUpdate.getName(), personToUpdate.getPhone(),
                personToUpdate.getEmail(), personToUpdate.getAddress(), personToUpdate.getOrganisation(), stage,
                personToUpdate.getInteractions(), personToUpdate.getTags());
        model.setPerson(personToUpdate, updatedPerson);

        return new CommandResult(String.format(MESSAGE_STAGE_CHANGED, personToUpdate.getName(), oldStage, stage));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof StageCommand otherStageCommand)) {
            return false;
        }

        return targetIndex.equals(otherStageCommand.targetIndex)
                && stage.equals(otherStageCommand.stage);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("stage", stage)
                .toString();
    }
}
