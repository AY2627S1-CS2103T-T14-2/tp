package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    @Test
    public void readJsonFile_nullContent_throwsDataLoadingException() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, "null");

        assertThrows(DataLoadingException.class, () ->
                JsonUtil.readJsonFile(SERIALIZATION_FILE, SerializableTestClass.class));
    }

    @Test
    public void fromJsonString_trailingWhitespace_returnsCompleteObject() throws IOException {
        String json = SerializableTestClass.JSON_STRING_REPRESENTATION + " \t\r\n";

        SerializableTestClass restored = JsonUtil.fromJsonString(json, SerializableTestClass.class);

        assertEquals(SerializableTestClass.getNameTestValue(), restored.getName());
        assertEquals(SerializableTestClass.getListTestValues(), restored.getListOfLocalDateTimes());
        assertEquals(SerializableTestClass.getHashMapTestValues(), restored.getMapOfIntegerToString());
    }

    @Test
    public void fromJsonString_trailingContent_throwsIoException() {
        for (String suffix : List.of("{}", "[]", "null", "123", "true", "\"extra\"", "not json")) {
            String json = SerializableTestClass.JSON_STRING_REPRESENTATION + " " + suffix;

            assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json, SerializableTestClass.class));
        }
        assertThrows(IOException.class, () -> JsonUtil.fromJsonString("null {}", SerializableTestClass.class));
    }

    @Test
    public void readJsonFile_trailingContent_throwsDataLoadingExceptionWithoutChangingFile() throws IOException {
        String json = SerializableTestClass.JSON_STRING_REPRESENTATION + "{}";
        FileUtil.writeToFile(SERIALIZATION_FILE, json);

        assertThrows(DataLoadingException.class, () ->
                JsonUtil.readJsonFile(SERIALIZATION_FILE, SerializableTestClass.class));
        assertEquals(json, FileUtil.readFromFile(SERIALIZATION_FILE));
    }

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
