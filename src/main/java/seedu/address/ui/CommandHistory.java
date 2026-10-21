package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores the commands entered this session so that they can be brought back with the Up and Down keys.
 */
public class CommandHistory {
    private final List<String> commands = new ArrayList<>();

    /** Index of the command currently shown; equal to the number of commands when past the newest one. */
    private int cursor = 0;

    /**
     * Records {@code command} as the newest command and moves the cursor past it.
     * A blank command, or one identical to the newest command, is not stored again.
     */
    public void add(String command) {
        requireNonNull(command);
        boolean isRepeat = !commands.isEmpty() && commands.get(commands.size() - 1).equals(command);
        if (!command.isBlank() && !isRepeat) {
            commands.add(command);
        }
        cursor = commands.size();
    }

    /**
     * Moves back to the previous command and returns it.
     * Stays on the oldest command once it is reached, and returns an empty {@code Optional} if no commands
     * have been entered.
     */
    public Optional<String> previous() {
        if (commands.isEmpty()) {
            return Optional.empty();
        }
        cursor = Math.max(0, cursor - 1);
        return Optional.of(commands.get(cursor));
    }

    /**
     * Moves forward to the next command and returns it.
     * Returns an empty string when moving past the newest command, or an empty {@code Optional} if already
     * past it, so text typed without browsing the history is kept.
     */
    public Optional<String> next() {
        if (cursor >= commands.size()) {
            return Optional.empty();
        }
        cursor++;
        return Optional.of(cursor == commands.size() ? "" : commands.get(cursor));
    }
}
