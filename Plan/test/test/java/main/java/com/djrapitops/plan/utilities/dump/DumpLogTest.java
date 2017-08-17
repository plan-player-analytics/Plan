package test.java.main.java.com.djrapitops.plan.utilities.dump;

import main.java.com.djrapitops.plan.utilities.file.dump.DumpLog;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Fuzzlemann
 */
public class DumpLogTest {

    @Test
    public void testDumpLogCreation() {
        DumpLog testLog = new DumpLog();

        testLog.addHeader("Test Header");
        testLog.add("StringValue", "Test");
        testLog.add("BooleanValue", true);
        testLog.add("IterableValue", Arrays.asList("Iterable 1", "Iterable 2"));

        testLog.addLine(new StringBuilder("CharSequence Test"));
        testLog.addLines(new StringBuilder("CharSequences Test"), new StringBuilder("CharSequences Test"));
        testLog.addLines(Arrays.asList("Iterable 1", "Iterable 2"));

        String expResult = "\n--- Test Header ---\n" +
                "StringValue: Test\n" +
                "BooleanValue: true\n" +
                "IterableValue: Iterable 1, Iterable 2\n" +
                "CharSequence Test\n" +
                "CharSequences Test\n" +
                "CharSequences Test\n" +
                "Iterable 1\n" +
                "Iterable 2";
        String result = testLog.toString();

        assertEquals(expResult, result);
    }
}
