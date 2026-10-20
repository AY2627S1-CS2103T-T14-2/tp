package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Name;
import seedu.address.model.person.Organisation;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Stage;
import seedu.address.model.tag.Tag;

public class ParserUtilTest {
    private static final Clock INTERACTION_CLOCK = Clock.fixed(
            Instant.parse("2026-10-19T16:30:00Z"), ZoneId.of("Asia/Singapore"));

    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "123456";
    private static final String VALID_ADDRESS = "123 Main Street #0505";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_INTERACTION_NOTE = "Called to discuss the year-end appeal";
    private static final String VALID_TAG_1 = "friend";
    private static final String VALID_TAG_2 = "neighbour";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseAddress_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAddress((String) null));
    }

    @Test
    public void parseAddress_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress(INVALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithoutWhitespace_returnsAddress() throws Exception {
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(VALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithWhitespace_returnsTrimmedAddress() throws Exception {
        String addressWithWhitespace = WHITESPACE + VALID_ADDRESS + WHITESPACE;
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(addressWithWhitespace));
    }

    @Test
    public void parseOrganisation_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseOrganisation(null));
    }

    @Test
    public void parseOrganisation_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Organisation.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseOrganisation(""));
        assertThrows(ParseException.class, Organisation.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseOrganisation(
                "a".repeat(Organisation.MAX_LENGTH + 1)));
    }

    @Test
    public void parseOrganisation_validValueWithWhitespace_returnsNormalisedOrganisation() throws Exception {
        Organisation expectedOrganisation = new Organisation("Procter & Gamble");
        String organisationWithWhitespace = WHITESPACE + "Procter  &  Gamble" + WHITESPACE;
        assertEquals(expectedOrganisation, ParserUtil.parseOrganisation(organisationWithWhitespace));
    }

    @Test
    public void parseStage_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseStage(null));
    }

    @Test
    public void parseStage_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Stage.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseStage("givng"));
        assertThrows(ParseException.class, Stage.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseStage(""));
        assertThrows(ParseException.class, Stage.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseStage("cult"));
    }

    @Test
    public void parseStage_validValueWithoutWhitespace_returnsStage() throws Exception {
        assertEquals(Stage.CULTIVATING, ParserUtil.parseStage("cultivating"));
        assertEquals(Stage.CULTIVATING, ParserUtil.parseStage("CULTIVATING"));
    }

    @Test
    public void parseStage_validValueWithWhitespace_returnsTrimmedStage() throws Exception {
        assertEquals(Stage.DECLINED, ParserUtil.parseStage(WHITESPACE + "Declined" + WHITESPACE));
    }

    @Test
    public void parseInteractionNote_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseInteractionNote(null));
    }

    @Test
    public void parseInteractionNote_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Interaction.MESSAGE_CONSTRAINTS, () ->
                ParserUtil.parseInteractionNote(" "));
        assertThrows(ParseException.class, Interaction.MESSAGE_CONSTRAINTS, () ->
                ParserUtil.parseInteractionNote("a".repeat(Interaction.MAX_NOTE_LENGTH + 1)));
    }

    @Test
    public void parseInteractionNote_validValueWithoutWhitespace_returnsNote() throws Exception {
        assertEquals(VALID_INTERACTION_NOTE, ParserUtil.parseInteractionNote(VALID_INTERACTION_NOTE));
    }

    @Test
    public void parseInteractionNote_validValueWithWhitespace_returnsTrimmedNote() throws Exception {
        String noteWithWhitespace = WHITESPACE + VALID_INTERACTION_NOTE + WHITESPACE;
        assertEquals(VALID_INTERACTION_NOTE, ParserUtil.parseInteractionNote(noteWithWhitespace));
    }

    @Test
    public void parseInteractionDate_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseInteractionDate(null));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseInteractionDate("2026-10-20", null));
    }

    @Test
    public void parseInteractionDate_invalidFormat_throwsParseException() {
        for (String date : List.of("", WHITESPACE, "2026-1-01", "2026-01-1", "26-01-01", "2026/01/01",
                "01-01-2026", "+2026-01-01", "10000-01-01", "2026-01-01T12:00:00", "2026- 01-01")) {
            assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_INTERACTION_DATE, () ->
                    ParserUtil.parseInteractionDate(date, INTERACTION_CLOCK));
        }
    }

    @Test
    public void parseInteractionDate_invalidCalendarDate_throwsParseException() {
        for (String date : List.of("2026-02-29", "2024-02-30", "2026-04-31", "2026-00-10",
                "2026-13-10", "2026-01-00", "2026-01-32")) {
            assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_INTERACTION_DATE, () ->
                    ParserUtil.parseInteractionDate(date, INTERACTION_CLOCK));
        }
    }

    @Test
    public void parseInteractionDate_pastDate_returnsDate() throws Exception {
        assertEquals(LocalDate.of(2026, 10, 19),
                ParserUtil.parseInteractionDate("2026-10-19", INTERACTION_CLOCK));
    }

    @Test
    public void parseInteractionDate_leapDay_returnsDate() throws Exception {
        assertEquals(LocalDate.of(2024, 2, 29),
                ParserUtil.parseInteractionDate("2024-02-29", INTERACTION_CLOCK));
    }

    @Test
    public void parseInteractionDate_surroundingWhitespace_returnsDate() throws Exception {
        assertEquals(LocalDate.of(2026, 10, 19),
                ParserUtil.parseInteractionDate(WHITESPACE + "2026-10-19" + WHITESPACE, INTERACTION_CLOCK));
    }

    @Test
    public void parseInteractionDate_todayInClockZone_returnsDate() throws Exception {
        // It is already 20 October in Singapore, although the UTC date is still 19 October.
        assertEquals(LocalDate.of(2026, 10, 20),
                ParserUtil.parseInteractionDate("2026-10-20", INTERACTION_CLOCK));
    }

    @Test
    public void parseInteractionDate_futureDate_throwsParseException() {
        assertThrows(ParseException.class, ParserUtil.MESSAGE_FUTURE_INTERACTION_DATE, () ->
                ParserUtil.parseInteractionDate("2026-10-21", INTERACTION_CLOCK));
    }

    @Test
    public void parseInteractionDate_withoutClock_returnsPastDate() throws Exception {
        assertEquals(LocalDate.of(2000, 1, 1), ParserUtil.parseInteractionDate("2000-01-01"));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseTag_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTag(null));
    }

    @Test
    public void parseTag_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTag(INVALID_TAG));
    }

    @Test
    public void parseTag_validValueWithoutWhitespace_returnsTag() throws Exception {
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(VALID_TAG_1));
    }

    @Test
    public void parseTag_validValueWithWhitespace_returnsTrimmedTag() throws Exception {
        String tagWithWhitespace = WHITESPACE + VALID_TAG_1 + WHITESPACE;
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(tagWithWhitespace));
    }

    @Test
    public void parseTags_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTags(null));
    }

    @Test
    public void parseTags_collectionWithInvalidTags_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTags(List.of(VALID_TAG_1, INVALID_TAG)));
    }

    @Test
    public void parseTags_emptyCollection_returnsEmptySet() throws Exception {
        assertTrue(ParserUtil.parseTags(List.of()).isEmpty());
    }

    @Test
    public void parseTags_collectionWithValidTags_returnsTagSet() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of(VALID_TAG_1, VALID_TAG_2));
        Set<Tag> expectedTagSet = Set.of(new Tag(VALID_TAG_1), new Tag(VALID_TAG_2));

        assertEquals(expectedTagSet, actualTagSet);
    }
}
