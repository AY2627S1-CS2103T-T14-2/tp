package seedu.address.logic.commands;

import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Removes tags from a supporter identified using its displayed index, keeping their other tags.
 */
public class UntagCommand extends TagChangeCommand {

    public static final String COMMAND_WORD = "untag";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Removes tags from the supporter identified "
            + "by the index number used in the displayed supporter list, keeping their other tags. "
            + "Case is ignored, and tags they do not have are skipped.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + PREFIX_TAG + "TAG [" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_TAG + "vip";

    public static final String MESSAGE_TAGS_REMOVED = "Removed tags from %1$s: %2$s";
    public static final String MESSAGE_TAGS_SKIPPED = "Skipped tags they do not have: %1$s";
    public static final String MESSAGE_NOTHING_REMOVED =
            "%1$s has none of these tags: %2$s. Nothing was changed.";

    /**
     * Creates a command that removes {@code tagsToRemove} from the supporter at {@code targetIndex}.
     */
    public UntagCommand(Index targetIndex, Set<Tag> tagsToRemove) {
        super(targetIndex, tagsToRemove);
    }

    @Override
    protected List<Tag> changeTag(Set<Tag> supporterTags, Tag tag) {
        // the message shows removed tags as the supporter had them, e.g. [VIP] for t/vip
        List<Tag> matchingTags = supporterTags.stream().filter(tag::isSameTag).toList();
        matchingTags.forEach(supporterTags::remove);
        return matchingTags;
    }

    @Override
    protected String getChangedMessage() {
        return MESSAGE_TAGS_REMOVED;
    }

    @Override
    protected String getSkippedMessage() {
        return MESSAGE_TAGS_SKIPPED;
    }

    @Override
    protected String getNothingChangedMessage() {
        return MESSAGE_NOTHING_REMOVED;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("tagsToRemove", tags)
                .toString();
    }
}
