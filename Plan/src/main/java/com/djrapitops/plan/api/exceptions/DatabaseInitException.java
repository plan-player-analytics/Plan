/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.api.exceptions;

/**
 * Thrown when something goes wrong with Database#init.
 *
 * @author Rsl1122
 */
public class DatabaseInitException extends DatabaseException {

    public DatabaseInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseInitException(Throwable cause) {
        super(cause);
    }

    public DatabaseInitException(String message) {
        super(message);
    }
}