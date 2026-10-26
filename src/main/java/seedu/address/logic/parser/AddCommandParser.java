package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORGANISATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STAGE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Organisation;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Stage;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                        PREFIX_ORGANISATION, PREFIX_STAGE, PREFIX_TAG);

        if (!arePrefixesPresent(argMultimap, PREFIX_NAME) || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }
        if (!arePrefixesPresent(argMultimap, PREFIX_PHONE) && !arePrefixesPresent(argMultimap, PREFIX_EMAIL)) {
            throw new ParseException(AddCommand.MESSAGE_MISSING_CONTACT);
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                PREFIX_ORGANISATION, PREFIX_STAGE);
        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
        Optional<Phone> phone = parseOptionalField(argMultimap, PREFIX_PHONE, AddCommand.MESSAGE_BLANK_PHONE,
                ParserUtil::parsePhone);
        Optional<Email> email = parseOptionalField(argMultimap, PREFIX_EMAIL, AddCommand.MESSAGE_BLANK_EMAIL,
                ParserUtil::parseEmail);
        Optional<Address> address = parseOptionalField(argMultimap, PREFIX_ADDRESS,
                AddCommand.MESSAGE_BLANK_ADDRESS, ParserUtil::parseAddress);
        Optional<Organisation> organisation = parseOptionalField(argMultimap, PREFIX_ORGANISATION,
                Organisation.MESSAGE_CONSTRAINTS, ParserUtil::parseOrganisation);
        Stage stage = argMultimap.getValue(PREFIX_STAGE).isPresent()
                ? ParserUtil.parseStage(argMultimap.getValue(PREFIX_STAGE).get())
                : Stage.DEFAULT_STAGE;
        Set<Tag> tagList = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));

        Person person = new Person(name, phone, email, address, organisation, stage, tagList);

        return new AddCommand(person);
    }

    /**
     * Parses the value of an optional field with {@code fieldParser}, or returns an empty {@code Optional}
     * if {@code prefix} was left out. A prefix given with nothing after it is rejected with
     * {@code blankMessage}, since in {@code add} the user leaves out a field the supporter does not have.
     *
     * @throws ParseException if the value is blank or invalid.
     */
    private static <T> Optional<T> parseOptionalField(ArgumentMultimap argMultimap, Prefix prefix,
            String blankMessage, FieldParser<T> fieldParser) throws ParseException {
        Optional<String> value = argMultimap.getValue(prefix);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        if (value.get().isBlank()) {
            throw new ParseException(blankMessage);
        }
        return Optional.of(fieldParser.parse(value.get()));
    }

    /**
     * Parses a field value, e.g. {@link ParserUtil#parsePhone(String)}.
     */
    @FunctionalInterface
    private interface FieldParser<T> {
        T parse(String value) throws ParseException;
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}
