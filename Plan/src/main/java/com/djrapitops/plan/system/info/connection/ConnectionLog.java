/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.utility.log.Log;

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
     * @return {@code Map<"In:  "/"Out: "+Address, Map<InfoRequestClassname, ConnectionLog.Entry>>}
     */
    public static Map<String, Map<String, Entry>> getLogEntries() {
        return getInstance().getLog();
    }

    public static void logConnectionTo(Server server, InfoRequest request, int responseCode) {
        String requestName = request.getClass().getSimpleName();
        String address = server.getWebAddress();
        logConnection(address, "Out: " + requestName, responseCode);
        Log.debug("ConnectionOut: " + requestName + " to " + address);
    }

    public static void logConnectionFrom(String server, String requestTarget, int responseCode) {
        logConnection(server, "In:  " + requestTarget, responseCode);
        Log.debug("ConnectionIn: " + requestTarget + " from " + server);
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

    public static class Entry implements Comparable<Entry> {

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

        /**
         * Most recent first.
         *
         * @param o object
         * @return -1 or 1
         */
        @Override
        public int compareTo(Entry o) {
            return -Long.compare(this.timeSent, o.timeSent);
        }
    }

}