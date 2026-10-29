package seedu.address.testutil;

import java.util.List;

/**
 * Provides invalid index inputs shared by parser tests.
 */
public final class IndexTestUtil {

    // one greater than Integer.MAX_VALUE, beyond the supported index range
    private static final String INDEX_ABOVE_INT_MAX = "2147483648";

    private IndexTestUtil() {
    }

    /**
     * Returns invalid index inputs covering a nonnumeric value, zero, a negative value,
     * an explicit plus sign, a decimal and integer overflow.
     *
     * @param nonNumericInput Nonnumeric example to include.
     */
    public static List<String> getInvalidIndexInputs(String nonNumericInput) {
        return List.of(nonNumericInput, "0", "-1", "+1", "1.5", INDEX_ABOVE_INT_MAX);
    }
}
