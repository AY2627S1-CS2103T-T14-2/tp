package seedu.address.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import seedu.address.model.person.Person;

/**
 * Wraps all data at the address-book level, with a history of saved states for undo and redo.
 */
public class VersionedAddressBook extends AddressBook {
    private final List<ReadOnlyAddressBook> addressBookStateList = new ArrayList<>();
    private int currentStatePointer;

    /**
     * Creates a VersionedAddressBook whose history starts with a copy of {@code initialState}.
     */
    public VersionedAddressBook(ReadOnlyAddressBook initialState) {
        super(initialState);
        addressBookStateList.add(new AddressBook(initialState));
        currentStatePointer = 0;
    }

    /**
     * Saves a copy of the current address book state, discarding any undone states.
     */
    public void commit() {
        addressBookStateList.subList(currentStatePointer + 1, addressBookStateList.size()).clear();
        addressBookStateList.add(new AddressBook(this));
        currentStatePointer++;
    }

    /** Returns true if the current data differs from the state at {@code currentStatePointer}. */
    public boolean hasChangesSinceLastCommit() {
        return !equals(addressBookStateList.get(currentStatePointer));
    }

    /**
     * Returns an identity-based set of record instances retained by the current data or undo/redo history.
     * This allows session-only selection metadata for discarded record versions to be released.
     */
    Set<Person> getRetainedPersonVersions() {
        Set<Person> retainedPersons = Collections.newSetFromMap(new IdentityHashMap<>());
        retainedPersons.addAll(getPersonList());
        for (ReadOnlyAddressBook state : addressBookStateList) {
            retainedPersons.addAll(state.getPersonList());
        }
        return retainedPersons;
    }

    /** Returns true if there is an earlier state to restore. */
    public boolean canUndo() {
        return currentStatePointer > 0;
    }

    /** Returns true if there is a later state to restore. */
    public boolean canRedo() {
        return currentStatePointer < addressBookStateList.size() - 1;
    }

    /**
     * Restores the previous address book state.
     *
     * @throws IllegalStateException if there is no earlier state.
     */
    public void undo() {
        if (!canUndo()) {
            throw new IllegalStateException("There is no earlier state to undo to.");
        }
        currentStatePointer--;
        resetData(addressBookStateList.get(currentStatePointer));
    }

    /**
     * Restores the next undone address book state.
     *
     * @throws IllegalStateException if there is no later state.
     */
    public void redo() {
        if (!canRedo()) {
            throw new IllegalStateException("There is no later state to redo to.");
        }
        currentStatePointer++;
        resetData(addressBookStateList.get(currentStatePointer));
    }
}
