/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plugin.api.utility.log.Log;

/**
 * Class that decides what to do with WebExceptions.
 *
 * @author Rsl1122
 */
public class WebExceptionLogger {

    public static void log(Class c, ExceptionLoggingAction action) {
        try {
            action.performAction();
        } catch (ConnectionFailException | UnsupportedTransferDatabaseException | UnauthorizedServerException
                | NotFoundException | NoServersException e) {
            Log.warn(e.getMessage());
        } catch (WebException e) {
            Log.toLog(WebExceptionLogger.class, e);
        }
    }

    public interface ExceptionLoggingAction {

        void performAction() throws WebException;

    }

}