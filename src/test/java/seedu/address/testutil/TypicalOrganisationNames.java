package seedu.address.testutil;

import java.util.List;

/**
 * Representative organisation names for checking Unicode length boundaries across application layers.
 */
public final class TypicalOrganisationNames {

    // keep the required boundary independent of the production limit
    public static final int ORGANISATION_CODE_POINT_LIMIT = 100;
    public static final int JOINED_EMOJI_CODE_POINT_COUNT = 3;

    public static final String ASCII_AND_EMOJI_AT_ORGANISATION_LIMIT =
            "a".repeat(ORGANISATION_CODE_POINT_LIMIT - 1) + "😀";
    public static final String EMOJI_ONLY_AT_ORGANISATION_LIMIT = "😀".repeat(ORGANISATION_CODE_POINT_LIMIT);
    // woman + zero-width joiner + laptop occupy three code points in one joined emoji
    public static final String JOINED_EMOJI_AT_ORGANISATION_LIMIT =
            "a".repeat(ORGANISATION_CODE_POINT_LIMIT - JOINED_EMOJI_CODE_POINT_COUNT) + "👩‍💻";
    public static final List<String> UNICODE_ORGANISATIONS_AT_LIMIT = List.of(
            ASCII_AND_EMOJI_AT_ORGANISATION_LIMIT, EMOJI_ONLY_AT_ORGANISATION_LIMIT,
            JOINED_EMOJI_AT_ORGANISATION_LIMIT);

    private TypicalOrganisationNames() {
    }
}
