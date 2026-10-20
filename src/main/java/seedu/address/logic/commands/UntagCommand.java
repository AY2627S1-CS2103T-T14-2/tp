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
 * Removes tags from a supporter identified using its displayed index, keeping their other tags.
 */
public class UntagCommand extends Command {

    public static final String COMMAND_WORD = "untag";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Removes tags from the supporter identified "
            + "by the index number used in the displayed supporter list, keeping their other tags. "
            + "Case is ignored, and tags they do not have are skipped.\n"
            + "Parameters: INDEX (must be a positive integer) " + PREFIX_TAG + "TAG [" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_TAG + "vip";

    public static final String MESSAGE_TAGS_REMOVED = "Removed tags from %1$s: %2$s";
    public static final String MESSAGE_TAGS_SKIPPED = "Skipped tags they do not have: %1$s";
    public static final String MESSAGE_NOTHING_REMOVED = "%1$s has none of these tags: %2$s. Nothing was changed.";

    private final Index targetIndex;
    private final Set<Tag> tagsToRemove;

    /**
     * Creates a command that removes {@code tagsToRemove} from the supporter at {@code targetIndex}.
     */
    public UntagCommand(Index targetIndex, Set<Tag> tagsToRemove) {
        requireAllNonNull(targetIndex, tagsToRemove);
        assert !tagsToRemove.isEmpty() : "at least one tag must be given";
        this.targetIndex = targetIndex;
        this.tagsToRemove = new HashSet<>(tagsToRemove);
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
        List<Tag> removedTags = new ArrayList<>();
        List<Tag> skippedTags = new ArrayList<>();
        for (Tag tag : tagsToRemove.stream().sorted(Tag.ALPHABETICAL_ORDER).toList()) {
            // the message shows removed tags as the supporter had them, e.g. [VIP] for t/vip
            List<Tag> matchingTags = updatedTags.stream().filter(tag::isSameTag).toList();
            if (matchingTags.isEmpty()) {
                skippedTags.add(tag);
            } else {
                matchingTags.forEach(updatedTags::remove);
                removedTags.addAll(matchingTags);
            }
        }

        if (removedTags.isEmpty()) {
            // the user's intention is already true, so this is reported as a normal result rather than an error
            model.setViewedPerson(target);
            return new CommandResult(String.format(MESSAGE_NOTHING_REMOVED, target.getName(),
                    Messages.formatTags(skippedTags)));
        }

        Person updatedPerson = target.withTags(updatedTags);
        model.setPerson(target, updatedPerson);
        model.setViewedPerson(updatedPerson);

        String message = String.format(MESSAGE_TAGS_REMOVED, target.getName(), Messages.formatTags(removedTags));
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
        if (!(other instanceof UntagCommand otherUntagCommand)) {
            return false;
        }

        return targetIndex.equals(otherUntagCommand.targetIndex)
                && tagsToRemove.equals(otherUntagCommand.tagsToRemove);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("tagsToRemove", tagsToRemove)
                .toString();
    }
}
