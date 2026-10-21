package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final VersionedAddressBook versionedAddressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final SortedList<Person> sortedPersons;
    private Person viewedPerson = null;
    private boolean hasUncommittedChanges = false;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.versionedAddressBook = new VersionedAddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.versionedAddressBook.getPersonList());
        sortedPersons = new SortedList<>(filteredPersons);
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        versionedAddressBook.resetData(addressBook);
        hasUncommittedChanges = true;
        viewedPerson = null;
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return versionedAddressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return versionedAddressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        versionedAddressBook.removePerson(target);
        hasUncommittedChanges = true;
        if (target.equals(viewedPerson)) {
            viewedPerson = null;
        }
    }

    @Override
    public void addPerson(Person person) {
        versionedAddressBook.addPerson(person);
        hasUncommittedChanges = true;
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        versionedAddressBook.setPerson(target, editedPerson);
        hasUncommittedChanges = true;
        if (target.equals(viewedPerson)) {
            viewedPerson = editedPerson;
        }
    }

    @Override
    public List<Person> getPersonsIntroducedBy(Name introducer) {
        requireNonNull(introducer);
        return versionedAddressBook.getPersonList().stream()
                .filter(person -> person.isIntroducedBy(introducer))
                .toList();
    }

    @Override
    public int replaceIntroducer(Name introducer, Optional<Name> replacement) {
        requireAllNonNull(introducer, replacement);
        List<Person> introducedPersons = getPersonsIntroducedBy(introducer);
        for (Person person : introducedPersons) {
            // goes through setPerson so that a viewed supporter is kept up to date
            setPerson(person, person.withIntroducer(replacement));
        }
        logger.fine("Replaced introducer " + introducer + " for " + introducedPersons.size() + " supporter(s)");
        return introducedPersons.size();
    }

    @Override
    public void commitAddressBookIfChanged() {
        if (!hasUncommittedChanges) {
            return;
        }
        versionedAddressBook.commit();
        hasUncommittedChanges = false;
    }

    @Override
    public boolean canUndoAddressBook() {
        return versionedAddressBook.canUndo();
    }

    @Override
    public boolean canRedoAddressBook() {
        return versionedAddressBook.canRedo();
    }

    @Override
    public void undoAddressBook() {
        versionedAddressBook.undo();
        refreshViewedPerson();
    }

    @Override
    public void redoAddressBook() {
        versionedAddressBook.redo();
        refreshViewedPerson();
    }

    /**
     * Replaces the viewed supporter with their record in the restored data, or clears the selection if they
     * are no longer in it.
     */
    private void refreshViewedPerson() {
        if (viewedPerson == null) {
            return;
        }
        Person previouslyViewed = viewedPerson;
        viewedPerson = versionedAddressBook.getPersonList().stream()
                .filter(previouslyViewed::isSamePerson)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Optional<Person> getViewedPerson() {
        return Optional.ofNullable(viewedPerson);
    }

    @Override
    public void setViewedPerson(Person person) {
        requireNonNull(person);
        viewedPerson = person;
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code versionedAddressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return sortedPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        sortedPersons.setComparator(null);
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public void updateSortedPersonList(Predicate<Person> predicate, Comparator<Person> comparator) {
        requireAllNonNull(predicate, comparator);
        filteredPersons.setPredicate(predicate);
        sortedPersons.setComparator(comparator);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return versionedAddressBook.equals(otherModelManager.versionedAddressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons)
                && sortedPersons.equals(otherModelManager.sortedPersons)
                && Objects.equals(viewedPerson, otherModelManager.viewedPerson);
    }

}
