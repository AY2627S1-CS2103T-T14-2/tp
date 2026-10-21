package seedu.address.model.tag;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a Tag in the address book.
 * Guarantees: immutable; name is valid as declared in {@link #isValidTagName(String)}
 */
public class Tag {

    public static final String MESSAGE_CONSTRAINTS = "Tag names should be alphanumeric";
    public static final String VALIDATION_REGEX = "\\p{Alnum}+";

    /** Orders tags alphabetically ignoring case, with tags differing only in case in a fixed order. */
    public static final Comparator<Tag> ALPHABETICAL_ORDER = Comparator
            .comparing((Tag tag) -> tag.tagName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(tag -> tag.tagName);

    public final String tagName;

    /**
     * Constructs a {@code Tag}.
     *
     * @param tagName A valid tag name.
     */
    public Tag(String tagName) {
        requireNonNull(tagName);
        checkArgument(isValidTagName(tagName), MESSAGE_CONSTRAINTS);
        this.tagName = tagName;
    }

    /**
     * Returns true if a given string is a valid tag name.
     */
    public static boolean isValidTagName(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns {@code tags} in {@link #ALPHABETICAL_ORDER}, keeping only the first of any tags that differ only in
     * case, so that e.g. {@code [VIP]} and {@code [vip]} given together count as one tag.
     */
    public static List<Tag> sortedDistinctIgnoringCase(Collection<Tag> tags) {
        requireNonNull(tags);
        List<Tag> distinctTags = new ArrayList<>();
        for (Tag tag : tags.stream().sorted(ALPHABETICAL_ORDER).toList()) {
            if (distinctTags.stream().noneMatch(tag::isSameTag)) {
                distinctTags.add(tag);
            }
        }
        return distinctTags;
    }

    /**
     * Returns true if both tags have the same name, ignoring case.
     */
    public boolean isSameTag(Tag otherTag) {
        return otherTag != null && tagName.equalsIgnoreCase(otherTag.tagName);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Tag otherTag)) {
            return false;
        }

        return tagName.equals(otherTag.tagName);
    }

    @Override
    public int hashCode() {
        return tagName.hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    public String toString() {
        return '[' + tagName + ']';
    }

}
