package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Wraps all data at the address-book level, with a history of saved states for undo and redo.
 */
public class VersionedAddressBook extends AddressBook {
    private static final Logger logger = LogsCenter.getLogger(VersionedAddressBook.class);

    private final List<ReadOnlyAddressBook> addressBookStateList = new ArrayList<>();
    /** Describes each saved change: entry {@code i} is the change from state {@code i} to state {@code i + 1}. */
    private final List<String> changeDescriptionList = new ArrayList<>();
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
     * Saves a copy of the current address book state with no description of the change,
     * discarding any undone states.
     */
    public void commit() {
        commit("");
    }

    /**
     * Saves a copy of the current address book state, discarding any undone states.
     * {@code changeDescription}, e.g. the command that made the change, is returned when the change is undone
     * or redone.
     */
    public void commit(String changeDescription) {
        requireNonNull(changeDescription);
        addressBookStateList.subList(currentStatePointer + 1, addressBookStateList.size()).clear();
        changeDescriptionList.subList(currentStatePointer, changeDescriptionList.size()).clear();
        addressBookStateList.add(new AddressBook(this));
        changeDescriptionList.add(changeDescription);
        currentStatePointer++;
        assert !canRedo() : "a new state is always the latest one";
        assert changeDescriptionList.size() == addressBookStateList.size() - 1 : "one description per change";
        logger.fine("Saved undo point " + describeHistoryPosition());
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
     * @return the description of the undone change, which is empty if it was saved without one.
     * @throws IllegalStateException if there is no earlier state.
     */
    public String undo() {
        if (!canUndo()) {
            throw new IllegalStateException("There is no earlier state to undo to.");
        }
        currentStatePointer--;
        resetData(addressBookStateList.get(currentStatePointer));
        assert canRedo() : "an undone state can always be redone";
        logger.fine("Undid to " + describeHistoryPosition());
        return changeDescriptionList.get(currentStatePointer);
    }

    /**
     * Restores the next undone address book state.
     *
     * @return the description of the redone change, which is empty if it was saved without one.
     * @throws IllegalStateException if there is no later state.
     */
    public String redo() {
        if (!canRedo()) {
            throw new IllegalStateException("There is no later state to redo to.");
        }
        currentStatePointer++;
        resetData(addressBookStateList.get(currentStatePointer));
        assert canUndo() : "a redone state can always be undone";
        logger.fine("Redid to " + describeHistoryPosition());
        return changeDescriptionList.get(currentStatePointer - 1);
    }

    /**
     * Returns the current position in the history for log messages, e.g. {@code state 2 of 3}.
     */
    private String describeHistoryPosition() {
        return "state " + (currentStatePointer + 1) + " of " + addressBookStateList.size();
    }
}
