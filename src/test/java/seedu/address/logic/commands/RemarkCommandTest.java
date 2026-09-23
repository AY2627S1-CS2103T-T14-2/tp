package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;

public class RemarkCommandTest {

    private final Model model = new ModelManager();

    @Test
    public void execute_displaysParsedArguments() {
        String remark = "Some remark";
        RemarkCommand remarkCommand = new RemarkCommand(INDEX_FIRST_PERSON, remark);

        assertCommandFailure(remarkCommand, model,
                String.format(RemarkCommand.MESSAGE_ARGUMENTS, INDEX_FIRST_PERSON.getOneBased(), remark));
    }

    @Test
    public void equals() {
        RemarkCommand standardCommand = new RemarkCommand(INDEX_FIRST_PERSON, "Likes skiing");

        assertTrue(standardCommand.equals(new RemarkCommand(INDEX_FIRST_PERSON, "Likes skiing")));
        assertTrue(standardCommand.equals(standardCommand));
        assertFalse(standardCommand.equals(null));
        assertFalse(standardCommand.equals(new ClearCommand()));
        assertFalse(standardCommand.equals(new RemarkCommand(INDEX_SECOND_PERSON, "Likes skiing")));
        assertFalse(standardCommand.equals(new RemarkCommand(INDEX_FIRST_PERSON, "Likes swimming")));
    }
}
