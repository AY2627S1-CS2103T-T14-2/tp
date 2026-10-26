package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INTRODUCER;

import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;

/**
 * Records, replaces or removes the supporter who introduced a supporter, using indices in the displayed list.
 */
public class IntroCommand extends Command {

    public static final String COMMAND_WORD = "intro";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Records which supporter introduced the supporter identified by the index number used in the "
            + "displayed supporter list. Leave out INTRODUCER_INDEX to remove the record.\n"
            + "Parameters: INDEX (must be a positive integer) " + PREFIX_INTRODUCER + "[INTRODUCER_INDEX]\n"
            + "INTRODUCER_INDEX must also be a positive integer when provided.\n"
            + "Example: " + COMMAND_WORD + " 3 " + PREFIX_INTRODUCER + "1";

    public static final String MESSAGE_INTRODUCER_SET = "Recorded that %1$s introduced %2$s.";
    public static final String MESSAGE_INTRODUCER_CHANGED = "Changed the introducer of %1$s: %2$s -> %3$s.";
    public static final String MESSAGE_INTRODUCER_REMOVED = "Removed %1$s as the introducer of %2$s.";
    public static final String MESSAGE_INTRODUCER_UNCHANGED =
            "%1$s is already recorded as the introducer of %2$s. Nothing was changed.";
    public static final String MESSAGE_NO_INTRODUCER = "%1$s has no introducer recorded. Nothing was changed.";
    public static final String MESSAGE_INTRODUCTION_LOOP =
            "%1$s cannot be recorded as the introducer of %2$s, "
            + "because %2$s introduced %1$s, directly or through other supporters.";

    private final Index targetIndex;
    private final Optional<Index> introducerIndex;

    /**
     * Creates a command that records the supporter at {@code introducerIndex} as the introducer of the supporter
     * at {@code targetIndex}, or removes the record if {@code introducerIndex} is empty.
     */
    public IntroCommand(Index targetIndex, Optional<Index> introducerIndex) {
        requireAllNonNull(targetIndex, introducerIndex);
        this.targetIndex = targetIndex;
        this.introducerIndex = introducerIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        Person target = getDisplayedPerson(lastShownList, targetIndex);

        Optional<Name> newIntroducer = Optional.empty();
        if (introducerIndex.isPresent()) {
            Person introducer = getDisplayedPerson(lastShownList, introducerIndex.get());
            if (introducer.isSamePerson(target)) {
                throw new CommandException(Person.MESSAGE_SELF_INTRODUCTION);
            }
            if (model.isIntroducedThrough(introducer, target.getName())) {
                throw new CommandException(String.format(MESSAGE_INTRODUCTION_LOOP, introducer.getName(),
                        target.getName()));
            }
            newIntroducer = Optional.of(introducer.getName());
        }

        Optional<Name> oldIntroducer = target.getIntroducer();
        if (isSameIntroducer(oldIntroducer, newIntroducer)) {
            // the user's intention is already true, so this is reported as a normal result rather than an error
            model.setViewedPerson(target);
            return new CommandResult(createUnchangedMessage(target, oldIntroducer));
        }

        Person updatedPerson = target.withIntroducer(newIntroducer);
        model.setPerson(target, updatedPerson);
        model.setViewedPerson(updatedPerson);
        return new CommandResult(createChangedMessage(target, oldIntroducer, newIntroducer));
    }

    private static Person getDisplayedPerson(List<Person> lastShownList, Index index) throws CommandException {
        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        return lastShownList.get(index.getZeroBased());
    }

    private static boolean isSameIntroducer(Optional<Name> oldIntroducer, Optional<Name> newIntroducer) {
        if (oldIntroducer.isEmpty() || newIntroducer.isEmpty()) {
            return oldIntroducer.isEmpty() && newIntroducer.isEmpty();
        }
        return oldIntroducer.get().isSameName(newIntroducer.get());
    }

    private static String createUnchangedMessage(Person target, Optional<Name> introducer) {
        return introducer.map(name -> String.format(MESSAGE_INTRODUCER_UNCHANGED, name, target.getName()))
                .orElseGet(() -> String.format(MESSAGE_NO_INTRODUCER, target.getName()));
    }

    private static String createChangedMessage(Person target, Optional<Name> oldIntroducer,
            Optional<Name> newIntroducer) {
        assert !isSameIntroducer(oldIntroducer, newIntroducer) : "an unchanged introducer has its own message";
        if (newIntroducer.isEmpty()) {
            return String.format(MESSAGE_INTRODUCER_REMOVED, oldIntroducer.get(), target.getName());
        }
        if (oldIntroducer.isEmpty()) {
            return String.format(MESSAGE_INTRODUCER_SET, newIntroducer.get(), target.getName());
        }
        return String.format(MESSAGE_INTRODUCER_CHANGED, target.getName(), oldIntroducer.get(), newIntroducer.get());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof IntroCommand otherIntroCommand)) {
            return false;
        }

        return targetIndex.equals(otherIntroCommand.targetIndex)
                && introducerIndex.equals(otherIntroCommand.introducerIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("introducerIndex", introducerIndex)
                .toString();
    }
}
