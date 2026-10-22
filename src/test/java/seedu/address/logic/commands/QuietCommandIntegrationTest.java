package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests commands against quiet-list indices and verifies storage, selection and undo/redo behavior.
 */
public class QuietCommandIntegrationTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 21);
    private static final ZoneId ZONE = ZoneId.of("Asia/Singapore");
    private static final Clock CLOCK = Clock.fixed(TODAY.atStartOfDay(ZONE).toInstant(), ZONE);

    @TempDir
    private Path temporaryFolder;

    private final Person recent = createSupporter("Recent", 10);
    private final Person old = createSupporter("Old", 100);
    private final Person never = new PersonBuilder().withName("Never").build();
    private final Person older = createSupporter("Older", 200);
    private AddressBook original;
    private Model model;
    private Logic logic;
    private JsonAddressBookStorage addressBookStorage;

    @BeforeEach
    public void setUp() {
        original = new AddressBook();
        for (Person person : List.of(recent, old, never, older)) {
            original.addPerson(person);
        }
        model = new ModelManager(original, new UserPrefs());
        addressBookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("supporterbook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"));
        logic = new LogicManager(model, new StorageManager(addressBookStorage, userPrefsStorage));
    }

    @Test
    public void execute_listAndFindAfterQuiet_restoreUnderlyingOrder() throws Exception {
        executeQuiet(60);
        assertEquals(List.of(never, older, old), model.getFilteredPersonList());

        logic.execute("find e");
        assertEquals(List.of(recent, never, older), model.getFilteredPersonList());

        executeQuiet(60);
        logic.execute("list");
        assertEquals(original.getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_newQuiet_replacesFilterAndThreshold() throws Exception {
        logic.execute("find Recent");
        executeQuiet(60);
        assertEquals(List.of(never, older, old), model.getFilteredPersonList());

        executeQuiet(180);
        assertEquals(List.of(never, older), model.getFilteredPersonList());

        executeQuiet(500);
        assertEquals(List.of(never), model.getFilteredPersonList());
    }

    @Test
    public void execute_quiet_preservesViewedSupporterAndCreatesNoUndoPoint() throws Exception {
        logic.execute("view 1");
        executeQuiet(60);

        assertEquals(Optional.of(recent), model.getViewedPerson());
        assertEquals(original, model.getAddressBook());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_viewAndDelete_useQuietListIndices() throws Exception {
        executeQuiet(60);
        logic.execute("view 1");
        assertEquals(Optional.of(never), model.getViewedPerson());

        logic.execute("delete 1");

        AddressBook expected = new AddressBook(original);
        expected.removePerson(never);
        assertEquals(expected, model.getAddressBook());
        assertEquals(List.of(older, old), model.getFilteredPersonList());
        assertTrue(model.getViewedPerson().isEmpty());
    }

    @Test
    public void execute_edit_usesQuietIndexAndRestoresNormalOrdering() throws Exception {
        executeQuiet(60);
        logic.execute("edit 2 o/DBS Bank");

        Person editedOlder = new PersonBuilder(older).withOrganisation("DBS Bank").build();
        AddressBook expected = new AddressBook(original);
        expected.setPerson(older, editedOlder);
        assertEquals(expected, model.getAddressBook());
        assertEquals(expected.getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_log_removesNowContactedSupporterFromQuietList() throws Exception {
        executeQuiet(60);
        new LogCommand(INDEX_FIRST_PERSON, "Called today", CLOCK).execute(model);
        model.commitAddressBookIfChanged();

        Person updatedNever = new PersonBuilder(never)
                .withInteractions(new Interaction(TODAY, "Called today")).build();
        AddressBook expected = new AddressBook(original);
        expected.setPerson(never, updatedNever);
        assertEquals(expected, model.getAddressBook());
        assertEquals(List.of(older, old), model.getFilteredPersonList());
        assertEquals(Optional.of(updatedNever), model.getViewedPerson());
    }

    @Test
    public void execute_unlog_movesNowUncontactedSupporterToFront() throws Exception {
        executeQuiet(60);
        logic.execute("unlog 3 1");

        Person updatedOld = new PersonBuilder(old).withInteractions().build();
        AddressBook expected = new AddressBook(original);
        expected.setPerson(old, updatedOld);
        assertEquals(expected, model.getAddressBook());
        assertEquals(List.of(updatedOld, never, older), model.getFilteredPersonList());
    }

    @Test
    public void execute_stageDeclined_removesSupporterFromQuietList() throws Exception {
        executeQuiet(60);
        logic.execute("stage 1 declined");

        assertEquals(List.of(older, old), model.getFilteredPersonList());
        AddressBook expected = new AddressBook(original);
        expected.setPerson(never, new PersonBuilder(never).withStage("declined").build());
        assertEquals(expected, model.getAddressBook());
    }

    @Test
    public void execute_tagNeverContactedSupporters_preservesFullListOrderWithinTies() throws Exception {
        Person secondNever = new PersonBuilder().withName("Second Never").build();
        Person thirdNever = new PersonBuilder().withName("Third Never").build();
        model.addPerson(secondNever);
        model.addPerson(thirdNever);
        model.commitAddressBookIfChanged();
        executeQuiet(60);
        assertEquals(List.of(never, secondNever, thirdNever, older, old), model.getFilteredPersonList());

        logic.execute("tag 1 t/vip");
        Person taggedNever = new PersonBuilder(never).withTags("vip").build();
        assertEquals(List.of(taggedNever, secondNever, thirdNever, older, old), model.getFilteredPersonList());

        logic.execute("tag 3 t/boardIntro");
        Person taggedThirdNever = new PersonBuilder(thirdNever).withTags("boardIntro").build();
        assertEquals(List.of(taggedNever, secondNever, taggedThirdNever, older, old), model.getFilteredPersonList());

        logic.execute("untag 1 t/vip");
        assertEquals(List.of(never, secondNever, taggedThirdNever, older, old), model.getFilteredPersonList());
        assertEquals(List.of(recent, old, never, older, secondNever, taggedThirdNever),
                model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_tagEqualContactDates_preservesFullListOrderWithinTies() throws Exception {
        Person secondOld = createSupporter("Second Old", 100);
        Person thirdOld = createSupporter("Third Old", 100);
        model.addPerson(secondOld);
        model.addPerson(thirdOld);
        model.commitAddressBookIfChanged();
        executeQuiet(60);
        assertEquals(List.of(never, older, old, secondOld, thirdOld), model.getFilteredPersonList());

        logic.execute("tag 3 t/vip");
        Person taggedOld = new PersonBuilder(old).withTags("vip").build();
        assertEquals(List.of(never, older, taggedOld, secondOld, thirdOld), model.getFilteredPersonList());

        logic.execute("tag 5 t/boardIntro");
        Person taggedThirdOld = new PersonBuilder(thirdOld).withTags("boardIntro").build();
        assertEquals(List.of(never, older, taggedOld, secondOld, taggedThirdOld), model.getFilteredPersonList());
        assertEquals(List.of(recent, taggedOld, never, older, secondOld, taggedThirdOld),
                model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_logCreatesContactDateTie_reordersUsingFullListPosition() throws Exception {
        executeQuiet(60);
        logic.execute("log 2 nt/Follow up dt/" + TODAY.minusDays(100));

        Person updatedOlder = new PersonBuilder(older)
                .withInteractions(older.getInteractions().get(0),
                        new Interaction(TODAY.minusDays(100), "Follow up"))
                .build();
        assertEquals(List.of(never, old, updatedOlder), model.getFilteredPersonList());

        logic.execute("tag 2 t/vip");
        Person taggedOld = new PersonBuilder(old).withTags("vip").build();
        assertEquals(List.of(never, taggedOld, updatedOlder), model.getFilteredPersonList());
        assertEquals(List.of(recent, taggedOld, never, updatedOlder), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_saveDuringQuiet_preservesStoredOrder() throws Exception {
        executeQuiet(60);
        logic.execute("view 1");

        assertEquals(List.of(never, older, old), model.getFilteredPersonList());
        assertEquals(original, addressBookStorage.readAddressBook().orElseThrow());
        assertEquals(original.getPersonList(), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_invalidQuiet_preservesViewAndResults() throws Exception {
        executeQuiet(60);
        logic.execute("view 1");

        assertThrows(ParseException.class, () -> logic.execute("quiet 0"));
        assertThrows(ParseException.class, () -> logic.execute("quiet 30 60"));

        assertEquals(List.of(never, older, old), model.getFilteredPersonList());
        assertEquals(Optional.of(never), model.getViewedPerson());
        assertEquals(original, model.getAddressBook());
    }

    @Test
    public void execute_quietAfterUndo_preservesRedoAndRestoresNormalOrder() throws Exception {
        logic.execute("delete 2");
        logic.execute("undo");
        executeQuiet(60);
        assertTrue(model.canRedoAddressBook());

        logic.execute("redo");
        assertEquals(List.of(recent, never, older), model.getFilteredPersonList());

        logic.execute("undo");
        assertEquals(original.getPersonList(), model.getFilteredPersonList());
        assertEquals(original, model.getAddressBook());
    }

    @Test
    public void execute_quietThroughLogic_savesDataWithoutUndoPoint() throws Exception {
        // The maximum threshold excludes all dated fixtures, independently of the current day.
        CommandResult result = logic.execute("quiet 2147483647");

        assertEquals(String.format(QuietCommand.MESSAGE_SUCCESS, 1, Integer.MAX_VALUE), result.getFeedbackToUser());
        assertEquals(List.of(never), model.getFilteredPersonList());
        assertEquals(original, addressBookStorage.readAddressBook().orElseThrow());
        assertFalse(model.canUndoAddressBook());
    }

    private void executeQuiet(int days) {
        new QuietCommand(days, CLOCK).execute(model);
        model.commitAddressBookIfChanged();
    }

    private Person createSupporter(String name, int daysAgo) {
        return new PersonBuilder().withName(name)
                .withInteractions(new Interaction(TODAY.minusDays(daysAgo), "Contact")).build();
    }
}
