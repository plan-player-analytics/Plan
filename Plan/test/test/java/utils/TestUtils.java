/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package test.java.utils;

import java.lang.reflect.Field;

/**
 * @author Fuzzlemann
 */
public class TestUtils {
    public static String getStringFieldValue(Enum enumeration, String modifier) throws NoSuchFieldException, IllegalAccessException {
        Field field = enumeration.getClass().getDeclaredField(modifier);
        field.setAccessible(true);
        return (String) field.get(enumeration);
    }
}
