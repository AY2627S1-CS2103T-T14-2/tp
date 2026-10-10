package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORGANISATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STAGE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Adds a person to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a supporter to SupporterBook. "
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_ORGANISATION + "ORGANISATION] "
            + "[" + PREFIX_STAGE + "STAGE] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "At least one of " + PREFIX_PHONE + "PHONE and " + PREFIX_EMAIL + "EMAIL is required.\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "Tan Wei Ming "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_EMAIL + "weiming@dbs.com "
            + PREFIX_ORGANISATION + "DBS Bank "
            + PREFIX_TAG + "boardIntro";

    public static final String MESSAGE_MISSING_CONTACT = "A supporter needs at least one way to reach them. "
            + "Provide " + PREFIX_PHONE + "PHONE, " + PREFIX_EMAIL + "EMAIL, or both.";

    public static final String MESSAGE_SUCCESS = "New supporter added: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON = "This supporter already exists in SupporterBook.";

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasPerson(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.addPerson(toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(toAdd)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
