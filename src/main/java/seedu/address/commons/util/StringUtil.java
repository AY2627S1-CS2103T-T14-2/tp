package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * Helper functions for handling strings.
 */
public class StringUtil {

    /**
     * Removes leading and trailing whitespace and Unicode space separators from a string,
     * including NEXT LINE (U+0085).
     * Internal spacing is preserved. Characters removed by {@link String#trim()} are also removed.
     *
     * @param text The non-null string to trim.
     * @return The trimmed string, or an empty string if it contains only trimmable characters.
     * @throws NullPointerException if {@code text} is null.
     */
    public static String trimUnicodeWhitespace(String text) {
        requireNonNull(text);
        int start = 0;
        int end = text.length();
        while (start < end && isTrimmableWhitespace(text.codePointAt(start))) {
            start += Character.charCount(text.codePointAt(start));
        }
        while (start < end && isTrimmableWhitespace(text.codePointBefore(end))) {
            end -= Character.charCount(text.codePointBefore(end));
        }
        return text.substring(start, end);
    }

    /**
     * Returns true if {@code text} is empty or contains only characters removed by
     * {@link #trimUnicodeWhitespace(String)}, such as NO-BREAK SPACE (U+00A0), which {@link String#isBlank()}
     * does not recognise.
     *
     * @throws NullPointerException if {@code text} is null.
     */
    public static boolean isUnicodeBlank(String text) {
        return trimUnicodeWhitespace(text).isEmpty();
    }

    private static boolean isTrimmableWhitespace(int codePoint) {
        // NEXT LINE is Unicode whitespace but is excluded by both Java Character predicates below
        return codePoint <= ' ' || codePoint == '\u0085'
                || Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint);
    }

    /**
     * Returns true if the {@code sentence} contains the {@code word}.
     *   Ignores case, but a full word match is required.
     *   <br>examples:<pre>
     *       containsWordIgnoreCase("ABc def", "abc") == true
     *       containsWordIgnoreCase("ABc def", "DEF") == true
     *       containsWordIgnoreCase("ABc def", "AB") == false //not a full word match
     *       </pre>
     * @param sentence cannot be null
     * @param word cannot be null, cannot be empty, must be a single word
     */
    public static boolean containsWordIgnoreCase(String sentence, String word) {
        requireNonNull(sentence);
        requireNonNull(word);

        String preppedWord = word.trim();
        checkArgument(!preppedWord.isEmpty(), "Word parameter cannot be empty");
        checkArgument(preppedWord.split("\\s+").length == 1, "Word parameter should be a single word");

        String preppedSentence = sentence;
        String[] wordsInPreppedSentence = preppedSentence.split("\\s+");

        return Arrays.stream(wordsInPreppedSentence)
                .anyMatch(preppedWord::equalsIgnoreCase);
    }

    /**
     * Returns a detailed message of {@code t}, including the stack trace.
     */
    public static String getDetails(Throwable t) {
        requireNonNull(t);
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return t.getMessage() + "\n" + sw.toString();
    }

    /**
     * Returns true if {@code s} represents a non-zero unsigned integer
     * e.g. 1, 2, 3, ..., {@code Integer.MAX_VALUE} <br>
     * Will return false for any other non-null string input
     * e.g. empty string, "-1", "0", "+1", and " 2 " (untrimmed), "3 0" (contains whitespace), "1 a" (contains letters)
     * @throws NullPointerException if {@code s} is null.
     */
    public static boolean isNonZeroUnsignedInteger(String s) {
        requireNonNull(s);

        try {
            int value = Integer.parseInt(s);
            return value > 0 && !s.startsWith("+"); // "+1" is successfully parsed by Integer#parseInt(String)
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}
