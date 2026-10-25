package seedu.address.logic.commands;

import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Adds tags to a supporter identified using its displayed index, keeping the tags they already have.
 */
public class TagCommand extends TagChangeCommand {

    public static final String COMMAND_WORD = "tag";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds tags to the supporter identified "
            + "by the index number used in the displayed supporter list, keeping their existing tags. "
            + "Tags they already have, ignoring case, are skipped.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + PREFIX_TAG + "TAG [" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_TAG + "boardIntro " + PREFIX_TAG + "vip";

    public static final String MESSAGE_TAGS_ADDED = "Added tags to %1$s: %2$s";
    public static final String MESSAGE_TAGS_SKIPPED = "Skipped tags they already have: %1$s";
    public static final String MESSAGE_NOTHING_ADDED =
            "%1$s already has these tags: %2$s. Nothing was changed.";

    /**
     * Creates a command that adds {@code tagsToAdd} to the supporter at {@code targetIndex}.
     */
    public TagCommand(Index targetIndex, Set<Tag> tagsToAdd) {
        super(targetIndex, tagsToAdd);
    }

    @Override
    protected List<Tag> changeTag(Set<Tag> supporterTags, Tag tag) {
        if (supporterTags.stream().anyMatch(tag::isSameTag)) {
            return List.of();
        }
        supporterTags.add(tag);
        return List.of(tag);
    }

    @Override
    protected String getChangedMessage() {
        return MESSAGE_TAGS_ADDED;
    }

    @Override
    protected String getSkippedMessage() {
        return MESSAGE_TAGS_SKIPPED;
    }

    @Override
    protected String getNothingChangedMessage() {
        return MESSAGE_NOTHING_ADDED;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("tagsToAdd", tags)
                .toString();
    }
}
