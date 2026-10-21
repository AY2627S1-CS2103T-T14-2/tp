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
        assertEquals("Last contact: 03 Oct 2026",
                LastContactFormatter.format(Optional.of(LocalDate.of(2026, 10, 3))));
    }

    @Test
    public void format_doubleDigitDay_preservesBothDigits() {
        assertEquals("Last contact: 16 Sep 2026",
                LastContactFormatter.format(Optional.of(LocalDate.of(2026, 9, 16))));
    }

    @Test
    @ResourceLock("java.util.Locale.default")
    public void format_nonEnglishDefaultLocale_usesEnglishMonth() {
        Locale originalLocale = Locale.getDefault(Locale.Category.FORMAT);
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.FRENCH);

            assertEquals("Last contact: 03 Oct 2026",
                    LastContactFormatter.format(Optional.of(LocalDate.of(2026, 10, 3))));
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, originalLocale);
        }
    }
}
