/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.utilities.Verify;

import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for logging what ConnectionOut objects get in return.
 *
 * @author Rsl1122
 */
public class ConnectionLog {

    private Map<Server, Map<String, Entry>> log;

    public ConnectionLog() {
        this.log = new HashMap<>();
    }

    public static Map<Server, Map<String, Entry>> getLogEntries() {
        return getInstance().getLog();
    }

    public static void logConnection(Server toServer, InfoRequest request, int responseCode) {
        Map<Server, Map<String, Entry>> log = getInstance().log;

        Map<String, Entry> requestMap = log.getOrDefault(toServer, new HashMap<>());
        requestMap.put(request.getClass().getSimpleName(), new Entry(responseCode, MiscUtils.getTime()));
        log.put(toServer, requestMap);
    }

    public static boolean isConnectionPlausible(Server server) {
        if (server == null) {
            return false;
        }

        Map<String, Entry> serverLog = getInstance().getLog().get(server);
        return Verify.isEmpty(serverLog) || hasConnectionSucceeded(serverLog);

    }

    public static boolean hasConnectionSucceeded(Server server) {
        if (server == null) {
            return false;
        }
        Map<String, Entry> serverLog = getInstance().getLog().get(server);
        return !Verify.isEmpty(serverLog)
                && hasConnectionSucceeded(serverLog);

    }

    private static boolean hasConnectionSucceeded(Map<String, Entry> serverLog) {
        for (Entry entry : serverLog.values()) {
            if (entry.responseCode == 200) {
                return true;
            }
        }
        return false;
    }

    private static ConnectionLog getInstance() {
        return ConnectionSystem.getInstance().getConnectionLog();
    }

    public Map<Server, Map<String, Entry>> getLog() {
        return log;
    }

    public static class Entry {

        private final int responseCode;
        private final long timeSent;

        public Entry(int responseCode, long timeSent) {
            this.responseCode = responseCode;
            this.timeSent = timeSent;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public long getTimeSent() {
            return timeSent;
        }
    }

}