package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Stage;

/**
 * Shows how many supporters are at each cultivation stage.
 * Every supporter is counted, not just those in the displayed list, and the displayed list does not change.
 */
public class StatsCommand extends Command {

    public static final String COMMAND_WORD = "stats";

    public static final String MESSAGE_HEADER = "Supporters by stage (%1$d in total):";
    public static final String MESSAGE_STAGE_COUNT = "%1$s: %2$d";
    public static final String STAGE_COUNT_SEPARATOR = ", ";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        List<Person> allPersons = model.getAddressBook().getPersonList();
        Map<Stage, Integer> counts = countByStage(allPersons);

        // all counts share one line so that the result fits the result display without scrolling
        String stageCounts = Stream.of(Stage.values())
                .map(stage -> String.format(MESSAGE_STAGE_COUNT, stage, counts.get(stage)))
                .collect(Collectors.joining(STAGE_COUNT_SEPARATOR));
        return new CommandResult(String.format(MESSAGE_HEADER, allPersons.size()) + "\n" + stageCounts);
    }

    /**
     * Returns the number of {@code persons} at each stage, with every stage present, including those with none.
     */
    private static Map<Stage, Integer> countByStage(List<Person> persons) {
        Map<Stage, Integer> counts = new EnumMap<>(Stage.class);
        for (Stage stage : Stage.values()) {
            counts.put(stage, 0);
        }
        for (Person person : persons) {
            counts.merge(person.getStage(), 1, Integer::sum);
        }

        assert counts.values().stream().mapToInt(Integer::intValue).sum() == persons.size()
                : "every supporter should be counted at exactly one stage";
        return counts;
    }
}
