package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORGANISATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new EditCommand object
 */
public class EditCommandParser implements Parser<EditCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the EditCommand
     * and returns an EditCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                        PREFIX_ORGANISATION, PREFIX_TAG);

        Index index = ParserUtil.parseIndex(argMultimap.getPreamble(), EditCommand.MESSAGE_USAGE);

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                PREFIX_ORGANISATION);

        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            editPersonDescriptor.setName(ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get()));
        }
        parseClearableField(argMultimap, PREFIX_PHONE, ParserUtil::parsePhone, editPersonDescriptor::setPhone,
                editPersonDescriptor::clearPhone);
        parseClearableField(argMultimap, PREFIX_EMAIL, ParserUtil::parseEmail, editPersonDescriptor::setEmail,
                editPersonDescriptor::clearEmail);
        parseClearableField(argMultimap, PREFIX_ADDRESS, ParserUtil::parseAddress,
                editPersonDescriptor::setAddress, editPersonDescriptor::clearAddress);
        parseClearableField(argMultimap, PREFIX_ORGANISATION, ParserUtil::parseOrganisation,
                editPersonDescriptor::setOrganisation, editPersonDescriptor::clearOrganisation);
        parseTagsForEdit(argMultimap.getAllValues(PREFIX_TAG)).ifPresent(editPersonDescriptor::setTags);

        if (!editPersonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return new EditCommand(index, editPersonDescriptor);
    }

    /**
     * Parses the field given with {@code prefix} using {@code fieldParser} and passes it to {@code setter}.
     * A field given with nothing but spaces after its prefix, e.g. {@code a/}, is cleared with
     * {@code clearer} instead, and a field that is not given is left unchanged.
     *
     * @throws ParseException if the given value is invalid.
     */
    private static <T> void parseClearableField(ArgumentMultimap argMultimap, Prefix prefix,
            FieldParser<T> fieldParser, Consumer<T> setter, Runnable clearer) throws ParseException {
        Optional<String> value = argMultimap.getValue(prefix);
        if (value.isEmpty()) {
            return;
        }
        if (value.get().isBlank()) {
            clearer.run();
            return;
        }
        setter.accept(fieldParser.parse(value.get()));
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>} if {@code tags} is non-empty.
     * If {@code tags} contains only one element which is an empty string, it will be parsed into a
     * {@code Set<Tag>} containing zero tags.
     */
    private Optional<Set<Tag>> parseTagsForEdit(Collection<String> tags) throws ParseException {
        assert tags != null;

        if (tags.isEmpty()) {
            return Optional.empty();
        }
        Collection<String> tagSet = tags.size() == 1 && tags.contains("") ? Collections.emptySet() : tags;
        return Optional.of(ParserUtil.parseTags(tagSet));
    }

}
