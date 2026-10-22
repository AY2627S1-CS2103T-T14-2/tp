package seedu.address.model;

import java.util.ArrayList;
import java.util.List;

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

    /** Returns true if the current data differs from the most recently saved state. */
    public boolean hasChangesSinceLastCommit() {
        return !equals(addressBookStateList.get(currentStatePointer));
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
