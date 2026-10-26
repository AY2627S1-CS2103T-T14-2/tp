package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class SharedContactWarningTest {
    private final Person alice = new PersonBuilder().withName("Alice").withPhone("+65 9123-4567")
            .withEmail("alice@example.com").build();

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SharedContactWarning(null, alice));
        assertThrows(NullPointerException.class, () -> new SharedContactWarning(List.of(), null));
    }

    @Test
    public void getMessage_noMatchingContacts_returnsEmpty() {
        Person different = new PersonBuilder().withName("Different").withPhone("91234567")
                .withEmail("other@example.com").build();

        assertEquals("", new SharedContactWarning(List.of(alice), different).getMessage());
        assertEquals("", new SharedContactWarning(List.of(), alice).getMessage());
    }

    @Test
    public void getMessage_matchingPhone_ignoresFormattingAndNamesExistingSupporter() {
        Person candidate = new PersonBuilder().withName("Bob").withPhone("6591234567").withoutEmail().build();

        assertEquals(warning("Alice (phone)"), new SharedContactWarning(List.of(alice), candidate).getMessage());
    }

    @Test
    public void getMessage_matchingEmail_ignoresCase() {
        Person candidate = new PersonBuilder().withName("Bob").withoutPhone().withEmail("ALICE@EXAMPLE.COM").build();

        assertEquals(warning("Alice (email)"), new SharedContactWarning(List.of(alice), candidate).getMessage());
    }

    @Test
    public void getMessage_matchingBothFields_namesSupporterOnce() {
        Person candidate = new PersonBuilder(alice).withName("Bob").build();

        assertEquals(warning("Alice (phone and email)"),
                new SharedContactWarning(List.of(alice), candidate).getMessage());
    }

    @Test
    public void getMessage_multipleMatches_retainsFullListOrderAndIdentifiesFields() {
        Person emailMatch = new PersonBuilder().withName("Email Match").withoutPhone()
                .withEmail("ALICE@example.com").build();
        Person phoneMatch = new PersonBuilder().withName("Phone Match").withPhone("6591234567").withoutEmail().build();

        assertEquals(warning("Email Match (email); Phone Match (phone); Alice (phone and email)"),
                new SharedContactWarning(List.of(emailMatch, phoneMatch, alice), alice).getMessage());
    }

    @Test
    public void getMessage_absentContactFields_doNotMatchEachOther() {
        Person phoneOnly = new PersonBuilder(alice).withoutEmail().build();
        Person emailOnly = new PersonBuilder(alice).withoutPhone().build();

        assertEquals("", new SharedContactWarning(List.of(phoneOnly), emailOnly).getMessage());
        assertEquals("", new SharedContactWarning(List.of(emailOnly), phoneOnly).getMessage());
    }

    @Test
    public void getMessage_emailAliasesAndPartialNumbers_doNotMatch() {
        Person partialPhone = new PersonBuilder().withName("Partial").withPhone("9123").withoutEmail().build();
        Person plusAlias = new PersonBuilder().withName("Plus Alias").withoutPhone()
                .withEmail("alice+donor@example.com").build();
        Person dotAlias = new PersonBuilder().withName("Dot Alias").withoutPhone()
                .withEmail("a.lice@example.com").build();

        for (Person candidate : List.of(partialPhone, plusAlias, dotAlias)) {
            assertEquals("", new SharedContactWarning(List.of(alice), candidate).getMessage());
        }
    }

    @Test
    public void constructor_mutableExistingList_takesSnapshot() {
        List<Person> existing = new ArrayList<>(List.of(alice));
        SharedContactWarning warning = new SharedContactWarning(existing, alice);
        existing.clear();

        assertEquals(warning("Alice (phone and email)"), warning.getMessage());
    }

    @Test
    public void normalizePhone_annotations_ignoresLabelsButKeepsExtensionBoundaries() {
        assertThrows(NullPointerException.class, () -> SharedContactWarning.normalizePhone(null));
        assertEquals("6591234567", SharedContactWarning.normalizePhone("+(65) 9123-4567 (HP)"));
        assertEquals("91234567#12", SharedContactWarning.normalizePhone("9123-4567 EXT 12 (Office Fax)"));
        assertEquals("91234567#012", SharedContactWarning.normalizePhone("91234567 ext 012"));
        assertEquals("9123456712", SharedContactWarning.normalizePhone("9123456712"));
    }

    private String warning(String matches) {
        return String.format(SharedContactWarning.MESSAGE_SHARED_CONTACTS, matches);
    }
}
