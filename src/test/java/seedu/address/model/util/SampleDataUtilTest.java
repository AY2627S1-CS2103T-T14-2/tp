package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.QuietCommand;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.person.Stage;

public class SampleDataUtilTest {

    private final List<Person> samplePersons = Arrays.asList(SampleDataUtil.getSamplePersons());

    @Test
    public void getSamplePersons_optionalFields_someSupportersWithoutThem() {
        assertTrue(samplePersons.stream().anyMatch(person -> person.getPhone().isEmpty()));
        assertTrue(samplePersons.stream().anyMatch(person -> person.getAddress().isEmpty()));
        assertTrue(samplePersons.stream().anyMatch(person -> person.getOrganisation().isEmpty()));
    }

    @Test
    public void getSamplePersons_interactions_someSupportersHaveMultipleInteractions() {
        long supportersWithInteractions = samplePersons.stream()
                .filter(person -> !person.getInteractions().isEmpty())
                .count();

        assertTrue(supportersWithInteractions >= 2);
        assertTrue(supportersWithInteractions < samplePersons.size());
        assertTrue(samplePersons.stream()
                .filter(person -> !person.getInteractions().isEmpty())
                .allMatch(person -> person.getInteractionCount() >= 2));
    }

    @Test
    public void getSamplePersons_introducers_nameOtherSampleSupporters() {
        assertTrue(samplePersons.stream().anyMatch(person -> person.getIntroducer().isPresent()));
        samplePersons.stream()
                .flatMap(person -> person.getIntroducer().stream())
                .forEach(introducer -> assertTrue(samplePersons.stream()
                        .anyMatch(other -> other.getName().isSameName(introducer))));
    }

    @Test
    public void getSamplePersons_lastContact_someActiveSupporterIsQuiet() {
        LocalDate quietCutoff = LocalDate.now().minusDays(QuietCommand.DEFAULT_DAYS);

        assertTrue(samplePersons.stream()
                .filter(person -> person.getStage() != Stage.DECLINED)
                .anyMatch(person -> person.getLastInteractionDate()
                        .filter(date -> date.isBefore(quietCutoff)).isPresent()));
    }

    @Test
    public void getSamplePersons_names_includeAcceptedPunctuation() {
        assertTrue(samplePersons.stream().anyMatch(person -> person.getName().fullName.contains("'")));
        assertTrue(samplePersons.stream().anyMatch(person -> person.getName().fullName.contains(" s/o ")));
    }

    @Test
    public void getSamplePersons_phones_includeCountryCode() {
        assertTrue(samplePersons.stream()
                .anyMatch(person -> person.getPhone().filter(phone -> phone.value.startsWith("+")).isPresent()));
    }

    @Test
    public void getSampleAddressBook_noDuplicates_containsAllSamplePersons() {
        ReadOnlyAddressBook sampleAddressBook = SampleDataUtil.getSampleAddressBook();
        assertEquals(samplePersons, sampleAddressBook.getPersonList());
    }
}
