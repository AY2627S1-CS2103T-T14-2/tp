package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;

/**
 * Prepares an immutable snapshot of the supporter details and history displayed in the detail panel.
 * Formatting and sorting do not change the supporter's stored record.
 */
public final class SupporterDetailData {

    public static final String MESSAGE_STARTUP = "Type view INDEX to see a supporter's details and history.";
    public static final String MESSAGE_EMPTY_HISTORY = "No interactions yet. Log one with: log INDEX nt/NOTE.";

    private static final String UNSET_FIELD = "-";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.ENGLISH);

    private final Optional<Person> viewedPerson;
    private final List<InteractionEntry> interactionEntries;

    /**
     * Creates display data for {@code viewedPerson}, or the startup state when no supporter is selected.
     */
    public SupporterDetailData(Optional<Person> viewedPerson) {
        this.viewedPerson = requireNonNull(viewedPerson);
        interactionEntries = viewedPerson.map(SupporterDetailData::createInteractionEntries).orElseGet(List::of);
    }

    /**
     * Returns whether a supporter is selected, independently of the displayed list and its filter.
     */
    public boolean hasSupporter() {
        return viewedPerson.isPresent();
    }

    public String getName() {
        return viewedPerson.map(person -> person.getName().fullName).orElse(UNSET_FIELD);
    }

    public String getStage() {
        return viewedPerson.map(person -> person.getStage().toString()).orElse(UNSET_FIELD);
    }

    public String getOrganisation() {
        return viewedPerson.flatMap(Person::getOrganisation)
                .map(organisation -> organisation.value).orElse(UNSET_FIELD);
    }

    public String getPhone() {
        return viewedPerson.flatMap(Person::getPhone).map(phone -> phone.value).orElse(UNSET_FIELD);
    }

    public String getEmail() {
        return viewedPerson.flatMap(Person::getEmail).map(email -> email.value).orElse(UNSET_FIELD);
    }

    public String getAddress() {
        return viewedPerson.flatMap(Person::getAddress).map(address -> address.value).orElse(UNSET_FIELD);
    }

    /**
     * Returns alphabetically ordered tag names separated by commas, or {@code -} when none are set.
     */
    public String getTags() {
        String tags = viewedPerson.map(person -> person.getTags().stream()
                .map(tag -> tag.tagName)
                .sorted()
                .collect(Collectors.joining(", "))).orElse("");
        return tags.isEmpty() ? UNSET_FIELD : tags;
    }

    /**
     * Returns the shared last-contact text, or {@code -} when no supporter is selected.
     */
    public String getLastContact() {
        return viewedPerson.map(person -> LastContactFormatter.format(person.getLastInteractionDate()))
                .orElse(UNSET_FIELD);
    }

    public String getHistoryHeading() {
        return hasSupporter() ? "Interactions (" + interactionEntries.size() + ") - newest first" : "";
    }

    /**
     * Returns the startup prompt when no supporter is selected, the empty-history message when no interactions
     * exist, or an empty string when interaction entries are available.
     */
    public String getPlaceholderMessage() {
        if (!hasSupporter()) {
            return MESSAGE_STARTUP;
        }
        return interactionEntries.isEmpty() ? MESSAGE_EMPTY_HISTORY : "";
    }

    /**
     * Returns immutable entries ordered by date descending, then by original entry number descending.
     */
    public List<InteractionEntry> getInteractionEntries() {
        return interactionEntries;
    }

    private static List<InteractionEntry> createInteractionEntries(Person person) {
        List<Interaction> interactions = person.getInteractions();
        List<InteractionEntry> entries = new ArrayList<>();
        for (int i = 0; i < interactions.size(); i++) {
            // Assign numbers from logging order before sorting the display entries.
            entries.add(new InteractionEntry(i + 1, interactions.get(i)));
        }
        entries.sort(Comparator.comparing((InteractionEntry entry) -> entry.date)
                .thenComparingInt(InteractionEntry::getNumber)
                .reversed());
        return List.copyOf(entries);
    }

    /**
     * An immutable display entry that retains an interaction's original number, date and complete note.
     */
    public static final class InteractionEntry {

        private final int number;
        private final LocalDate date;
        private final String note;

        private InteractionEntry(int number, Interaction interaction) {
            this.number = number;
            date = interaction.getDate();
            note = interaction.getNote();
        }

        /**
         * Returns the interaction's one-based position in the stored logging order.
         */
        public int getNumber() {
            return number;
        }

        /**
         * Returns the date formatted as {@code dd MMM uuuu} using English month abbreviations.
         */
        public String getDate() {
            return date.format(DATE_FORMATTER);
        }

        public String getNote() {
            return note;
        }
    }
}
