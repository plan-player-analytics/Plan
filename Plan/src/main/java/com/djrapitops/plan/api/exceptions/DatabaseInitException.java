/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.api.exceptions;

/**
 * //TODO Class Javadoc Comment
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