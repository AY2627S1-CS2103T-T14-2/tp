package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
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
}
