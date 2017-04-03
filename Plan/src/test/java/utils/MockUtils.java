
package test.java.utils;

import org.bukkit.World;
import org.mockito.Mockito;

/**
 *
 * @author Rsl1122
 */
public class MockUtils {
    public static World mockWorld() {
        World mockWorld = Mockito.mock(World.class);
        Mockito.doReturn("World").when(mockWorld).toString();
        return mockWorld;
    }
}
