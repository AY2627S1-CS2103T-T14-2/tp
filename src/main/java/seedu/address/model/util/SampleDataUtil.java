package seedu.address.model.util;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
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
            new Person(new Name("Alex Yeoh"), new Phone("87438807"), new Email("alexyeoh@example.com"),
                new Address("Blk 30 Geylang Street 29, #06-40"),
                Stage.GIVING, getTagSet("friends")),
            new Person(new Name("Bernice Yu"), Optional.of(new Phone("99272758")),
                Optional.of(new Email("berniceyu@example.com")),
                Optional.of(new Address("Blk 30 Lorong 3 Serangoon Gardens, #07-18")),
                Optional.of(new Organisation("DBS Bank")), Stage.CULTIVATING, getTagSet("colleagues", "friends")),
            // an online donor who gave only an email
            new Person(new Name("Charlotte Oliveiro"), Optional.empty(),
                Optional.of(new Email("charlotte@example.com")), Optional.empty(), Optional.empty(),
                Stage.PROSPECT, getTagSet("neighbours")),
            // a corporate contact whose postal address is not known
            new Person(new Name("David Li"), Optional.of(new Phone("91031282")),
                Optional.of(new Email("lidavid@example.com")), Optional.empty(),
                Optional.of(new Organisation("Keppel Corporation")), Stage.CONTACTED, getTagSet("family")),
            new Person(new Name("Irfan Ibrahim"), new Phone("92492021"), new Email("irfan@example.com"),
                new Address("Blk 47 Tampines Street 20, #17-35"),
                Stage.LAPSED, getTagSet("classmates")),
            new Person(new Name("Roy Balakrishnan"), Optional.of(new Phone("92624417")),
                Optional.of(new Email("royb@example.com")),
                Optional.of(new Address("Blk 45 Aljunied Street 85, #11-31")),
                Optional.of(new Organisation("Procter & Gamble")), Stage.DECLINED, getTagSet("colleagues"))
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
