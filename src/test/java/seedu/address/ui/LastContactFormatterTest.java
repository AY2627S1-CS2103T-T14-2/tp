package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Tests the shared last-contact text without starting JavaFX.
 */
public class LastContactFormatterTest {

    @Test
    public void format_nullDate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> LastContactFormatter.format(null));
    }

    @Test
    public void format_emptyDate_returnsNoInteractionsMessage() {
        assertEquals("No interactions yet", LastContactFormatter.format(Optional.empty()));
    }

    @Test
    public void format_singleDigitDay_includesLeadingZero() {
        assertFormattedDate(LocalDate.of(2026, 10, 3), "Last contact: 03 Oct 2026");
    }

    @Test
    public void format_doubleDigitDay_preservesBothDigits() {
        assertFormattedDate(LocalDate.of(2026, 9, 16), "Last contact: 16 Sep 2026");
    }

    @Test
    public void format_leapDay_returnsLeapDayText() {
        assertFormattedDate(LocalDate.of(2024, 2, 29), "Last contact: 29 Feb 2024");
    }

    @Test
    public void format_yearBoundary_preservesCalendarYear() {
        assertFormattedDate(LocalDate.of(2023, 12, 31), "Last contact: 31 Dec 2023");
        assertFormattedDate(LocalDate.of(2024, 1, 1), "Last contact: 01 Jan 2024");
    }

    @Test
    @ResourceLock("java.util.Locale.default")
    public void format_nonEnglishDefaultLocale_usesEnglishMonth() {
        Locale originalLocale = Locale.getDefault(Locale.Category.FORMAT);
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.FRENCH);

            assertFormattedDate(LocalDate.of(2026, 10, 3), "Last contact: 03 Oct 2026");
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, originalLocale);
        }
    }

    /**
     * Verifies the complete last-contact text for a supplied interaction date.
     */
    private void assertFormattedDate(LocalDate date, String expectedText) {
        assertEquals(expectedText, LastContactFormatter.format(Optional.of(date)));
    }
}
