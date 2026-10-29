package seedu.address.logic;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The supporter index provided is invalid.";
    public static final String MESSAGE_INVALID_INTERACTION_INDEX = "The interaction number provided is invalid.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d supporter(s) listed!";
    public static final String MESSAGE_UNRECOGNISED_PREFIX =
            "Note: %1$s is not a recognised prefix for this command, "
                    + "so it was saved as part of the value before it. "
                    + "If this was a typo, run undo and enter the command again.";
    public static final String MESSAGE_UNRECOGNISED_PREFIXES =
            "Note: %1$s are not recognised prefixes for this command, "
                    + "so they were saved as part of the values before them. "
                    + "If this was a typo, run undo and enter the command again.";
    public static final String MESSAGE_UNRECOGNISED_PREFIX_IN_ERROR =
            "Note: %1$s is not a recognised prefix for this command, "
                    + "so it was read as part of the value before it.";
    public static final String MESSAGE_UNRECOGNISED_PREFIXES_IN_ERROR =
            "Note: %1$s are not recognised prefixes for this command, "
                    + "so they were read as part of the values before them.";
    public static final String MESSAGE_DUPLICATE_PERSON = "A supporter named %1$s already exists. "
            + "If this is a different person, add something to tell them apart, e.g. n/%1$s DBS";
    public static final String MESSAGE_DUPLICATE_FIELDS =
            "Multiple values specified for the following single-valued field(s): ";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields = Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Returns an error message naming the supporter in {@code existingPersons} who
     * counts as the same
     * supporter as {@code person}, so the user can see which record they clashed
     * with.
     */
    public static String getErrorMessageForDuplicatePerson(List<Person> existingPersons, Person person) {
        Name existingName = existingPersons.stream()
                .filter(person::isSamePerson)
                .findFirst()
                .map(Person::getName)
                .orElse(person.getName());
        return String.format(MESSAGE_DUPLICATE_PERSON, existingName);
    }

    /**
     * Formats {@code tags} for display, in alphabetical order ignoring case, e.g. {@code [boardIntro] [vip]}.
     */
    public static String formatTags(Collection<Tag> tags) {
        return tags.stream()
                .sorted(Tag.ALPHABETICAL_ORDER)
                .map(Tag::toString)
                .collect(Collectors.joining(" "));
    }

    /**
     * Returns a note pointing out {@code unrecognisedPrefixes}, e.g. {@code em/}, which were saved as text.
     */
    public static String formatUnrecognisedPrefixNote(List<String> unrecognisedPrefixes) {
        return formatPrefixNote(unrecognisedPrefixes,
                MESSAGE_UNRECOGNISED_PREFIX, MESSAGE_UNRECOGNISED_PREFIXES);
    }

    /**
     * Returns a note pointing out {@code unrecognisedPrefixes}, e.g. {@code x/}, which were read as part of
     * a value that was then rejected, so the user knows the error may come from a mistyped prefix.
     */
    public static String formatUnrecognisedPrefixErrorNote(List<String> unrecognisedPrefixes) {
        return formatPrefixNote(unrecognisedPrefixes,
                MESSAGE_UNRECOGNISED_PREFIX_IN_ERROR, MESSAGE_UNRECOGNISED_PREFIXES_IN_ERROR);
    }

    private static String formatPrefixNote(List<String> prefixes, String oneFormat, String manyFormat) {
        assert !prefixes.isEmpty() : "only called when an unrecognised prefix was found";
        if (prefixes.size() == 1) {
            return String.format(oneFormat, prefixes.get(0));
        }
        return String.format(manyFormat, String.join(", ", prefixes));
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Stage: ")
                .append(person.getStage());
        // an optional field is left out of the message when the supporter does not have it
        person.getPhone().ifPresent(phone -> builder.append("; Phone: ").append(phone));
        person.getEmail().ifPresent(email -> builder.append("; Email: ").append(email));
        person.getAddress().ifPresent(address -> builder.append("; Address: ").append(address));
        person.getOrganisation().ifPresent(organisation -> builder.append("; Organisation: ").append(organisation));
        if (!person.getTags().isEmpty()) {
            builder.append("; Tags: ").append(formatTags(person.getTags()));
        }
        return builder.toString();
    }

}
