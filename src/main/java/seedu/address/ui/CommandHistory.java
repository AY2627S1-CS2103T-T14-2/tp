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

    /** Text that was in the command box when browsing started, shown again when moving past the newest command. */
    private String draft = "";

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
        assert isCursorInRange();
    }

    /**
     * Moves back to the previous command and returns it.
     * Stays on the oldest command once it is reached, and returns an empty {@code Optional} if no commands
     * have been entered. If browsing starts here, {@code currentText} is kept as the draft for {@link #next()}.
     */
    public Optional<String> previous(String currentText) {
        requireNonNull(currentText);
        if (commands.isEmpty()) {
            return Optional.empty();
        }
        if (cursor == commands.size()) {
            draft = currentText;
        }
        cursor = Math.max(0, cursor - 1);
        assert isCursorInRange();
        return Optional.of(commands.get(cursor));
    }

    /**
     * Moves forward to the next command and returns it.
     * Returns the draft kept by {@link #previous(String)} when moving past the newest command, or an empty
     * {@code Optional} if already past it, so text typed without browsing the history is kept.
     */
    public Optional<String> next() {
        if (cursor >= commands.size()) {
            return Optional.empty();
        }
        cursor++;
        assert isCursorInRange();
        return Optional.of(cursor == commands.size() ? draft : commands.get(cursor));
    }

    /** Returns true if the cursor is on a stored command or just past the newest one. */
    private boolean isCursorInRange() {
        return cursor >= 0 && cursor <= commands.size();
    }
}
