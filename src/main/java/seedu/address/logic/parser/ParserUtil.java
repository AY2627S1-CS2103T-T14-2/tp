package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
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

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer (1, 2, 3, ...).";
    public static final String MESSAGE_INVALID_INTERACTION_INDICES =
            "Provide exactly two positive integer indices: INDEX ENTRY.";

    public static final String MESSAGE_INVALID_INTERACTION_DATE =
            "Interaction dates must be valid dates in yyyy-MM-dd format, e.g. 2026-10-01.";
    public static final LocalDate MIN_INTERACTION_DATE = LocalDate.of(1900, 1, 1);
    public static final String MESSAGE_EARLY_INTERACTION_DATE =
            "Interaction dates must be on or after " + MIN_INTERACTION_DATE + ".";
    public static final String MESSAGE_FUTURE_INTERACTION_DATE = "Interaction dates cannot be in the future.";
    public static final String MESSAGE_BLANK_TAG = "Tag should not be blank. "
            + "Provide a tag name after " + PREFIX_TAG + ".";

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
     * Parses a single index, reporting invalid command format for empty or multi-word input.
     * For a single invalid value, appends the command's usage to the index error.
     * Uses the same validation as {@link #parseIndex(String)}; displayed-list bounds are checked by the command.
     *
     * @throws ParseException if the specified index is invalid.
     */
    public static Index parseIndex(String oneBasedIndex, String commandUsage) throws ParseException {
        requireAllNonNull(oneBasedIndex, commandUsage);
        String trimmedIndex = oneBasedIndex.trim();
        if (!trimmedIndex.matches("\\S+")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, commandUsage));
        }
        try {
            return parseIndex(oneBasedIndex);
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + commandUsage, e);
        }
    }

    /**
     * Parses {@code INDEX ENTRY} into a supporter index and an interaction index.
     * Accepts leading, trailing and separating whitespace. Only parses the indices; commands must check
     * their bounds and map the displayed interaction number to its stored position.
     * Command parsers should wrap a failure with their own invalid-format usage message.
     *
     * @throws ParseException if there are not exactly two valid positive integer indices.
     */
    public static InteractionIndices parseInteractionIndices(String indices) throws ParseException {
        requireNonNull(indices);
        String[] parts = indices.trim().split("\\s+");
        if (parts.length != 2) {
            throw new ParseException(MESSAGE_INVALID_INTERACTION_INDICES);
        }
        try {
            return new InteractionIndices(parseIndex(parts[0]), parseIndex(parts[1]));
        } catch (ParseException e) {
            throw new ParseException(MESSAGE_INVALID_INTERACTION_INDICES, e);
        }
    }

    /**
     * Parses interaction indices, appending the command's usage to the index error if parsing fails.
     * Uses the same validation as {@link #parseInteractionIndices(String)}; bounds are checked by the command.
     *
     * @throws ParseException if there are not exactly two valid positive integer indices.
     */
    public static InteractionIndices parseInteractionIndices(String indices, String commandUsage)
            throws ParseException {
        requireAllNonNull(indices, commandUsage);
        try {
            return parseInteractionIndices(indices);
        } catch (ParseException e) {
            throw new ParseException(e.getMessage() + "\n" + commandUsage, e);
        }
    }

    /**
     * The supporter and interaction indices supplied to an interaction-editing or deletion command.
     * The interaction index identifies the displayed entry number, not its stored list position.
     *
     * @param supporterIndex index in the displayed supporter list.
     * @param interactionIndex chronological entry number shown beside the interaction, with oldest numbered 1.
     */
    public record InteractionIndices(Index supporterIndex, Index interactionIndex) {
        /**
         * Creates a pair of non-null supporter and interaction indices.
         */
        public InteractionIndices {
            requireAllNonNull(supporterIndex, interactionIndex);
        }
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
        String trimmedAddress = StringUtil.trimUnicodeWhitespace(address);
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
        String trimmedNote = StringUtil.trimUnicodeWhitespace(note);
        if (!Interaction.isValidNote(trimmedNote)) {
            throw new ParseException(Interaction.MESSAGE_CONSTRAINTS);
        }
        return trimmedNote;
    }

    /**
     * Parses a supplied interaction date, or returns an empty {@code Optional} if absent.
     * A present value, including a blank string, is validated by {@link #parseInteractionDate(String)}.
     *
     * @throws ParseException if the supplied date is invalid, before the minimum date, or in the future.
     */
    public static Optional<LocalDate> parseOptionalInteractionDate(Optional<String> date) throws ParseException {
        requireNonNull(date);
        if (date.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(parseInteractionDate(date.get()));
    }

    /**
     * Parses an interaction date in yyyy-MM-dd format, trimming leading and trailing whitespace.
     * Accepts dates from {@link #MIN_INTERACTION_DATE} (1900-01-01) through today, inclusive.
     * Uses the computer's local date to determine today.
     *
     * @throws ParseException if the date is invalid, before {@link #MIN_INTERACTION_DATE}, or in the future.
     */
    public static LocalDate parseInteractionDate(String date) throws ParseException {
        return parseInteractionDate(date, Clock.systemDefaultZone());
    }

    /**
     * Parses an interaction date using {@code clock} to determine today's date.
     * Accepts dates from {@link #MIN_INTERACTION_DATE} (1900-01-01) through today, inclusive.
     *
     * @throws ParseException if the date is invalid, before {@link #MIN_INTERACTION_DATE}, or in the future.
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
        if (StringUtil.isUnicodeBlank(tag)) {
            throw new ParseException(MESSAGE_BLANK_TAG);
        }
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>} in the order they were typed.
     * Tags differing only in case count as one, keeping the spelling typed first.
     *
     * @throws ParseException if any of the given tags is invalid.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new LinkedHashSet<>();
        for (String tagName : tags) {
            Tag tag = parseTag(tagName);
            // tags differing only in case count as one, so the spelling typed first is kept
            if (tagSet.stream().noneMatch(tag::isSameTag)) {
                tagSet.add(tag);
            }
        }
        return tagSet;
    }
}
