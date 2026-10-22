package seedu.address.model.util;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Name;
import seedu.address.model.person.Organisation;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Stage;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Alex Yeoh"), Optional.of(new Phone("87438807")),
                Optional.of(new Email("alexyeoh@example.com")),
                Optional.of(new Address("Blk 30 Geylang Street 29, #06-40")), Optional.empty(), Optional.empty(),
                Stage.GIVING,
                List.of(
                    new Interaction(LocalDate.of(2026, 7, 18), "Thanked him for his recurring donation"),
                    new Interaction(LocalDate.of(2026, 9, 12),
                        "Confirmed attendance at the donor appreciation dinner")),
                getTagSet("monthlyDonor")),
            new Person(new Name("Bernice Yu"), Optional.of(new Phone("99272758")),
                Optional.of(new Email("berniceyu@example.com")),
                Optional.of(new Address("Blk 30 Lorong 3 Serangoon Gardens, #07-18")),
                Optional.of(new Organisation("DBS Bank")), Optional.empty(), Stage.CULTIVATING,
                List.of(
                    new Interaction(LocalDate.of(2026, 8, 5), "Discussed DBS employee giving programme"),
                    new Interaction(LocalDate.of(2026, 9, 16),
                        "Coffee at Raffles Place, keen on a Q4 volunteering day"),
                    new Interaction(LocalDate.of(2026, 10, 3),
                        "Sent volunteering day details and next steps")),
                getTagSet("csr", "boardIntro")),
            // an online donor who gave only an email
            new Person(new Name("Charlotte O'Brien"), Optional.empty(),
                Optional.of(new Email("charlotte@example.com")), Optional.empty(), Optional.empty(),
                Stage.PROSPECT, getTagSet("onlineDonor")),
            // a corporate contact whose postal address is not known, introduced by another supporter
            new Person(new Name("David Li"), Optional.of(new Phone("+65 9103 1282")),
                Optional.of(new Email("lidavid@example.com")), Optional.empty(),
                Optional.of(new Organisation("Keppel Corporation")), Optional.of(new Name("Bernice Yu")),
                Stage.CONTACTED,
                List.of(
                    new Interaction(LocalDate.of(2026, 8, 21), "Sent the community outreach sponsorship deck"),
                    new Interaction(LocalDate.of(2026, 9, 25),
                        "Follow-up call; checking internally on event sponsorship")),
                getTagSet("csr")),
            // a past donor who has gone quiet since the last gala
            new Person(new Name("Irfan Ibrahim"), Optional.of(new Phone("92492021")),
                Optional.of(new Email("irfan@example.com")),
                Optional.of(new Address("Blk 47 Tampines Street 20, #17-35")), Optional.empty(), Optional.empty(),
                Stage.LAPSED,
                List.of(
                    new Interaction(LocalDate.of(2025, 11, 29), "Bought a table at the charity gala"),
                    new Interaction(LocalDate.of(2026, 3, 10),
                        "Asked about renewing his gala table; no reply yet")),
                getTagSet("gala2025")),
            // a corporate contact who declined, so quiet leaves them out
            new Person(new Name("Rajesh s/o Balakrishnan"), Optional.of(new Phone("92624417")),
                Optional.of(new Email("rajeshb@example.com")),
                Optional.of(new Address("Blk 45 Aljunied Street 85, #11-31")),
                Optional.of(new Organisation("Procter & Gamble")), Stage.DECLINED, getTagSet("csr"))
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

}
