package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.ui.SupporterDetailData.InteractionEntry;

/**
 * Tests supporter detail formatting and history ordering without starting JavaFX.
 */
public class SupporterDetailDataTest {

    @Test
    public void constructor_nullSelection_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SupporterDetailData(null));
    }

    @Test
    public void constructor_nullIntroductions_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SupporterDetailData(Optional.empty(), null));
    }

    @Test
    public void constructor_introducerAndIntroductions_showsBoth() {
        Person person = new PersonBuilder().withName("Lim Mei Hua").withIntroducer("Ong Kai Wen").build();
        Person firstIntroduced = new PersonBuilder().withName("Tan Wei Ming").build();
        Person secondIntroduced = new PersonBuilder().withName("David Li").build();
        SupporterDetailData data = new SupporterDetailData(Optional.of(person),
                List.of(firstIntroduced, secondIntroduced));

        assertEquals("Ong Kai Wen", data.getIntroducer());
        assertEquals("Tan Wei Ming, David Li", data.getIntroductions());
    }

    @Test
    public void constructor_noIntroducerOrIntroductions_showsUnsetMarkers() {
        SupporterDetailData data = new SupporterDetailData(Optional.of(new PersonBuilder().build()));
        assertEquals("-", data.getIntroducer());
        assertEquals("-", data.getIntroductions());

        // introductions are not shown when no supporter is selected
        SupporterDetailData noSelection = new SupporterDetailData(Optional.empty(),
                List.of(new PersonBuilder().build()));
        assertEquals("-", noSelection.getIntroductions());
    }

    @Test
    public void constructor_noSelection_showsStartupState() {
        SupporterDetailData data = new SupporterDetailData(Optional.empty());

        assertFalse(data.hasSupporter());
        assertEquals("Type view INDEX to see a supporter's details and history.", data.getPlaceholderMessage());
        assertEquals("", data.getHistoryHeading());
        assertTrue(data.getInteractionEntries().isEmpty());
        assertEquals("-", data.getName());
        assertEquals("-", data.getStage());
        assertEquals("-", data.getOrganisation());
        assertEquals("-", data.getPhone());
        assertEquals("-", data.getEmail());
        assertEquals("-", data.getAddress());
        assertEquals("-", data.getTags());
    }

    @Test
    public void constructor_allFieldsSet_showsDetailsAndSortedTags() {
        Person person = new PersonBuilder()
                .withName("Tan Wei Ming")
                .withStage("cultivating")
                .withOrganisation("DBS Bank")
                .withPhone("91234567")
                .withEmail("weiming@dbs.com")
                .withAddress("12 Example Road")
                .withTags("monthly", "boardIntro", "gala2025")
                .build();
        SupporterDetailData data = new SupporterDetailData(Optional.of(person));

        assertTrue(data.hasSupporter());
        assertEquals("Tan Wei Ming", data.getName());
        assertEquals("Cultivating", data.getStage());
        assertEquals("DBS Bank", data.getOrganisation());
        assertEquals("91234567", data.getPhone());
        assertEquals("weiming@dbs.com", data.getEmail());
        assertEquals("12 Example Road", data.getAddress());
        assertEquals("boardIntro, gala2025, monthly", data.getTags());
    }

    @Test
    public void getTags_mixedCaseTags_sortsIgnoringCase() {
        Person person = new PersonBuilder().withTags("VIP", "boardIntro", "monthlyDonor").build();
        SupporterDetailData data = new SupporterDetailData(Optional.of(person));

        assertEquals("boardIntro, monthlyDonor, VIP", data.getTags());
    }

    @Test
    public void constructor_optionalFieldsUnset_showsDashes() {
        Person person = new PersonBuilder().withoutPhone().withoutAddress().withTags().build();
        SupporterDetailData data = new SupporterDetailData(Optional.of(person));

        assertTrue(data.hasSupporter());
        assertEquals("-", data.getPhone());
        assertEquals("-", data.getAddress());
        assertEquals("-", data.getOrganisation());
        assertEquals("-", data.getTags());
        assertEquals("amy@gmail.com", data.getEmail());
    }

    @Test
    public void constructor_emailUnset_showsDashAndPhone() {
        Person person = new PersonBuilder().withoutEmail().build();
        SupporterDetailData data = new SupporterDetailData(Optional.of(person));

        assertEquals("-", data.getEmail());
        assertEquals("85355255", data.getPhone());
    }

    @Test
    public void constructor_emptyHistory_showsLogPrompt() {
        Person person = new PersonBuilder().build();
        SupporterDetailData data = new SupporterDetailData(Optional.of(person));

        assertTrue(data.hasSupporter());
        assertEquals("Interactions (0) - newest first", data.getHistoryHeading());
        assertEquals("No interactions yet. Log one with: log INDEX nt/NOTE.", data.getPlaceholderMessage());
        assertTrue(data.getInteractionEntries().isEmpty());
    }

    @Test
    public void getLastContact_noSelection_returnsDash() {
        SupporterDetailData data = new SupporterDetailData(Optional.empty());

        assertEquals("-", data.getLastContact());
    }

    @Test
    public void getLastContact_emptyHistory_returnsNoInteractionsMessage() {
        Person person = new PersonBuilder().build();
        SupporterDetailData data = new SupporterDetailData(Optional.of(person));

        assertEquals("No interactions yet", data.getLastContact());
    }

    @Test
    public void getLastContact_outOfOrderDates_returnsLatestDateText() {
        Interaction firstInteraction = new Interaction(LocalDate.of(2026, 9, 16), "Called");
        Interaction latestInteraction = new Interaction(LocalDate.of(2026, 10, 3), "Met");
        Interaction lastLoggedInteraction = new Interaction(LocalDate.of(2026, 9, 30), "Emailed");
        Person person = new PersonBuilder()
                .withInteractions(firstInteraction, latestInteraction, lastLoggedInteraction)
                .build();
        SupporterDetailData data = new SupporterDetailData(Optional.of(person));

        assertEquals("Last contact: 03 Oct 2026", data.getLastContact());
        assertEquals(List.of(firstInteraction, latestInteraction, lastLoggedInteraction), person.getInteractions());
    }

    @Test
    public void getInteractionEntries_mixedDates_numbersByDateThenLoggingOrderAndPreservesStoredHistory() {
        Interaction firstInteraction = new Interaction(LocalDate.of(2026, 9, 2), "First September meeting");
        Interaction secondInteraction = new Interaction(LocalDate.of(2027, 1, 1), "New year call");
        Interaction thirdInteraction = new Interaction(LocalDate.of(2026, 8, 14), "Earlier dated meeting");
        Interaction fourthInteraction = new Interaction(LocalDate.of(2026, 9, 2), "Second September meeting");
        List<Interaction> loggingOrder = List.of(firstInteraction, secondInteraction,
                thirdInteraction, fourthInteraction);
        Person person = new PersonBuilder()
                .withInteractions(firstInteraction, secondInteraction, thirdInteraction, fourthInteraction)
                .build();
        SupporterDetailData data = new SupporterDetailData(Optional.of(person));
        List<InteractionEntry> entries = data.getInteractionEntries();

        assertEquals(List.of(4, 3, 2, 1), entries.stream().map(InteractionEntry::getNumber).toList());
        assertEquals(List.of("01 Jan 2027", "02 Sep 2026", "02 Sep 2026", "14 Aug 2026"),
                entries.stream().map(InteractionEntry::getDate).toList());
        assertEquals(List.of("New year call", "Second September meeting",
                "First September meeting", "Earlier dated meeting"),
                entries.stream().map(InteractionEntry::getNote).toList());
        assertEquals(loggingOrder, person.getInteractions());
        assertEquals("Interactions (4) - newest first", data.getHistoryHeading());
        assertEquals("", data.getPlaceholderMessage());
    }

    @Test
    public void getInteractionEntries_identicalInteractions_keepsBothWithDisplayNumbers() {
        Interaction interaction = new Interaction(LocalDate.of(2026, 9, 16), "Called");
        Person person = new PersonBuilder().withInteractions(interaction, interaction).build();
        SupporterDetailData data = new SupporterDetailData(Optional.of(person));
        List<InteractionEntry> entries = data.getInteractionEntries();

        assertEquals(List.of(2, 1), entries.stream().map(InteractionEntry::getNumber).toList());
        assertEquals(List.of("Called", "Called"), entries.stream().map(InteractionEntry::getNote).toList());
        assertEquals(List.of(interaction, interaction), person.getInteractions());
    }

    @Test
    public void getInteractionEntries_longNote_preservesCompleteTextAndLineBreaks() {
        String note = "Meeting notes\n" + "x".repeat(480);
        Interaction interaction = new Interaction(LocalDate.of(2026, 9, 16), note);
        Person person = new PersonBuilder().withInteractions(interaction).build();
        SupporterDetailData data = new SupporterDetailData(Optional.of(person));

        assertEquals(note, data.getInteractionEntries().get(0).getNote());
    }

    @Test
    public void getInteractionEntries_modifyEntries_throwsUnsupportedOperationException() {
        Interaction interaction = new Interaction(LocalDate.of(2026, 9, 16), "Called");
        Person person = new PersonBuilder().withInteractions(interaction).build();
        SupporterDetailData data = new SupporterDetailData(Optional.of(person));

        assertThrows(UnsupportedOperationException.class, () -> data.getInteractionEntries().remove(0));
        assertEquals(1, data.getInteractionEntries().size());
        assertEquals(List.of(interaction), person.getInteractions());
    }
}
