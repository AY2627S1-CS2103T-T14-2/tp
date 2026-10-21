package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonMatchesSearchPredicateTest {
    private final Person supporter = new PersonBuilder().withName("Tan Wei Ming").withOrganisation("DBS Bank")
            .withTags("boardIntro", "donor").withStage("giving").withPhone("6591234567")
            .withEmail("weiming@dbs.com").build();

    @Test
    public void test_nameKeywords_matchesAnyCaseInsensitiveSubstring() {
        assertTrue(new PersonMatchesSearchPredicate(List.of("missing", "WEI")).test(supporter));
        assertTrue(new PersonMatchesSearchPredicate(List.of("ming", "tan", "tan")).test(supporter));
        assertFalse(new PersonMatchesSearchPredicate(List.of("weiming")).test(supporter));
        assertFalse(new PersonMatchesSearchPredicate(List.of("DBS", "9123")).test(supporter));
    }

    @Test
    public void test_organisationKeywords_matchesAnyCaseInsensitiveSubstring() {
        PersonMatchesSearchPredicate predicate = new PersonMatchesSearchPredicate(List.of(),
                List.of("OCBC", "dbs bank"), List.of(), List.of(), List.of(), List.of());

        assertTrue(predicate.test(supporter));
        assertFalse(predicate.test(new PersonBuilder(supporter).withOrganisation("Other Bank").build()));
        assertFalse(predicate.test(new PersonBuilder(supporter).withoutOrganisation().build()));
    }

    @Test
    public void test_tagKeywords_matchesAnyTagSubstring() {
        PersonMatchesSearchPredicate predicate = new PersonMatchesSearchPredicate(List.of(), List.of(),
                List.of("missing", "BOARD"), List.of(), List.of(), List.of());

        assertTrue(predicate.test(supporter));
        assertFalse(predicate.test(new PersonBuilder(supporter).withTags("volunteer").build()));
        assertFalse(predicate.test(new PersonBuilder(supporter).withTags().build()));
    }

    @Test
    public void test_stages_matchesAnyExactStage() {
        PersonMatchesSearchPredicate predicate = new PersonMatchesSearchPredicate(List.of(), List.of(), List.of(),
                List.of(Stage.GIVING, Stage.LAPSED), List.of(), List.of());

        assertTrue(predicate.test(supporter));
        assertTrue(predicate.test(new PersonBuilder(supporter).withStage("lapsed").build()));
        assertFalse(predicate.test(new PersonBuilder(supporter).withStage("prospect").build()));
    }

    @Test
    public void test_phoneKeywords_ignoresFormattingAndMatchesAnySubstring() {
        PersonMatchesSearchPredicate predicate = new PersonMatchesSearchPredicate(List.of(), List.of(), List.of(),
                List.of(), List.of("000", "+65 9123-4"), List.of());

        assertTrue(predicate.test(supporter));
        assertFalse(predicate.test(new PersonBuilder(supporter).withPhone("81112222").build()));
        assertFalse(predicate.test(new PersonBuilder(supporter).withoutPhone().build()));
    }

    @Test
    public void test_emailKeywords_matchesAnyCaseInsensitiveSubstring() {
        PersonMatchesSearchPredicate predicate = new PersonMatchesSearchPredicate(List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of("@gmail", "@DBS"));

        assertTrue(predicate.test(supporter));
        assertFalse(predicate.test(new PersonBuilder(supporter).withEmail("someone@example.com").build()));
        assertFalse(predicate.test(new PersonBuilder(supporter).withoutEmail().build()));
    }

    @Test
    public void test_multipleFields_requiresEverySpecifiedGroup() {
        PersonMatchesSearchPredicate predicate = new PersonMatchesSearchPredicate(List.of("tan", "lim"),
                List.of("DBS", "OCBC"), List.of("board", "vip"), List.of(Stage.GIVING, Stage.LAPSED),
                List.of("9123", "8111"), List.of("@dbs", "@ocbc"));

        assertTrue(predicate.test(supporter));
        assertFalse(predicate.test(new PersonBuilder(supporter).withName("Alice").build()));
        assertFalse(predicate.test(new PersonBuilder(supporter).withOrganisation("Other Bank").build()));
        assertFalse(predicate.test(new PersonBuilder(supporter).withTags("volunteer").build()));
        assertFalse(predicate.test(new PersonBuilder(supporter).withStage("declined").build()));
        assertFalse(predicate.test(new PersonBuilder(supporter).withPhone("99999999").build()));
        assertFalse(predicate.test(new PersonBuilder(supporter).withEmail("alice@example.com").build()));
    }

    @Test
    public void test_noCriteria_returnsFalse() {
        assertFalse(new PersonMatchesSearchPredicate(List.of()).test(supporter));
    }

    @Test
    public void constructor_mutableCriteria_defensivelyCopiesLists() {
        List<String> organisations = new ArrayList<>(List.of("DBS"));
        List<Stage> stages = new ArrayList<>(List.of(Stage.GIVING));
        PersonMatchesSearchPredicate predicate = new PersonMatchesSearchPredicate(List.of(), organisations,
                List.of(), stages, List.of(), List.of());
        organisations.clear();
        stages.clear();

        assertTrue(predicate.test(supporter));
        assertFalse(predicate.test(new PersonBuilder(supporter).withOrganisation("Other Bank").build()));
        assertFalse(predicate.test(new PersonBuilder(supporter).withStage("prospect").build()));
    }

    @Test
    public void normalizePhone_formattedNumbers_removesOnlyPhoneFormatting() {
        assertEquals("6591234567", PersonMatchesSearchPredicate.normalizePhone("+65 9123-4567"));
        assertEquals("6591234567", PersonMatchesSearchPredicate.normalizePhone("+65-9123 4567"));
        assertEquals("9123", PersonMatchesSearchPredicate.normalizePhone("9123"));
        assertEquals("", PersonMatchesSearchPredicate.normalizePhone("+ -"));
        assertEquals("65abc", PersonMatchesSearchPredicate.normalizePhone("+65 abc"));
    }

    @Test
    public void test_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PersonMatchesSearchPredicate(List.of("tan")).test(null));
    }

    @Test
    public void equals() {
        PersonMatchesSearchPredicate predicate = new PersonMatchesSearchPredicate(List.of("tan"));
        PersonMatchesSearchPredicate copy = new PersonMatchesSearchPredicate(List.of("TAN"));

        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(copy));
        assertEquals(predicate.hashCode(), copy.hashCode());
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals("tan"));
        assertFalse(predicate.equals(new PersonMatchesSearchPredicate(List.of("lim"))));
        assertFalse(predicate.equals(new PersonMatchesSearchPredicate(List.of(), List.of("tan"),
                List.of(), List.of(), List.of(), List.of())));
    }
}
