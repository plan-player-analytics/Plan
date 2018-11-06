package utilities;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Utility for asserts containing Optionals.
 *
 * @author Rsl1122
 */
public class OptionalAssert {

    public static <T> void equals(T expected, Optional<T> result) {
        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

}