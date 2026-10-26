package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORGANISATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STAGE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PersonMatchesSearchPredicate;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Stage;

/**
 * Parses name keywords followed by optional, repeatable field filters into a find command.
 */
public class FindCommandParser implements Parser<FindCommand> {
    public static final String MESSAGE_INVALID_PHONE_SEARCH =
            "Phone search values must contain digits and follow the phone format: "
                    + "an optional leading +, digit groups separated by single spaces or hyphens, "
                    + "optional parentheses around digit groups, optional ext with 1 to 6 digits, "
                    + "and an optional trailing alphabetic label in parentheses";

    /**
     * Parses the given arguments into a command combining name keywords and field filters.
     *
     * @throws ParseException if criteria are empty, a stage is invalid, or a phone query is invalid.
     */
    @Override
    public FindCommand parse(String args) throws ParseException {
        // The tokenizer requires a preceding space, including when a prefix is the first argument.
        String tokenizableArgs = " " + args.replaceAll("\\s+", " ").trim();
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(tokenizableArgs,
                PREFIX_ORGANISATION, PREFIX_TAG, PREFIX_STAGE, PREFIX_PHONE, PREFIX_EMAIL);
        String preamble = arguments.getPreamble();
        List<String> nameKeywords = preamble.isEmpty() ? List.of() : List.of(preamble.split("\\s+"));
        List<String> organisations = parseSearchValues(arguments, PREFIX_ORGANISATION);
        List<String> tags = parseSearchValues(arguments, PREFIX_TAG);
        List<String> phones = parseSearchValues(arguments, PREFIX_PHONE);
        List<String> emails = parseSearchValues(arguments, PREFIX_EMAIL);
        List<Stage> stages = new ArrayList<>();
        for (String stage : arguments.getAllValues(PREFIX_STAGE)) {
            stages.add(ParserUtil.parseStage(stage));
        }

        for (String phone : phones) {
            if (!Phone.isValidPhoneSearch(phone)) {
                throw new ParseException(MESSAGE_INVALID_PHONE_SEARCH);
            }
        }
        if (Stream.of(nameKeywords, organisations, tags, stages, phones, emails).allMatch(List::isEmpty)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        return new FindCommand(new PersonMatchesSearchPredicate(nameKeywords, organisations, tags, stages,
                phones, emails));
    }

    private static List<String> parseSearchValues(ArgumentMultimap arguments, Prefix prefix) throws ParseException {
        List<String> values = arguments.getAllValues(prefix);
        if (values.stream().anyMatch(String::isEmpty)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        return values;
    }
}
