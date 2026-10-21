package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {

    private final CommandHistory history = new CommandHistory();

    @Test
    public void previous_noCommands_returnsEmpty() {
        assertEquals(Optional.empty(), history.previous());
    }

    @Test
    public void next_noCommands_returnsEmpty() {
        assertEquals(Optional.empty(), history.next());
    }

    @Test
    public void next_notBrowsing_returnsEmpty() {
        history.add("list");

        assertEquals(Optional.empty(), history.next());
    }

    @Test
    public void previous_multipleCommands_returnsNewestFirst() {
        history.add("list");
        history.add("delete 1");

        assertEquals(Optional.of("delete 1"), history.previous());
        assertEquals(Optional.of("list"), history.previous());
    }

    @Test
    public void previous_atOldestCommand_staysOnOldest() {
        history.add("list");
        history.add("delete 1");
        history.previous();
        history.previous();

        assertEquals(Optional.of("list"), history.previous());
    }

    @Test
    public void next_afterPrevious_movesForward() {
        history.add("list");
        history.add("delete 1");
        history.previous();
        history.previous();

        assertEquals(Optional.of("delete 1"), history.next());
    }

    @Test
    public void next_fromNewestCommand_returnsEmptyString() {
        history.add("list");
        history.previous();

        assertEquals(Optional.of(""), history.next());
    }

    @Test
    public void next_alreadyPastNewestCommand_returnsEmpty() {
        history.add("list");
        history.previous();
        history.next();

        assertEquals(Optional.empty(), history.next());
    }

    @Test
    public void previous_afterNextPastNewest_returnsNewest() {
        history.add("list");
        history.add("delete 1");
        history.previous();
        history.next();

        assertEquals(Optional.of("delete 1"), history.previous());
    }

    @Test
    public void add_consecutiveIdenticalCommands_storedOnce() {
        history.add("list");
        history.add("list");

        assertEquals(Optional.of("list"), history.previous());
        assertEquals(Optional.of("list"), history.previous());
        assertEquals(Optional.of(""), history.next());
    }

    @Test
    public void add_nonConsecutiveIdenticalCommands_storedEachTime() {
        history.add("list");
        history.add("delete 1");
        history.add("list");

        assertEquals(Optional.of("list"), history.previous());
        assertEquals(Optional.of("delete 1"), history.previous());
        assertEquals(Optional.of("list"), history.previous());
    }

    @Test
    public void add_whileBrowsing_resetsToNewest() {
        history.add("list");
        history.add("delete 1");
        history.previous();
        history.previous();

        history.add("view 2");

        assertEquals(Optional.of("view 2"), history.previous());
    }

    @Test
    public void add_blankCommand_notStored() {
        history.add("list");
        history.add("   ");

        assertEquals(Optional.of("list"), history.previous());
        assertEquals(Optional.of("list"), history.previous());
    }

    @Test
    public void add_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> history.add(null));
    }
}
