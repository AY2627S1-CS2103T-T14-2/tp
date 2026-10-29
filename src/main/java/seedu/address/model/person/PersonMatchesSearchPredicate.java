package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.text.Normalizer;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Stream;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Matches any criterion within a field and all specified fields of a supporter.
 * Text matching uses NFC, ignores case and apostrophe style, and matches substrings.
 * Stages match exactly. Stored text is unchanged.
 * Phone matching ignores formatting, type labels and extension markers, but retains extension digits.
 */
public class PersonMatchesSearchPredicate implements Predicate<Person> {
    private static final Logger logger = LogsCenter.getLogger(PersonMatchesSearchPredicate.class);

    private final List<String> nameKeywords;
    private final List<String> organisationKeywords;
    private final List<String> tagKeywords;
    private final List<Stage> stages;
    private final List<String> phoneKeywords;
    private final List<String> emailKeywords;

    /**
     * Creates a predicate that searches only names using the given keywords.
     */
    public PersonMatchesSearchPredicate(List<String> nameKeywords) {
        this(nameKeywords, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    /**
     * Creates a predicate with the given alternatives for each field.
     * Empty lists omit that field; if all lists are empty, no supporter matches.
     * Search text must be nonempty, and phone criteria must contain digits after normalization.
     * The supplied lists are defensively copied.
     */
    public PersonMatchesSearchPredicate(List<String> nameKeywords, List<String> organisationKeywords,
            List<String> tagKeywords, List<Stage> stages, List<String> phoneKeywords, List<String> emailKeywords) {
        this.nameKeywords = normalizeText(nameKeywords);
        this.organisationKeywords = normalizeText(organisationKeywords);
        this.tagKeywords = normalizeText(tagKeywords);
        this.stages = List.copyOf(stages);
        this.phoneKeywords = phoneKeywords.stream().map(PersonMatchesSearchPredicate::normalizePhone).toList();
        this.emailKeywords = normalizeText(emailKeywords);
        assert Stream.of(this.nameKeywords, this.organisationKeywords, this.tagKeywords, this.emailKeywords)
                .flatMap(List::stream).noneMatch(String::isEmpty) : "Validated text criteria must be nonempty";
        assert this.phoneKeywords.stream().allMatch(keyword -> !keyword.isEmpty()
                && keyword.chars().allMatch(digit -> digit >= '0' && digit <= '9'))
                : "Validated phone criteria must normalize to nonempty ASCII digits";
        logger.fine(() -> "Created search predicate: names=" + this.nameKeywords.size()
                + ", organisations=" + this.organisationKeywords.size() + ", tags=" + this.tagKeywords.size()
                + ", stages=" + this.stages.size() + ", phones=" + this.phoneKeywords.size()
                + ", emails=" + this.emailKeywords.size());
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        boolean hasCriteria = !nameKeywords.isEmpty() || !organisationKeywords.isEmpty() || !tagKeywords.isEmpty()
                || !stages.isEmpty() || !phoneKeywords.isEmpty() || !emailKeywords.isEmpty();

        return hasCriteria
                && matchesText(person.getName().fullName, nameKeywords)
                && (organisationKeywords.isEmpty() || person.getOrganisation()
                        .map(organisation -> matchesText(organisation.value, organisationKeywords)).orElse(false))
                && (tagKeywords.isEmpty() || person.getTags().stream()
                        .anyMatch(tag -> matchesText(tag.tagName, tagKeywords)))
                && (stages.isEmpty() || stages.contains(person.getStage()))
                && (phoneKeywords.isEmpty() || person.getPhone()
                        .map(phone -> matchesText(normalizePhone(phone.value), phoneKeywords)).orElse(false))
                && (emailKeywords.isEmpty() || person.getEmail()
                        .map(email -> matchesText(email.value, emailKeywords)).orElse(false));
    }

    /**
     * Returns phone search digits without formatting, trailing labels or extension markers.
     * This does not validate the remaining characters or require a complete phone number.
     */
    public static String normalizePhone(String phone) {
        return Phone.normalizeForSearch(phone);
    }

    private static List<String> normalizeText(List<String> keywords) {
        return keywords.stream().map(PersonMatchesSearchPredicate::normalizeText).toList();
    }

    private static String normalizeText(String text) {
        // Fold code points independently: whole-string lowercasing changes final sigma and expands dotted I.
        return Normalizer.normalize(text, Normalizer.Form.NFC).replace('’', '\'').codePoints()
                .map(Character::toUpperCase).map(Character::toLowerCase)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    private static boolean matchesText(String value, List<String> keywords) {
        String normalizedValue = normalizeText(value);
        return keywords.isEmpty() || keywords.stream().anyMatch(normalizedValue::contains);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PersonMatchesSearchPredicate otherPredicate)) {
            return false;
        }
        return nameKeywords.equals(otherPredicate.nameKeywords)
                && organisationKeywords.equals(otherPredicate.organisationKeywords)
                && tagKeywords.equals(otherPredicate.tagKeywords)
                && stages.equals(otherPredicate.stages)
                && phoneKeywords.equals(otherPredicate.phoneKeywords)
                && emailKeywords.equals(otherPredicate.emailKeywords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameKeywords, organisationKeywords, tagKeywords, stages, phoneKeywords, emailKeywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("nameKeywords", nameKeywords)
                .add("organisationKeywords", organisationKeywords)
                .add("tagKeywords", tagKeywords)
                .add("stages", stages)
                .add("phoneKeywords", phoneKeywords)
                .add("emailKeywords", emailKeywords)
                .toString();
    }
}
