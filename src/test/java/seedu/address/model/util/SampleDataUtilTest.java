package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

public class SampleDataUtilTest {

    private final List<Person> samplePersons = Arrays.asList(SampleDataUtil.getSamplePersons());

    @Test
    public void getSamplePersons_optionalFields_someSupportersWithoutThem() {
        assertTrue(samplePersons.stream().anyMatch(person -> person.getPhone().isEmpty()));
        assertTrue(samplePersons.stream().anyMatch(person -> person.getAddress().isEmpty()));
        assertTrue(samplePersons.stream().anyMatch(person -> person.getOrganisation().isEmpty()));
    }

    @Test
    public void getSampleAddressBook_noDuplicates_containsAllSamplePersons() {
        ReadOnlyAddressBook sampleAddressBook = SampleDataUtil.getSampleAddressBook();
        assertEquals(samplePersons, sampleAddressBook.getPersonList());
    }
}
