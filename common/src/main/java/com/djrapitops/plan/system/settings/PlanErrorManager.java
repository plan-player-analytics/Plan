/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.settings;

import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.ErrorLogger;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.api.utility.log.errormanager.ErrorManager;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract Plugin Framework ErrorManager for logging Errors properly instead of a vague message.
 *
 * @author Rsl1122
 */
public class PlanErrorManager implements ErrorManager {

    @Override
    public void toLog(String source, Throwable e, Class callingPlugin) {
        try {
            File logsFolder = Log.getLogsFolder(callingPlugin);
            Log.warn(source + " Caught: " + e, callingPlugin);
            if (WebServerSystem.isWebServerEnabled()) {
                Log.warn("Exception can be viewed at " + WebServer.getInstance().getAccessAddress() + "/debug");
            } else {
                Log.warn("It has been logged to ErrorLog.txt");
            }
            try {
                if ((Check.isBukkitAvailable() && Check.isBungeeAvailable()) || Settings.DEV_MODE.isTrue()) {
                    Logger.getGlobal().log(Level.WARNING, source, e);
                }
            } catch (IllegalStateException ignored) {
                /* Config system not initialized */
            }
            ErrorLogger.logThrowable(e, logsFolder);
        } catch (Exception exception) {
            System.out.println("Failed to log error to file because of " + exception);
            System.out.println("Error:");
            // Fallback
            System.out.println("Fail Reason:");
            Logger.getGlobal().log(Level.WARNING, source, e);
        }
    }
}
