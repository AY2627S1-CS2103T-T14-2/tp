package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Adds tags to a supporter identified using its displayed index, keeping the tags they already have.
 */
public class TagCommand extends Command {

    public static final String COMMAND_WORD = "tag";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds tags to the supporter identified "
            + "by the index number used in the displayed supporter list, keeping their existing tags. "
            + "Tags they already have, ignoring case, are skipped.\n"
            + "Parameters: INDEX (must be a positive integer) " + PREFIX_TAG + "TAG [" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_TAG + "boardIntro " + PREFIX_TAG + "vip";

    public static final String MESSAGE_TAGS_ADDED = "Added tags to %1$s: %2$s";
    public static final String MESSAGE_TAGS_SKIPPED = "Skipped tags they already have: %1$s";
    public static final String MESSAGE_NOTHING_ADDED = "%1$s already has these tags: %2$s. Nothing was changed.";

    private final Index targetIndex;
    private final List<Tag> tagsToAdd;

    /**
     * Creates a command that adds {@code tagsToAdd} to the supporter at {@code targetIndex}.
     */
    public TagCommand(Index targetIndex, Set<Tag> tagsToAdd) {
        requireAllNonNull(targetIndex, tagsToAdd);
        assert !tagsToAdd.isEmpty() : "at least one tag must be given";
        this.targetIndex = targetIndex;
        // tags differing only in case count as one, so t/VIP t/vip never reports one of them as skipped
        this.tagsToAdd = Tag.sortedDistinctIgnoringCase(tagsToAdd);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person target = lastShownList.get(targetIndex.getZeroBased());
        Set<Tag> updatedTags = new HashSet<>(target.getTags());
        List<Tag> addedTags = new ArrayList<>();
        List<Tag> skippedTags = new ArrayList<>();
        for (Tag tag : tagsToAdd) {
            if (updatedTags.stream().anyMatch(tag::isSameTag)) {
                skippedTags.add(tag);
            } else {
                updatedTags.add(tag);
                addedTags.add(tag);
            }
        }

        if (addedTags.isEmpty()) {
            // the user's intention is already true, so this is reported as a normal result rather than an error
            model.setViewedPerson(target);
            return new CommandResult(String.format(MESSAGE_NOTHING_ADDED, target.getName(),
                    Messages.formatTags(skippedTags)));
        }

        Person updatedPerson = target.withTags(updatedTags);
        model.setPerson(target, updatedPerson);
        model.setViewedPerson(updatedPerson);

        String message = String.format(MESSAGE_TAGS_ADDED, target.getName(), Messages.formatTags(addedTags));
        if (!skippedTags.isEmpty()) {
            message += "\n" + String.format(MESSAGE_TAGS_SKIPPED, Messages.formatTags(skippedTags));
        }
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TagCommand otherTagCommand)) {
            return false;
        }

        return targetIndex.equals(otherTagCommand.targetIndex)
                && tagsToAdd.equals(otherTagCommand.tagsToAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("tagsToAdd", tagsToAdd)
                .toString();
    }
}
