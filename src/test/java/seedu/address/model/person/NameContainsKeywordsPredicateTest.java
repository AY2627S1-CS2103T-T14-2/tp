package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class NameContainsKeywordsPredicateTest {

    @Test
    public void equals() {
        List<String> firstPredicateKeywordList = List.of("first");
        List<String> secondPredicateKeywordList = List.of("first", "second");

        NameContainsKeywordsPredicate firstPredicate = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        NameContainsKeywordsPredicate secondPredicate = new NameContainsKeywordsPredicate(secondPredicateKeywordList);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        NameContainsKeywordsPredicate firstPredicateCopy = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        assertTrue(firstPredicate.equals(firstPredicateCopy));

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different person -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void test_nameContainsKeywords_returnsTrue() {
        // One keyword
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Alice"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Multiple keywords
        predicate = new NameContainsKeywordsPredicate(List.of("Alice", "Bob"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Only one matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Bob", "Carol"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Carol").build()));

        // Mixed-case keywords
        predicate = new NameContainsKeywordsPredicate(List.of("aLIce", "bOB"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));
    }

    @Test
    public void test_partialKeyword_returnsTrue() {
        Person person = new PersonBuilder().withName("Weiling Koh").build();

        // Beginning, middle and end of a word
        assertTrue(new NameContainsKeywordsPredicate(List.of("wei")).test(person));
        assertTrue(new NameContainsKeywordsPredicate(List.of("eili")).test(person));
        assertTrue(new NameContainsKeywordsPredicate(List.of("ing")).test(person));

        // Partial matching ignores case and uses OR across keywords
        assertTrue(new NameContainsKeywordsPredicate(List.of("TAN", "WEI")).test(person));
        assertTrue(new NameContainsKeywordsPredicate(List.of("kOH")).test(person));
    }

    @Test
    public void test_repeatedKeyword_sameResult() {
        Person person = new PersonBuilder().withName("Tan Wei Ming").build();
        NameContainsKeywordsPredicate singleKeyword = new NameContainsKeywordsPredicate(List.of("tan"));
        NameContainsKeywordsPredicate repeatedKeyword = new NameContainsKeywordsPredicate(List.of("tan", "tan"));

        assertEquals(singleKeyword.test(person), repeatedKeyword.test(person));
    }

    @Test
    public void test_differentApostropheStyles_returnsTrue() {
        Person straightPerson = new PersonBuilder().withName("Charlotte O'Brien").build();
        Person curlyPerson = new PersonBuilder().withName("Eleanor O’Brien").build();

        for (String keyword : List.of("O'Brien", "O’Brien", "O’BRIEN")) {
            NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of(keyword));

            assertTrue(predicate.test(straightPerson));
            assertTrue(predicate.test(curlyPerson));
        }
    }

    @Test
    public void test_canonicallyEquivalentKeywords_returnsTrue() {
        Person person = new PersonBuilder().withName("José Tan").build();

        assertTrue(new NameContainsKeywordsPredicate(List.of("JOSE\u0301")).test(person));
        assertFalse(new NameContainsKeywordsPredicate(List.of("Jose")).test(person));
    }

    @Test
    public void test_keywordAcrossSpace_returnsFalse() {
        Person person = new PersonBuilder().withName("Tan Wei Ming").build();

        assertFalse(new NameContainsKeywordsPredicate(List.of("weiming")).test(person));
        assertTrue(new NameContainsKeywordsPredicate(List.of("wei")).test(person));
    }

    @Test
    public void test_partialKeywordOutsideName_returnsFalse() {
        Person person = new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@example.com").withAddress("Main Street").build();

        assertFalse(new NameContainsKeywordsPredicate(List.of("234", "example", "treet")).test(person));
        assertFalse(new NameContainsKeywordsPredicate(List.of("@")).test(person));
    }

    @Test
    public void test_nameDoesNotContainKeywords_returnsFalse() {
        // Zero keywords
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of());
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").build()));

        // Non-matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Carol"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Keywords match phone, email and address, but do not match name
        predicate = new NameContainsKeywordsPredicate(List.of("12345", "alice@email.com", "Main", "Street"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@email.com").withAddress("Main Street").build()));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(keywords);

        String expected = NameContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}
