/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.api.exceptions;

/**
 * Thrown when WebAPI returns 404, usually when response is supposed to be false.
 *
 * @author Rsl1122
 */
public class WebAPINotFoundException extends WebAPIException {
    public WebAPINotFoundException() {
        super("Not Found");
    }
}