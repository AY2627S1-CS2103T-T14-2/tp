package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.function.Executable;

/**
 * Provides utilities for verifying diagnostic log messages without changing later tests' logger settings.
 */
public final class LogTestUtil {

    private LogTestUtil() {
    }

    /**
     * Captures FINE messages from the initialized source class while running the action.
     * Restores the logger's level, parent-handler setting, and handlers even if the action fails.
     *
     * @throws Throwable If the source class cannot be initialized or the action fails.
     */
    public static List<String> captureLogs(Class<?> source, Executable action) throws Throwable {
        Class.forName(source.getName());
        Logger logger = Logger.getLogger("supporterbook." + source.getSimpleName());
        Level originalLevel = logger.getLevel();
        boolean hadParentHandlers = logger.getUseParentHandlers();
        List<String> messages = new ArrayList<>();
        Handler handler = createHandler(messages);
        logger.addHandler(handler);
        logger.setLevel(Level.FINE);
        logger.setUseParentHandlers(false);
        try {
            action.execute();
            return messages;
        } finally {
            logger.removeHandler(handler);
            logger.setLevel(originalLevel);
            logger.setUseParentHandlers(hadParentHandlers);
        }
    }

    private static Handler createHandler(List<String> messages) {
        return new Handler() {
            @Override
            public void publish(LogRecord record) {
                assertEquals(Level.FINE, record.getLevel());
                messages.add(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
    }
}
