package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Adds or removes some tags of a supporter identified using its displayed index, keeping their other tags.
 * Tags that are already as the user wants (e.g. a tag the supporter already has) are skipped and reported.
 */
public abstract class TagChangeCommand extends Command {

    private static final Logger logger = LogsCenter.getLogger(TagChangeCommand.class);

    protected final Index targetIndex;
    protected final List<Tag> tags;

    /**
     * Creates a command that changes {@code tags} of the supporter at {@code targetIndex}.
     */
    protected TagChangeCommand(Index targetIndex, Set<Tag> tags) {
        requireAllNonNull(targetIndex, tags);
        assert !tags.isEmpty() : "at least one tag must be given";
        this.targetIndex = targetIndex;
        // tags differing only in case count as one, so t/VIP t/vip never reports one of them as skipped
        this.tags = List.copyOf(Tag.sortedDistinctIgnoringCase(tags));
    }

    /**
     * Adds or removes {@code tag} by changing {@code supporterTags} in place, and returns the tags actually
     * added or removed, or an empty list if {@code tag} is skipped.
     * Removed tags are returned as stored on the supporter, so their case may differ from {@code tag}.
     * {@code execute} relies on both the change to {@code supporterTags} and the returned list.
     */
    protected abstract List<Tag> changeTag(Set<Tag> supporterTags, Tag tag);

    /** Returns the message for changed tags, with the supporter's name and the tags. */
    protected abstract String getChangedMessage();

    /** Returns the message for skipped tags, with the tags as its argument. */
    protected abstract String getSkippedMessage();

    /** Returns the message for when every tag is skipped, with the supporter's name and the tags. */
    protected abstract String getNothingChangedMessage();

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person target = lastShownList.get(targetIndex.getZeroBased());
        Set<Tag> updatedTags = new HashSet<>(target.getTags());
        List<Tag> changedTags = new ArrayList<>();
        List<Tag> skippedTags = new ArrayList<>();
        for (Tag tag : tags) {
            List<Tag> tagsChanged = changeTag(updatedTags, tag);
            if (tagsChanged.isEmpty()) {
                skippedTags.add(tag);
            } else {
                changedTags.addAll(tagsChanged);
            }
        }

        if (changedTags.isEmpty()) {
            // the user's intention is already true, so this is a normal result rather than an error
            model.setViewedPerson(target);
            return new CommandResult(String.format(getNothingChangedMessage(), target.getName(),
                    Messages.formatTags(skippedTags)));
        }

        Person updatedPerson = target.withTags(updatedTags);
        model.setPerson(target, updatedPerson);
        model.setViewedPerson(updatedPerson);
        logger.fine("Changed tags of " + target.getName() + ": " + changedTags + ", skipped " + skippedTags);
        return new CommandResult(createChangedMessage(target, changedTags, skippedTags));
    }

    private String createChangedMessage(Person target, List<Tag> changedTags, List<Tag> skippedTags) {
        String message = String.format(getChangedMessage(), target.getName(),
                Messages.formatTags(changedTags));
        if (!skippedTags.isEmpty()) {
            message += "\n" + String.format(getSkippedMessage(), Messages.formatTags(skippedTags));
        }
        return message;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // a TagCommand never equals an UntagCommand, even with the same index and tags
        if (other == null || other.getClass() != getClass()) {
            return false;
        }

        TagChangeCommand otherCommand = (TagChangeCommand) other;
        return targetIndex.equals(otherCommand.targetIndex)
                && tags.equals(otherCommand.tags);
    }
}
