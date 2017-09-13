/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.api.exceptions;

/**
 * Thrown when WebUser can not be authorized (WebServer).
 *
 * @author Rsl1122
 */
public class WebUserAuthException extends Exception {
    public WebUserAuthException() {
    }

    public WebUserAuthException(String message) {
        super(message);
    }

    public WebUserAuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebUserAuthException(Throwable cause) {
        super(cause);
    }
}