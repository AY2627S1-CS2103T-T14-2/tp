package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getViewedPerson_noSelection_returnsEmptyOptional() {
        assertEquals(Optional.empty(), modelManager.getViewedPerson());

        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).build();
        ModelManager populatedModelManager = new ModelManager(addressBook, new UserPrefs());
        assertEquals(Optional.empty(), populatedModelManager.getViewedPerson());
    }

    @Test
    public void setViewedPerson_validPerson_setsViewedPerson() {
        modelManager.addPerson(ALICE);

        modelManager.setViewedPerson(ALICE);

        assertEquals(Optional.of(ALICE), modelManager.getViewedPerson());
    }

    @Test
    public void setViewedPerson_anotherPerson_switchesViewedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.setViewedPerson(ALICE);

        modelManager.setViewedPerson(BENSON);

        assertEquals(Optional.of(BENSON), modelManager.getViewedPerson());
    }

    @Test
    public void setViewedPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setViewedPerson(null));
        assertEquals(Optional.empty(), modelManager.getViewedPerson());

        modelManager.addPerson(ALICE);
        modelManager.setViewedPerson(ALICE);

        assertThrows(NullPointerException.class, () -> modelManager.setViewedPerson(null));
        assertEquals(Optional.of(ALICE), modelManager.getViewedPerson());
    }

    @Test
    public void setViewedPerson_filteredList_preservesAddressBookAndDisplayedList() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        modelManager = new ModelManager(addressBook, new UserPrefs());
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());

        modelManager.setViewedPerson(BENSON);

        assertEquals(Optional.of(BENSON), modelManager.getViewedPerson());
        assertEquals(addressBook, modelManager.getAddressBook());
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
    }

    @Test
    public void updateFilteredPersonList_viewedPersonExcluded_retainsViewedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.setViewedPerson(ALICE);

        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Benson")));

        assertEquals(List.of(BENSON), modelManager.getFilteredPersonList());
        assertEquals(Optional.of(ALICE), modelManager.getViewedPerson());

        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        assertEquals(List.of(ALICE, BENSON), modelManager.getFilteredPersonList());
        assertEquals(Optional.of(ALICE), modelManager.getViewedPerson());
    }

    @Test
    public void setPerson_viewedPersonExcludedFromFilter_updatesViewedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.setViewedPerson(ALICE);
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Benson")));
        Person editedAlice = new PersonBuilder(ALICE).withName("Alice Tan").build();

        modelManager.setPerson(new PersonBuilder(ALICE).build(), editedAlice);

        assertEquals(Optional.of(editedAlice), modelManager.getViewedPerson());
        assertEquals(List.of(BENSON), modelManager.getFilteredPersonList());
        assertEquals(List.of(editedAlice, BENSON), modelManager.getAddressBook().getPersonList());
    }

    @Test
    public void setPerson_anotherPerson_retainsViewedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.setViewedPerson(ALICE);
        Person editedBenson = new PersonBuilder(BENSON).withPhone("91234567").build();

        modelManager.setPerson(BENSON, editedBenson);

        assertEquals(Optional.of(ALICE), modelManager.getViewedPerson());
        assertEquals(List.of(ALICE, editedBenson), modelManager.getAddressBook().getPersonList());
    }

    @Test
    public void setPerson_duplicatePerson_preservesViewedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.setViewedPerson(ALICE);

        assertThrows(DuplicatePersonException.class, () -> modelManager.setPerson(ALICE, BENSON));

        assertEquals(Optional.of(ALICE), modelManager.getViewedPerson());
        assertEquals(List.of(ALICE, BENSON), modelManager.getAddressBook().getPersonList());
    }

    @Test
    public void deletePerson_viewedPerson_clearsViewedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.setViewedPerson(ALICE);

        modelManager.deletePerson(new PersonBuilder(ALICE).build());

        assertEquals(Optional.empty(), modelManager.getViewedPerson());
        assertEquals(List.of(BENSON), modelManager.getAddressBook().getPersonList());
    }

    @Test
    public void deletePerson_anotherPerson_retainsViewedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.setViewedPerson(ALICE);

        modelManager.deletePerson(BENSON);

        assertEquals(Optional.of(ALICE), modelManager.getViewedPerson());
        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
    }

    @Test
    public void deletePerson_missingPerson_preservesViewedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.setViewedPerson(ALICE);

        assertThrows(PersonNotFoundException.class, () -> modelManager.deletePerson(BENSON));

        assertEquals(Optional.of(ALICE), modelManager.getViewedPerson());
        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
    }

    @Test
    public void setAddressBook_replacementContainingViewedPerson_clearsViewedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.setViewedPerson(ALICE);
        AddressBook replacement = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();

        modelManager.setAddressBook(replacement);

        assertEquals(Optional.empty(), modelManager.getViewedPerson());
        assertEquals(replacement, modelManager.getAddressBook());
    }

    @Test
    public void setAddressBook_emptyAddressBook_clearsViewedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.setViewedPerson(ALICE);

        modelManager.setAddressBook(new AddressBook());

        assertEquals(Optional.empty(), modelManager.getViewedPerson());
        assertTrue(modelManager.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void setAddressBook_nullAddressBook_preservesViewedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.setViewedPerson(ALICE);

        assertThrows(NullPointerException.class, () -> modelManager.setAddressBook(null));

        assertEquals(Optional.of(ALICE), modelManager.getViewedPerson());
        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
    }

    @Test
    public void executeList_viewedPersonExcludedFromFilter_retainsViewedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.setViewedPerson(ALICE);
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Benson")));

        new ListCommand().execute(modelManager);

        assertEquals(Optional.of(ALICE), modelManager.getViewedPerson());
        assertEquals(List.of(ALICE, BENSON), modelManager.getFilteredPersonList());
    }

    @Test
    public void executeFind_viewedPersonExcludedFromResults_retainsViewedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.setViewedPerson(ALICE);

        new FindCommand(new NameContainsKeywordsPredicate(List.of("Benson"))).execute(modelManager);

        assertEquals(Optional.of(ALICE), modelManager.getViewedPerson());
        assertEquals(List.of(BENSON), modelManager.getFilteredPersonList());
    }

    @Test
    public void executeAdd_anotherPerson_retainsViewedPerson() throws Exception {
        modelManager.addPerson(ALICE);
        modelManager.setViewedPerson(ALICE);
        modelManager.updateFilteredPersonList(unused -> false);

        new AddCommand(BENSON).execute(modelManager);

        assertEquals(Optional.of(ALICE), modelManager.getViewedPerson());
        assertEquals(List.of(ALICE, BENSON), modelManager.getFilteredPersonList());
    }

    @Test
    public void canUndoAddressBook_noCommits_returnsFalse() {
        assertFalse(modelManager.canUndoAddressBook());
        assertFalse(modelManager.canRedoAddressBook());
    }

    @Test
    public void commitAddressBookIfChanged_noChange_savesNoUndoPoint() {
        modelManager.setViewedPerson(ALICE);
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        modelManager.commitAddressBookIfChanged();

        assertFalse(modelManager.canUndoAddressBook());
    }

    @Test
    public void commitAddressBookIfChanged_calledTwiceAfterOneChange_savesOneUndoPoint() {
        modelManager.addPerson(ALICE);

        modelManager.commitAddressBookIfChanged();
        modelManager.commitAddressBookIfChanged();
        modelManager.undoAddressBook();

        assertFalse(modelManager.canUndoAddressBook());
    }

    @Test
    public void undoAddressBook_afterCommit_restoresPreviousData() {
        modelManager.addPerson(ALICE);
        modelManager.commitAddressBookIfChanged();

        modelManager.undoAddressBook();

        assertEquals(List.of(), modelManager.getAddressBook().getPersonList());
        assertEquals(List.of(), modelManager.getFilteredPersonList());
        assertFalse(modelManager.canUndoAddressBook());
        assertTrue(modelManager.canRedoAddressBook());
    }

    @Test
    public void redoAddressBook_afterUndo_restoresUndoneData() {
        modelManager.addPerson(ALICE);
        modelManager.commitAddressBookIfChanged();
        modelManager.undoAddressBook();

        modelManager.redoAddressBook();

        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
        assertTrue(modelManager.canUndoAddressBook());
        assertFalse(modelManager.canRedoAddressBook());
    }

    @Test
    public void undoAddressBook_noEarlierState_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> modelManager.undoAddressBook());
    }

    @Test
    public void redoAddressBook_noLaterState_throwsIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> modelManager.redoAddressBook());
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }
}
