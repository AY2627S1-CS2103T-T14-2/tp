package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Stage;

/**
 * Lists non-Declined supporters with no interactions or more than the specified days since last contact.
 * Never-contacted supporters appear first, followed by the oldest latest-contact date.
 */
public class QuietCommand extends Command {
    public static final String COMMAND_WORD = "quiet";
    public static final int DEFAULT_DAYS = 60;
    public static final String MESSAGE_CONSTRAINTS = "DAYS must be a whole number from 1 to 2147483647";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists supporters needing follow-up, longest silence first. "
            + "Includes never-contacted supporters and excludes Declined supporters.\n"
            + "Parameters: [DAYS] (default: " + DEFAULT_DAYS + "). " + MESSAGE_CONSTRAINTS + ".\n"
            + "A supporter last contacted exactly DAYS days ago is not included.\n"
            + "Example: " + COMMAND_WORD + " 30";
    public static final String MESSAGE_SUCCESS =
            "%1$d supporter(s) listed: no interactions or last contacted more than %2$d days ago.";

    private static final Logger logger = LogsCenter.getLogger(QuietCommand.class);
    private static final Comparator<Person> LAST_CONTACT_ORDER =
            Comparator.comparing(person -> person.getLastInteractionDate().orElse(LocalDate.MIN));

    private final int days;
    private final Clock clock;

    /**
     * Creates a quiet command with the default threshold and the computer's clock.
     */
    public QuietCommand() {
        this(DEFAULT_DAYS);
    }

    /**
     * Creates a quiet command with a positive day threshold and the computer's clock.
     */
    public QuietCommand(int days) {
        this(days, Clock.systemDefaultZone());
    }

    /**
     * Creates a quiet command using {@code clock} to determine today's date at execution.
     */
    QuietCommand(int days, Clock clock) {
        checkArgument(days > 0, MESSAGE_CONSTRAINTS);
        this.days = days;
        this.clock = requireNonNull(clock);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDate today = LocalDate.now(clock);
        // Read current full-list positions so replacing a supporter cannot move them within a tied group.
        Comparator<Person> quietOrder = LAST_CONTACT_ORDER
                .thenComparingInt(model.getAddressBook().getPersonList()::indexOf);
        Predicate<Person> quietPredicate = person -> person.getStage() != Stage.DECLINED
                && person.getLastInteractionDate()
                        .map(date -> ChronoUnit.DAYS.between(date, today) > days).orElse(true);
        model.updateSortedPersonList(quietPredicate, quietOrder);
        List<Person> quietSupporters = model.getFilteredPersonList();
        assert quietSupporters.stream().allMatch(quietPredicate) : "Every quiet result must meet the threshold";
        assert IntStream.range(1, quietSupporters.size())
                .allMatch(index -> quietOrder.compare(quietSupporters.get(index - 1), quietSupporters.get(index)) <= 0)
                : "Quiet results must retain last-contact and full-list tie ordering";
        logger.fine(() -> "Applied quiet filter: days=" + days + ", date=" + today
                + ", matches=" + quietSupporters.size());
        return new CommandResult(String.format(MESSAGE_SUCCESS, quietSupporters.size(), days));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof QuietCommand otherCommand && days == otherCommand.days;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(days);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("days", days).toString();
    }
}
