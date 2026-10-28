package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import seedu.address.ui.SupporterDetailData;
import seedu.address.ui.SupporterDetailData.InteractionEntry;

/**
 * Provides assertions for interaction history shown in supporter detail data.
 */
public final class InteractionHistoryTestUtil {

    private InteractionHistoryTestUtil() {
    }

    /**
     * Verifies entry numbers, formatted dates and notes in display order.
     */
    public static void assertHistory(SupporterDetailData display, List<Integer> expectedNumbers,
            List<String> expectedDates, List<String> expectedNotes) {
        List<InteractionEntry> entries = display.getInteractionEntries();
        assertEquals(expectedNumbers, entries.stream().map(InteractionEntry::getNumber).toList());
        assertEquals(expectedDates, entries.stream().map(InteractionEntry::getDate).toList());
        assertEquals(expectedNotes, entries.stream().map(InteractionEntry::getNote).toList());
    }
}
