/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package utilities.mocks.objects;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logger to use during tests with Mockito.
 *
 * @author Rsl1122
 */
public class TestLogger extends Logger {

    public TestLogger() {
        super("TestLogger", null);
    }

    @Override
    public void log(Level level, String msg) {
        System.out.println(level.getName() + ": " + msg);
    }
}
