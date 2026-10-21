package seedu.address.model;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /**
     * Returns every supporter whose recorded introducer is the supporter called {@code introducer},
     * in the order they appear in the address book. Names are compared ignoring case and extra spaces.
     */
    List<Person> getPersonsIntroducedBy(Name introducer);

    /**
     * Replaces the introducer of every supporter introduced by {@code introducer} with {@code replacement},
     * or removes it if {@code replacement} is empty, and returns how many supporters were changed.
     * Used to keep introductions correct when a supporter is renamed or deleted.
     */
    int replaceIntroducer(Name introducer, Optional<Name> replacement);

    /**
     * Saves the current address book state to the undo/redo history if supporter data has changed since the
     * last saved state. Any undone states are then discarded, so they can no longer be redone.
     */
    void commitAddressBookIfChanged();

    /**
     * Returns true if there is an earlier address book state to restore.
     */
    boolean canUndoAddressBook();

    /**
     * Returns true if there is a later address book state to restore.
     */
    boolean canRedoAddressBook();

    /**
     * Restores the previous address book state from the undo/redo history.
     *
     * @throws IllegalStateException if there is no earlier state.
     */
    void undoAddressBook();

    /**
     * Restores the next undone address book state from the undo/redo history.
     *
     * @throws IllegalStateException if there is no later state.
     */
    void redoAddressBook();

    /**
     * Returns the supporter being viewed, or an empty {@code Optional} if none has been selected.
     */
    Optional<Person> getViewedPerson();

    /**
     * Records {@code person} as the supporter being viewed without changing the displayed list or its filter.
     *
     * @throws NullPointerException if {@code person} is null.
     */
    void setViewedPerson(Person person);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);
}
