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

    // blank inputs, unsupported formats and months or days outside 01-12 and 01-31
    public static final List<String> INVALID_DATE_FORMAT_INPUTS = List.of(
            "", "   ", "2024-2-29", "2024/02/29", "2024-13-01", "2024-01-32");

    // correctly formatted dates that do not exist in the calendar
    public static final List<String> NONEXISTENT_DATE_INPUTS = List.of("2024-02-30", "2023-02-29", "2024-04-31");

    private TypicalInteractionDates() {
    }
}
