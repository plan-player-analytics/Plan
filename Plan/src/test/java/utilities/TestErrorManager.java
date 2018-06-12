/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package utilities;

import com.djrapitops.plugin.api.utility.log.errormanager.ErrorManager;

/**
 * ErrorManager for tests that should throw the exceptions that occur.
 *
 * @author Rsl1122
 */
public class TestErrorManager implements ErrorManager {

    @Override
    public void toLog(String s, Throwable throwable, Class aClass) {
        throw new RuntimeException("Error During Test.", throwable);
    }
}
