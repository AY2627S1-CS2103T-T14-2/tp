package seedu.address.testutil;

import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.LocalDate;
import java.util.List;

import seedu.address.model.person.Interaction;
import seedu.address.model.person.Person;

/**
 * A supporter with one interaction and a supplied display order for testing inconsistent mappings.
 */
public final class PersonWithDisplayOrderStub extends Person {

    private final List<Integer> displayOrderIndices;

    /**
     * Creates a supporter whose display-order indices may deliberately violate the history contract.
     */
    public PersonWithDisplayOrderStub(List<Integer> displayOrderIndices) {
        super(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                ALICE.getOrganisation(), ALICE.getIntroducer(), ALICE.getStage(),
                List.of(new Interaction(LocalDate.of(2024, 2, 29), "Called")), ALICE.getTags());
        this.displayOrderIndices = displayOrderIndices;
    }

    @Override
    public List<Integer> getInteractionIndicesInDisplayOrder() {
        return displayOrderIndices;
    }
}
