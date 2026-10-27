package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class VersionedAddressBookTest {

    private final AddressBook initialState = new AddressBookBuilder().withPerson(ALICE).build();
    private final VersionedAddressBook versionedAddressBook = new VersionedAddressBook(initialState);

    @Test
    public void constructor_validInitialState_copiesDataWithNoHistory() {
        assertEquals(initialState, versionedAddressBook);
        assertFalse(versionedAddressBook.canUndo());
        assertFalse(versionedAddressBook.canRedo());
    }

    @Test
    public void getRetainedPersonVersions_includesCurrentAndHistoryByIdentity() {
        Person editedAlice = new PersonBuilder(ALICE).withName("Renamed Alice").build();
        versionedAddressBook.setPerson(ALICE, editedAlice);
        versionedAddressBook.commit();
        versionedAddressBook.undo();
        Person equalAlice = new PersonBuilder(ALICE).build();
        versionedAddressBook.setPerson(ALICE, equalAlice);

        Set<Person> retained = versionedAddressBook.getRetainedPersonVersions();
        assertTrue(retained.contains(ALICE));
        assertTrue(retained.contains(editedAlice));
        assertTrue(retained.contains(equalAlice));
        assertFalse(retained.contains(new PersonBuilder(ALICE).build()));

        versionedAddressBook.addPerson(BENSON);
        versionedAddressBook.commit();
        assertFalse(versionedAddressBook.getRetainedPersonVersions().contains(editedAlice));
    }

    @Test
    public void constructor_initialStateModifiedAfterwards_undoRestoresOriginalData() {
        AddressBook mutableInitialState = new AddressBookBuilder().withPerson(ALICE).build();
        VersionedAddressBook versioned = new VersionedAddressBook(mutableInitialState);
        mutableInitialState.addPerson(BENSON);
        versioned.addPerson(CARL);
        versioned.commit();

        versioned.undo();

        assertEquals(List.of(ALICE), versioned.getPersonList());
    }

    @Test
    public void commit_afterChange_canUndoButNotRedo() {
        versionedAddressBook.addPerson(BENSON);

        versionedAddressBook.commit();

        assertTrue(versionedAddressBook.canUndo());
        assertFalse(versionedAddressBook.canRedo());
    }

    @Test
    public void hasChangesSinceLastCommit_noChange_returnsFalse() {
        assertFalse(versionedAddressBook.hasChangesSinceLastCommit());
    }

    @Test
    public void hasChangesSinceLastCommit_afterChange_returnsTrue() {
        versionedAddressBook.addPerson(BENSON);

        assertTrue(versionedAddressBook.hasChangesSinceLastCommit());
    }

    @Test
    public void hasChangesSinceLastCommit_changeReverted_returnsFalse() {
        versionedAddressBook.addPerson(BENSON);
        versionedAddressBook.removePerson(BENSON);

        assertFalse(versionedAddressBook.hasChangesSinceLastCommit());
    }

    @Test
    public void hasChangesSinceLastCommit_afterCommitAndUndo_returnsFalse() {
        versionedAddressBook.addPerson(BENSON);
        versionedAddressBook.commit();
        assertFalse(versionedAddressBook.hasChangesSinceLastCommit());

        versionedAddressBook.undo();
        assertFalse(versionedAddressBook.hasChangesSinceLastCommit());
    }

    @Test
    public void undo_afterCommit_restoresPreviousState() {
        versionedAddressBook.addPerson(BENSON);
        versionedAddressBook.commit();

        versionedAddressBook.undo();

        assertEquals(List.of(ALICE), versionedAddressBook.getPersonList());
        assertFalse(versionedAddressBook.canUndo());
        assertTrue(versionedAddressBook.canRedo());
    }

    @Test
    public void undo_multipleCommits_restoresStatesInReverseOrder() {
        versionedAddressBook.addPerson(BENSON);
        versionedAddressBook.commit();
        versionedAddressBook.addPerson(CARL);
        versionedAddressBook.commit();

        versionedAddressBook.undo();
        assertEquals(List.of(ALICE, BENSON), versionedAddressBook.getPersonList());

        versionedAddressBook.undo();
        assertEquals(List.of(ALICE), versionedAddressBook.getPersonList());
    }

    @Test
    public void redo_afterUndo_restoresUndoneState() {
        versionedAddressBook.addPerson(BENSON);
        versionedAddressBook.commit();
        versionedAddressBook.undo();

        versionedAddressBook.redo();

        assertEquals(List.of(ALICE, BENSON), versionedAddressBook.getPersonList());
        assertTrue(versionedAddressBook.canUndo());
        assertFalse(versionedAddressBook.canRedo());
    }

    @Test
    public void commit_afterUndo_discardsUndoneStates() {
        versionedAddressBook.addPerson(BENSON);
        versionedAddressBook.commit();
        versionedAddressBook.undo();
        versionedAddressBook.addPerson(CARL);

        versionedAddressBook.commit();

        assertFalse(versionedAddressBook.canRedo());
        versionedAddressBook.undo();
        assertEquals(List.of(ALICE), versionedAddressBook.getPersonList());
        versionedAddressBook.redo();
        assertEquals(List.of(ALICE, CARL), versionedAddressBook.getPersonList());
    }

    @Test
    public void commit_liveDataModifiedAfterwards_savedStateUnchanged() {
        versionedAddressBook.addPerson(BENSON);
        versionedAddressBook.commit();
        versionedAddressBook.addPerson(CARL);
        versionedAddressBook.commit();
        versionedAddressBook.undo();

        versionedAddressBook.removePerson(ALICE);
        versionedAddressBook.undo();
        versionedAddressBook.redo();

        assertEquals(List.of(ALICE, BENSON), versionedAddressBook.getPersonList());
    }

    @Test
    public void undo_noEarlierState_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class, versionedAddressBook::undo);
        assertEquals(List.of(ALICE), versionedAddressBook.getPersonList());
    }

    @Test
    public void redo_noLaterState_throwsIllegalStateException() {
        versionedAddressBook.addPerson(BENSON);
        versionedAddressBook.commit();

        assertThrows(IllegalStateException.class, versionedAddressBook::redo);
        assertEquals(List.of(ALICE, BENSON), versionedAddressBook.getPersonList());
    }

    @Test
    public void equals_sameDataDifferentHistory_returnsTrue() {
        versionedAddressBook.addPerson(BENSON);
        versionedAddressBook.commit();
        AddressBook sameData = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();

        assertEquals(new VersionedAddressBook(sameData), versionedAddressBook);
    }
}
