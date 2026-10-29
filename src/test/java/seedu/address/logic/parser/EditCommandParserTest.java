package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ORGANISATION_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ORGANISATION_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ORGANISATION_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ORGANISATION_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORGANISATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.IndexTestUtil.getInvalidIndexInputs;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;
import static seedu.address.testutil.TypicalOrganisationNames.ASCII_AND_EMOJI_AT_ORGANISATION_LIMIT;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Organisation;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {

    private static final String TAG_EMPTY = " " + PREFIX_TAG;

    private static final String MESSAGE_INVALID_INDEX =
            ParserUtil.MESSAGE_INVALID_INDEX + "\n" + EditCommand.MESSAGE_USAGE;

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);

    private EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_organisationUnicodeLengthBoundary_checksCodePoints() {
        String organisationAtLimit = ASCII_AND_EMOJI_AT_ORGANISATION_LIMIT;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withOrganisation(organisationAtLimit).build();
        String input = INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_ORGANISATION + organisationAtLimit;

        assertParseSuccess(parser, input, new EditCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseFailure(parser, input + "a", Organisation.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidIndexValues_reportsIndexErrorAndUsage() {
        for (String invalidIndex : getInvalidIndexInputs("abc")) {
            assertParseFailure(parser, invalidIndex + " n/Amy", MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_accentedName_success() {
        assertNameParses("José Tan");
    }

    @Test
    public void parse_nonLatinName_success() {
        assertNameParses("李伟");
    }

    @Test
    public void parse_curlyApostropheName_success() {
        assertNameParses("Charlotte O’Brien");
    }

    @Test
    public void parse_supportedNames_success() {
        String[] names = {"O'Brien", "Lee-Tan", "J. Tan", "Ravi s/o Kumar", "Priya d/o Kumar",
            "Ravi S/O Kumar", "Priya D/O Kumar", "J. O'Brien-Tan d/o Kumar",
            "Ahmad bin Ali @ Ah Mat", "Tan Ah Kow (Chen Ah Kow)", "Tan, Ah Kow"};

        for (String name : names) {
            assertNameParses(name);
        }
    }

    @Test
    public void parse_supportedNamesWithAddress_success() {
        String[] names = {"Ravi s/o Kumar", "Priya d/o Kumar", "J. O'Brien-Tan D/O Kumar",
            "Ahmad bin Ali @ Ah Mat", "Tan Ah Kow (Chen Ah Kow)", "Tan, Ah Kow"};

        for (String name : names) {
            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(name)
                    .withAddress(VALID_ADDRESS_AMY).build();
            EditCommand expectedCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

            assertParseSuccess(parser, "1 " + PREFIX_NAME + name + ADDRESS_DESC_AMY, expectedCommand);
            assertParseSuccess(parser, "1" + ADDRESS_DESC_AMY + " " + PREFIX_NAME + name, expectedCommand);
        }
    }

    @Test
    public void parse_supportedNameWithExtraSpaces_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName("J. O'Brien-Tan s/o Kumar")
                .withAddress(VALID_ADDRESS_AMY).build();

        assertParseSuccess(parser, "1 " + PREFIX_NAME + "  J.   O'Brien-Tan   s/o   Kumar  " + ADDRESS_DESC_AMY,
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_nameWithNonBreakingSpaces_success() {
        String[] names = {"Tan\u00a0Wei Ming", "\u00a0Tan Wei Ming", "Tan Wei Ming\u00a0",
            "Tan\u00a0\u00a0Wei Ming", " \u00a0Tan \t\u00a0Wei\u00a0 Ming\u00a0 "};
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName("Tan Wei Ming")
                .withPhone(VALID_PHONE_AMY).build();
        EditCommand expectedCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        for (String name : names) {
            assertParseSuccess(parser, "1 " + PREFIX_NAME + name + PHONE_DESC_AMY, expectedCommand);
            assertParseSuccess(parser, "1" + PHONE_DESC_AMY + " " + PREFIX_NAME + name, expectedCommand);
        }
    }

    @Test
    public void parse_invalidNameForms_failure() {
        String[] names = {"'Tan", "-Tan", ".Tan", "Tan/Lim", "Ravi s/ Kumar", "Ravis/o Kumar", "Priya d/oKumar"};

        for (String name : names) {
            assertParseFailure(parser, "1 " + PREFIX_NAME + name + ADDRESS_DESC_AMY, Name.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailure(parser, NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // no field specified
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);

        // no index and no field specified
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // negative index
        assertParseFailure(parser, "-5" + NAME_DESC_AMY, MESSAGE_INVALID_INDEX);

        // zero index
        assertParseFailure(parser, "0" + NAME_DESC_AMY, MESSAGE_INVALID_INDEX);

        assertParseFailure(parser, "1 x n/Bob", MESSAGE_INVALID_FORMAT);

        // invalid arguments being parsed as preamble
        assertParseFailure(parser, "1 some random string", MESSAGE_INVALID_FORMAT);

        // invalid prefix being parsed as preamble
        assertParseFailure(parser, "1 i/ string", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "1" + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS); // invalid name
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS); // invalid phone
        assertParseFailure(parser, "1" + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS); // invalid email
        assertParseFailure(parser, "1" + INVALID_TAG_DESC, Tag.MESSAGE_CONSTRAINTS); // invalid tag

        // invalid phone followed by valid email
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC + EMAIL_DESC_AMY, Phone.MESSAGE_CONSTRAINTS);

        // while parsing {@code PREFIX_TAG} alone will reset the tags of the {@code Person} being edited,
        // parsing it together with a valid tag results in error
        assertParseFailure(parser, "1" + TAG_DESC_FRIEND + TAG_DESC_HUSBAND + TAG_EMPTY,
                ParserUtil.MESSAGE_BLANK_TAG);
        assertParseFailure(parser, "1" + TAG_DESC_FRIEND + TAG_EMPTY + TAG_DESC_HUSBAND,
                ParserUtil.MESSAGE_BLANK_TAG);
        assertParseFailure(parser, "1" + TAG_EMPTY + TAG_DESC_FRIEND + TAG_DESC_HUSBAND,
                ParserUtil.MESSAGE_BLANK_TAG);

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailure(parser, "1" + INVALID_NAME_DESC + INVALID_EMAIL_DESC + VALID_ADDRESS_AMY + VALID_PHONE_AMY,
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        Index targetIndex = INDEX_SECOND_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + TAG_DESC_HUSBAND
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY + ORGANISATION_DESC_AMY + NAME_DESC_AMY + TAG_DESC_FRIEND;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_AMY).withAddress(VALID_ADDRESS_AMY)
                .withOrganisation(VALID_ORGANISATION_AMY).withTags(VALID_TAG_HUSBAND, VALID_TAG_FRIEND).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + EMAIL_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_formattedPhone_success() {
        String formattedPhone = "+65-9123 4567";
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(formattedPhone).build();

        assertParseSuccess(parser, "1 " + PREFIX_PHONE + formattedPhone,
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_annotatedPhone_preservesFormatting() {
        String[] phones = {"9123 4567 (HP)", "(65) 9123 4567", "9123 4567 ext 12", "+65 9123-4567 ext 12 (Office)"};
        for (String phone : phones) {
            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(phone).build();
            assertParseSuccess(parser, "1 " + PREFIX_PHONE + phone, new EditCommand(INDEX_FIRST_PERSON, descriptor));
        }
    }

    @Test
    public void parse_malformedPhoneAnnotations_failure() {
        String[] phones = {"(65 9123 4567", "9123 4567 (HP2)", "9123 4567 ext 1234567", "12 ext 123"};
        for (String phone : phones) {
            assertParseFailure(parser, "1 " + PREFIX_PHONE + phone, Phone.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        // name
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + NAME_DESC_AMY;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // phone
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // email
        userInput = targetIndex.getOneBased() + EMAIL_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // address
        userInput = targetIndex.getOneBased() + ADDRESS_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withAddress(VALID_ADDRESS_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // organization
        userInput = targetIndex.getOneBased() + ORGANISATION_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withOrganisation(VALID_ORGANISATION_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // tags
        userInput = targetIndex.getOneBased() + TAG_DESC_FRIEND;
        descriptor = new EditPersonDescriptorBuilder().withTags(VALID_TAG_FRIEND).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_multipleRepeatedFields_failure() {
        // More extensive testing of duplicate parameter detections is done in
        // AddCommandParserTest#parse_repeatedNonTagValue_failure()

        // valid followed by invalid
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + PHONE_DESC_BOB;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid followed by valid
        userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + INVALID_PHONE_DESC;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple valid fields repeated
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY + ADDRESS_DESC_AMY + EMAIL_DESC_AMY
                + TAG_DESC_FRIEND + PHONE_DESC_AMY + ADDRESS_DESC_AMY + EMAIL_DESC_AMY + TAG_DESC_FRIEND
                + PHONE_DESC_BOB + ADDRESS_DESC_BOB + EMAIL_DESC_BOB + TAG_DESC_HUSBAND;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS));

        // multiple organizations
        userInput = targetIndex.getOneBased() + ORGANISATION_DESC_AMY + ORGANISATION_DESC_BOB;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ORGANISATION));

        // multiple invalid values
        userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + INVALID_ADDRESS_DESC + INVALID_EMAIL_DESC
                + INVALID_PHONE_DESC + INVALID_ADDRESS_DESC + INVALID_EMAIL_DESC;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS));
    }

    @Test
    public void parse_clearOptionalFields_success() {
        Index targetIndex = INDEX_FIRST_PERSON;

        // each optional field on its own
        assertParseSuccess(parser, targetIndex.getOneBased() + " " + PREFIX_PHONE,
                new EditCommand(targetIndex, new EditPersonDescriptorBuilder().withoutPhone().build()));
        assertParseSuccess(parser, targetIndex.getOneBased() + " " + PREFIX_EMAIL,
                new EditCommand(targetIndex, new EditPersonDescriptorBuilder().withoutEmail().build()));
        assertParseSuccess(parser, targetIndex.getOneBased() + " " + PREFIX_ADDRESS,
                new EditCommand(targetIndex, new EditPersonDescriptorBuilder().withoutAddress().build()));
        assertParseSuccess(parser, targetIndex.getOneBased() + " " + PREFIX_ORGANISATION,
                new EditCommand(targetIndex, new EditPersonDescriptorBuilder().withoutOrganisation().build()));

        // only spaces after the prefix also clears it
        assertParseSuccess(parser, targetIndex.getOneBased() + INVALID_ORGANISATION_DESC,
                new EditCommand(targetIndex, new EditPersonDescriptorBuilder().withoutOrganisation().build()));

        // so do no-break spaces, which String#isBlank does not recognize
        EditPersonDescriptor clearedDescriptor = new EditPersonDescriptorBuilder().withoutAddress()
                .withoutOrganisation().build();
        assertParseSuccess(parser, targetIndex.getOneBased() + " " + PREFIX_ADDRESS + "\u00a0 "
                + PREFIX_ORGANISATION + "\u00a0", new EditCommand(targetIndex, clearedDescriptor));

        // cleared fields together with an edited one
        String userInput = targetIndex.getOneBased() + INVALID_ADDRESS_DESC + PHONE_DESC_AMY + " " + PREFIX_EMAIL;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withoutAddress()
                .withPhone(VALID_PHONE_AMY).withoutEmail().build();
        assertParseSuccess(parser, userInput, new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_clearName_failure() {
        // the name is required, so it cannot be cleared
        assertParseFailure(parser, "1 " + PREFIX_NAME, Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_resetTags_success() {
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + TAG_EMPTY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags().build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    private void assertNameParses(String name) {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(name).build();

        assertParseSuccess(parser, INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_NAME + name,
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }
}
