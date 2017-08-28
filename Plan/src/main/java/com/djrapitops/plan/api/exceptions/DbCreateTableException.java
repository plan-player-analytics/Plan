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
public class DbCreateTableException extends DatabaseInitException {

    public DbCreateTableException(String tableName, String message, Throwable cause) {
        super(tableName + ": " + message, cause);
    }

    public DbCreateTableException(Throwable cause) {
        super(cause);
    }
}