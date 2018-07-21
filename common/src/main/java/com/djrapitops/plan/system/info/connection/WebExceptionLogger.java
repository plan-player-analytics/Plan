/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.utilities.java.ThrowingVoidFunction;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Class that decides what to do with WebExceptions.
 *
 * @author Rsl1122
 */
public class WebExceptionLogger {

    public static void logIfOccurs(Class c, ThrowingVoidFunction<WebException> function) {
        try {
            function.apply();
        } catch (ConnectionFailException e) {
            if (shouldLog(e)) {
                Log.warn(e.getMessage());
            }
        } catch (UnsupportedTransferDatabaseException | UnauthorizedServerException
                | NotFoundException | NoServersException e) {
            Log.warn(e.getMessage());
        } catch (WebException e) {
            Log.toLog(c, e);
        }
    }

    private static boolean shouldLog(ConnectionFailException e) {
        String address = getAddress(e);
        if (address == null) {
            return true;
        }
        Map<String, Map<String, ConnectionLog.Entry>> logEntries = ConnectionLog.getLogEntries();
        Map<String, ConnectionLog.Entry> entries = logEntries.get("Out: " + address);
        if (entries != null) {
            List<ConnectionLog.Entry> connections = new ArrayList<>(entries.values());
            Collections.sort(connections);
            return connections.isEmpty() || connections.get(0).getResponseCode() != -1;
        }
        return true;
    }

    private static String getAddress(ConnectionFailException e) {
        if (e.getMessage().contains("to address")) {
            String[] split = e.getMessage().split("to address: ");
            if (split.length == 2) {
                String[] split2 = split[1].split("<br>");
                if (split2.length == 2) {
                    return split2[0];
                }
            }
        }
        return null;
    }

}
