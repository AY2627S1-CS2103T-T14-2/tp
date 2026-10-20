package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.LogCommand;
import seedu.address.logic.commands.StageCommand;
import seedu.address.logic.commands.StatsCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.Stage;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " 3") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_log() throws Exception {
        String note = "Called to discuss the year-end appeal";
        LogCommand command = (LogCommand) parser.parseCommand(LogCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " nt/" + note);
        assertEquals(new LogCommand(INDEX_FIRST_PERSON, note), command);
    }

    @Test
    public void parseCommand_stage() throws Exception {
        StageCommand command = (StageCommand) parser.parseCommand(
                StageCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased() + " cultivating");
        assertEquals(new StageCommand(INDEX_FIRST_PERSON, Stage.CULTIVATING), command);
    }

    @Test
    public void parseCommand_stageWrongCase_throwsParseException() {
        // command words are case-sensitive
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("STAGE 1 giving"));
    }

    @Test
    public void parseCommand_view_returnsViewCommand() throws Exception {
        assertEquals(new ViewCommand(INDEX_FIRST_PERSON), parser.parseCommand("view 1"));
        assertEquals(new ViewCommand(INDEX_FIRST_PERSON), parser.parseCommand("  view\t1  "));
    }

    @Test
    public void parseCommand_viewInvalidArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("view"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("view abc"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("view 0"));
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand("view 1 2"));
    }

    @Test
    public void parseCommand_viewWrongCase_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("VIEW 1"));
    }

    @Test
    public void parseCommand_viewAfterFind_selectsSupporterAndKeepsFilteredList() throws Exception {
        Model model = new ModelManager();
        Person alice = new PersonBuilder().withName("Alice").build();
        Person benson = new PersonBuilder().withName("Benson").build();
        model.addPerson(alice);
        model.addPerson(benson);
        parser.parseCommand("find Benson").execute(model);
        assertEquals(List.of(benson), model.getFilteredPersonList());

        assertEquals("Showing Benson: no interactions logged yet",
                parser.parseCommand("view 1").execute(model).getFeedbackToUser());
        assertEquals(Optional.of(benson), model.getViewedPerson());
        assertEquals(List.of(benson), model.getFilteredPersonList());
        assertEquals(List.of(alice, benson), model.getAddressBook().getPersonList());
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " 3") instanceof ListCommand);
    }

    @Test
    public void parseCommand_stats() throws Exception {
        assertTrue(parser.parseCommand(StatsCommand.COMMAND_WORD) instanceof StatsCommand);
        assertTrue(parser.parseCommand(StatsCommand.COMMAND_WORD + " 3") instanceof StatsCommand);
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }
}
