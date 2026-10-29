package seedu.address.testutil;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

/**
 * Representative date inputs and a fixed clock for interaction validation tests.
 */
public final class TypicalInteractionDates {

    // today is 20 October locally, although the UTC date is still 19 October
    public static final Clock INTERACTION_CLOCK = Clock.fixed(
            Instant.parse("2026-10-19T16:30:00Z"), ZoneId.of("Asia/Singapore"));

    // blank inputs, unsupported formats and impossible calendar dates
    public static final List<String> INVALID_DATE_INPUTS = List.of(
            "", "   ", "2024-2-29", "2024/02/29", "2024-02-30", "2023-02-29");

    private TypicalInteractionDates() {
    }
}
