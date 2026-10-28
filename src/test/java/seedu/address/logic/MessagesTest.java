package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void format_allFieldsPresent_showsAllFields() {
        Person person = new PersonBuilder().withName("Tan Wei Ming").withPhone("91234567")
                .withEmail("weiming@dbs.com").withAddress("12 Marina Boulevard").withOrganisation("DBS Bank")
                .withStage("cultivating").withTags("boardIntro").build();
        assertEquals("Tan Wei Ming; Stage: Cultivating; Phone: 91234567; Email: weiming@dbs.com; "
                + "Address: 12 Marina Boulevard; Organisation: DBS Bank; Tags: [boardIntro]", Messages.format(person));
    }

    @Test
    public void format_optionalFieldsMissing_leavesThemOut() {
        Person emailOnly = new PersonBuilder().withName("Aisha Rahman").withoutPhone().withoutAddress()
                .withEmail("aisha.r@gmail.com").withTags().build();
        assertEquals("Aisha Rahman; Stage: Prospect; Email: aisha.r@gmail.com", Messages.format(emailOnly));
    }

    @Test
    public void format_severalTags_alphabeticalIgnoringCase() {
        Person person = new PersonBuilder().withName("Tan Wei Ming").withPhone("91234567").withoutAddress()
                .withTags("vip", "Donor", "boardIntro").build();
        assertEquals("Tan Wei Ming; Stage: Prospect; Phone: 91234567; Email: "
                + person.getEmail().get() + "; Tags: [boardIntro][Donor][vip]", Messages.format(person));
    }

    @Test
    public void formatTags_alphabeticalIgnoringCase() {
        List<Tag> tags = List.of(new Tag("vip"), new Tag("Donor"), new Tag("boardIntro"));
        assertEquals("[boardIntro] [Donor] [vip]", Messages.formatTags(tags));
        assertEquals("", Messages.formatTags(List.of()));
    }

    @Test
    public void formatUnrecognisedPrefixNote_oneOrMorePrefixes_namesThem() {
        assertEquals(String.format(Messages.MESSAGE_UNRECOGNISED_PREFIX, "em/"),
                Messages.formatUnrecognisedPrefixNote(List.of("em/")));
        assertEquals(String.format(Messages.MESSAGE_UNRECOGNISED_PREFIXES, "em/, dte/"),
                Messages.formatUnrecognisedPrefixNote(List.of("em/", "dte/")));
        assertThrows(AssertionError.class, () -> Messages.formatUnrecognisedPrefixNote(List.of()));
    }

    @Test
    public void formatUnrecognisedPrefixErrorNote_oneOrMorePrefixes_namesThem() {
        assertEquals(String.format(Messages.MESSAGE_UNRECOGNISED_PREFIX_IN_ERROR, "x/"),
                Messages.formatUnrecognisedPrefixErrorNote(List.of("x/")));
        assertEquals(String.format(Messages.MESSAGE_UNRECOGNISED_PREFIXES_IN_ERROR, "x/, ee/"),
                Messages.formatUnrecognisedPrefixErrorNote(List.of("x/", "ee/")));
        assertThrows(AssertionError.class, () -> Messages.formatUnrecognisedPrefixErrorNote(List.of()));
    }
}
