/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.api.exceptions;

/**
 * Thrown when WebAPI POST-request fails, general Exception.
 *
 * @author Rsl1122
 */
public class WebAPIException extends Exception {

    public WebAPIException() {
    }

    public WebAPIException(String message) {
        super(message);
    }

    public WebAPIException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebAPIException(Throwable cause) {
        super(cause);
    }
}