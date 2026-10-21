package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Name;
import seedu.address.model.person.Organisation;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Stage;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";

    public static final String MESSAGE_INVALID_INTERACTION_DATE =
            "Interaction dates must be valid dates in yyyy-MM-dd format, e.g. 2026-10-01.";
    public static final LocalDate MIN_INTERACTION_DATE = LocalDate.of(1900, 1, 1);
    public static final String MESSAGE_EARLY_INTERACTION_DATE =
            "Interaction dates must be on or after " + MIN_INTERACTION_DATE + ".";
    public static final String MESSAGE_FUTURE_INTERACTION_DATE = "Interaction dates cannot be in the future.";

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String organisation} into an {@code Organisation}.
     * Leading and trailing whitespaces will be trimmed, and runs of whitespace inside reduced to one.
     *
     * @throws ParseException if the given {@code organisation} is invalid.
     */
    public static Organisation parseOrganisation(String organisation) throws ParseException {
        requireNonNull(organisation);
        if (!Organisation.isValidOrganisation(organisation)) {
            throw new ParseException(Organisation.MESSAGE_CONSTRAINTS);
        }
        return new Organisation(organisation);
    }

    /**
     * Parses a {@code String stage} into a {@code Stage}.
     * Leading and trailing whitespaces will be trimmed and case is ignored.
     *
     * @throws ParseException if the given {@code stage} is invalid.
     */
    public static Stage parseStage(String stage) throws ParseException {
        requireNonNull(stage);
        String trimmedStage = stage.trim();
        if (!Stage.isValidStage(trimmedStage)) {
            throw new ParseException(Stage.MESSAGE_CONSTRAINTS);
        }
        return Stage.fromString(trimmedStage);
    }

    /**
     * Parses a {@code String note} into a valid interaction note.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code note} is invalid.
     */
    public static String parseInteractionNote(String note) throws ParseException {
        requireNonNull(note);
        String trimmedNote = note.trim();
        if (!Interaction.isValidNote(trimmedNote)) {
            throw new ParseException(Interaction.MESSAGE_CONSTRAINTS);
        }
        return trimmedNote;
    }

    /**
     * Parses an interaction date in yyyy-MM-dd format, trimming leading and trailing whitespace.
     * Uses the computer's local date to reject future dates.
     *
     * @throws ParseException if the date is invalid or in the future.
     */
    public static LocalDate parseInteractionDate(String date) throws ParseException {
        return parseInteractionDate(date, Clock.systemDefaultZone());
    }

    /**
     * Parses an interaction date using {@code clock} to determine today's date.
     *
     * @throws ParseException if the date is invalid or in the future.
     */
    static LocalDate parseInteractionDate(String date, Clock clock) throws ParseException {
        requireNonNull(date);
        requireNonNull(clock);
        String trimmedDate = date.trim();
        if (!trimmedDate.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            throw new ParseException(MESSAGE_INVALID_INTERACTION_DATE);
        }

        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(trimmedDate);
        } catch (DateTimeParseException e) {
            throw new ParseException(MESSAGE_INVALID_INTERACTION_DATE, e);
        }
        if (parsedDate.isBefore(MIN_INTERACTION_DATE)) {
            throw new ParseException(MESSAGE_EARLY_INTERACTION_DATE);
        }
        if (parsedDate.isAfter(LocalDate.now(clock))) {
            throw new ParseException(MESSAGE_FUTURE_INTERACTION_DATE);
        }
        return parsedDate;
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }
}
