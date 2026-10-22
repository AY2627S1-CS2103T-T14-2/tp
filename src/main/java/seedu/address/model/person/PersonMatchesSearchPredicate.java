package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Matches any criterion within a field and all specified fields of a supporter.
 * Text matching ignores case and uses substrings, while stages match exactly.
 * Phone matching ignores plus signs, spaces and hyphens.
 */
public class PersonMatchesSearchPredicate implements Predicate<Person> {
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
     * Returns phone text without plus signs, spaces or hyphens for substring searches.
     * This does not validate the remaining characters or require a complete phone number.
     */
    public static String normalizePhone(String phone) {
        requireNonNull(phone);
        return phone.replace("+", "").replace(" ", "").replace("-", "");
    }

    private static List<String> normalizeText(List<String> keywords) {
        return keywords.stream().map(keyword -> keyword.toLowerCase(Locale.ROOT)).toList();
    }

    private static boolean matchesText(String value, List<String> keywords) {
        String normalizedValue = value.toLowerCase(Locale.ROOT);
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
