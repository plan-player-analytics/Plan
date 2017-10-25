/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.api.exceptions;

/**
 * Thrown when WebAPI fails to connect to an address.
 *
 * @author Rsl1122
 */
public class WebAPIConnectionFailException extends WebAPIException {

    public WebAPIConnectionFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebAPIConnectionFailException(Throwable cause) {
        super(cause);
    }
}