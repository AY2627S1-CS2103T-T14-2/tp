package seedu.address.logic;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The supporter index provided is invalid.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d supporter(s) listed!";
    public static final String MESSAGE_DUPLICATE_PERSON = "A supporter named %1$s already exists. "
            + "If this is a different person, add something to tell them apart, e.g. n/%1$s DBS";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Returns an error message naming the supporter in {@code existingPersons} who counts as the same
     * supporter as {@code person}, so the user can see which record they clashed with.
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
            builder.append("; Tags: ");
            person.getTags().forEach(builder::append);
        }
        return builder.toString();
    }

}
