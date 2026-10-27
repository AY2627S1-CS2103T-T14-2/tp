package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests selection across history without using a mutable name or displayed index as identity.
 */
public class ViewedPersonHistoryTest {
    private final Model model = new ModelManager(new AddressBookBuilder().withPerson(ALICE)
            .withPerson(BENSON).build(), new UserPrefs());

    @Test
    public void undoRedo_viewedSupporterRenamed_restoresCorrespondingRecord() {
        model.setViewedPerson(ALICE);
        Person renamed = renamePerson(new PersonBuilder(ALICE).build(), "Renamed Supporter");
        assertSame(renamed, model.getViewedPerson().orElseThrow());

        model.undoAddressBook();
        assertSame(ALICE, model.getViewedPerson().orElseThrow());
        model.redoAddressBook();
        assertSame(renamed, model.getViewedPerson().orElseThrow());
    }

    @Test
    public void undoRedo_multipleEdits_followsSameSupporter() {
        model.setViewedPerson(ALICE);
        Person renamed = renamePerson(ALICE, "Renamed Supporter");
        Person edited = new PersonBuilder(renamed).withPhone("123").build();
        model.setPerson(renamed, edited);
        model.commitAddressBookIfChanged();

        model.undoAddressBook();
        assertSame(renamed, model.getViewedPerson().orElseThrow());
        model.undoAddressBook();
        assertSame(ALICE, model.getViewedPerson().orElseThrow());
        model.redoAddressBook();
        model.redoAddressBook();
        assertSame(edited, model.getViewedPerson().orElseThrow());
    }

    @Test
    public void undo_reusedName_doesNotSelectDifferentSupporter() {
        Person renamedAlice = renamePerson(ALICE, "Renamed Alice");
        Person renamedBenson = renamePerson(BENSON, ALICE.getName().fullName);
        model.setViewedPerson(renamedBenson);

        model.undoAddressBook();
        assertSame(BENSON, model.getViewedPerson().orElseThrow());
        model.undoAddressBook();
        assertSame(BENSON, model.getViewedPerson().orElseThrow());
        model.redoAddressBook();
        assertSame(BENSON, model.getViewedPerson().orElseThrow());
        model.setViewedPerson(renamedAlice);
        model.redoAddressBook();
        assertSame(renamedAlice, model.getViewedPerson().orElseThrow());
    }

    @Test
    public void undoRedo_otherSupporterDeleted_preservesSelectionDespiteShiftedIndex() {
        model.setViewedPerson(BENSON);
        model.deletePerson(ALICE);
        model.commitAddressBookIfChanged();

        model.undoAddressBook();
        assertSame(BENSON, model.getViewedPerson().orElseThrow());
        model.redoAddressBook();
        assertSame(BENSON, model.getViewedPerson().orElseThrow());
    }

    @Test
    public void undo_addedSupporter_clearsSelectionWithoutSelectingReusedName() {
        renamePerson(ALICE, "Renamed Alice");
        Person newAlice = new PersonBuilder(ALICE).build();
        model.addPerson(newAlice);
        model.commitAddressBookIfChanged();
        model.setViewedPerson(newAlice);

        model.undoAddressBook();
        assertFalse(model.getViewedPerson().isPresent());
        model.redoAddressBook();
        assertFalse(model.getViewedPerson().isPresent());
    }

    @Test
    public void undo_afterClear_doesNotRestoreClearedSelection() {
        model.setViewedPerson(ALICE);
        renamePerson(ALICE, "Renamed Alice");
        model.setAddressBook(new AddressBook());
        model.commitAddressBookIfChanged();

        model.undoAddressBook();
        assertFalse(model.getViewedPerson().isPresent());
    }

    @Test
    public void redo_afterNoOpEdit_retainsSupporterIdentity() {
        model.setViewedPerson(ALICE);
        Person renamed = renamePerson(ALICE, "Renamed Alice");
        model.undoAddressBook();
        model.setPerson(ALICE, new PersonBuilder(ALICE).build());
        model.commitAddressBookIfChanged();

        model.redoAddressBook();
        assertSame(renamed, model.getViewedPerson().orElseThrow());
    }

    @Test
    public void undoRedo_afterFailedEditAndNewHistory_followsNewBranch() {
        model.setViewedPerson(ALICE);
        Person renamed = renamePerson(ALICE, "Renamed Alice");
        assertThrows(DuplicatePersonException.class, () -> model.setPerson(renamed, BENSON));
        assertSame(renamed, model.getViewedPerson().orElseThrow());
        model.undoAddressBook();
        Person replacement = renamePerson(ALICE, CARL.getName().fullName);
        assertFalse(model.canRedoAddressBook());

        model.undoAddressBook();
        assertSame(ALICE, model.getViewedPerson().orElseThrow());
        model.redoAddressBook();
        assertSame(replacement, model.getViewedPerson().orElseThrow());
    }

    private Person renamePerson(Person target, String name) {
        Person renamed = new PersonBuilder(target).withName(name).build();
        model.setPerson(target, renamed);
        model.commitAddressBookIfChanged();
        return renamed;
    }
}
