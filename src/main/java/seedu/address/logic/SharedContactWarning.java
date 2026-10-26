package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import seedu.address.model.person.Person;

/**
 * Describes existing supporters whose contact details match a supporter being added.
 * Matches are advisory and do not change the name-based rule for duplicate supporters.
 */
public class SharedContactWarning {
    public static final String MESSAGE_SHARED_CONTACTS =
            "Warning: Shared contact details with existing supporter(s): %s. "
                    + "The supporter was added; check whether this is intentional.";

    private final List<Person> existingPersons;
    private final Optional<String> phoneKey;
    private final Optional<String> emailKey;

    /**
     * Creates a warning using a snapshot of supporters before {@code person} is added.
     */
    public SharedContactWarning(List<Person> existingPersons, Person person) {
        this.existingPersons = List.copyOf(requireNonNull(existingPersons));
        requireNonNull(person);
        phoneKey = person.getPhone().map(phone -> normalizePhone(phone.value));
        emailKey = person.getEmail().map(email -> email.value.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns a warning naming each matching supporter and field, or an empty string when there are no matches.
     * Results retain full-list order and each supporter appears at most once.
     */
    public String getMessage() {
        List<String> matches = new ArrayList<>();
        for (Person existing : existingPersons) {
            boolean hasSharedPhone = matchesContact(phoneKey,
                    existing.getPhone().map(phone -> normalizePhone(phone.value)));
            boolean hasSharedEmail = matchesContact(emailKey,
                    existing.getEmail().map(email -> email.value.toLowerCase(Locale.ROOT)));
            if (hasSharedPhone || hasSharedEmail) {
                matches.add(existing.getName().fullName + " (" + sharedFields(hasSharedPhone, hasSharedEmail) + ")");
            }
        }
        return matches.isEmpty() ? "" : String.format(MESSAGE_SHARED_CONTACTS, String.join("; ", matches));
    }

    private static boolean matchesContact(Optional<String> first, Optional<String> second) {
        return first.isPresent() && first.equals(second);
    }

    private static String sharedFields(boolean hasSharedPhone, boolean hasSharedEmail) {
        if (hasSharedPhone && hasSharedEmail) {
            return "phone and email";
        }
        return hasSharedPhone ? "phone" : "email";
    }

    /**
     * Returns a full-number comparison key, keeping extension boundaries and digits significant.
     * Labels and formatting are ignored; country codes are not inferred or removed.
     * This deliberately differs from the substring normalization used by phone searches.
     */
    static String normalizePhone(String phone) {
        requireNonNull(phone);
        return phone.replaceAll(" \\([A-Za-z]+(?: [A-Za-z]+)*\\)$", "")
                .replaceAll("(?i) ext ", "#")
                .replaceAll("[+ ()-]", "");
    }
}
