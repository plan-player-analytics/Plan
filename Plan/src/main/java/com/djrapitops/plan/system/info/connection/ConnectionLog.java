/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.utilities.MiscUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for logging what ConnectionOut objects get in return.
 *
 * @author Rsl1122
 */
public class ConnectionLog {

    private Map<String, Map<String, Entry>> log;

    public ConnectionLog() {
        this.log = new HashMap<>();
    }

    /**
     * Get a map sorted by Addresses, then Requests and then Log entries.
     *
     * @return {@code Map<"-> "/"<- "+Address, Map<InfoRequestClassname, ConnectionLog.Entry>>}
     */
    public static Map<String, Map<String, Entry>> getLogEntries() {
        return getInstance().getLog();
    }

    public static void logConnectionTo(Server server, InfoRequest request, int responseCode) {
        logConnection(server.getWebAddress(), "-> " + request.getClass().getSimpleName(), responseCode);
    }

    public static void logConnectionFrom(String server, String requestTarget, int responseCode) {
        logConnection(server, "<- " + requestTarget, responseCode);
    }

    private static void logConnection(String address, String infoRequestName, int responseCode) {
        Map<String, Map<String, Entry>> log = getInstance().log;
        Map<String, Entry> requestMap = log.getOrDefault(address, new HashMap<>());
        requestMap.put(infoRequestName, new Entry(responseCode, MiscUtils.getTime()));
        log.put(address, requestMap);
    }

    private static ConnectionLog getInstance() {
        return ConnectionSystem.getInstance().getConnectionLog();
    }

    public Map<String, Map<String, Entry>> getLog() {
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