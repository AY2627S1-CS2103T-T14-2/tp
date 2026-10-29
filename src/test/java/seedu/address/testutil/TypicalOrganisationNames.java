package seedu.address.testutil;

/**
 * Representative organisation names for checking Unicode length boundaries across application layers.
 */
public final class TypicalOrganisationNames {

    // keep the required boundary independent of the production limit
    public static final int ORGANISATION_CODE_POINT_LIMIT = 100;

    public static final String ASCII_AND_EMOJI_AT_ORGANISATION_LIMIT =
            "a".repeat(ORGANISATION_CODE_POINT_LIMIT - 1) + "😀";

    private TypicalOrganisationNames() {
    }
}
