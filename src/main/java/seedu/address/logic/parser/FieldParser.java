package seedu.address.logic.parser;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the text given for one field into its value, e.g. {@link ParserUtil#parsePhone(String)}.
 */
@FunctionalInterface
interface FieldParser<T> {

    /**
     * Returns the value that {@code text} represents.
     *
     * @throws ParseException if {@code text} is not a valid value for the field.
     */
    T parse(String text) throws ParseException;
}
