package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Finds and lists supporters matching the specified name keywords and field filters.
 * Alternatives within a field use OR; different fields use AND.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds supporters by name or other fields. "
            + "Use OR within a field and AND between fields.\n"
            + "Parameters: [KEYWORD]... [o/ORG]... [t/TAG]... [st/STAGE]... [p/PHONE]... [e/EMAIL]...\n"
            + "Supply at least one nonempty criterion. Place name keywords before all prefixes. "
            + "Text searches ignore case and match substrings; stages match full names. "
            + "Phone searches ignore plus signs, spaces and hyphens.\n"
            + "Example: " + COMMAND_WORD + " tan wei o/DBS st/giving st/lapsed";

    private final Predicate<Person> predicate;

    /**
     * Creates a command that lists supporters matching the given predicate.
     */
    public FindCommand(Predicate<Person> predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
