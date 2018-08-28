/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plugin.logging.debug.DebugLogger;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class responsible for logging what {@link ConnectionOut} and {@link ConnectionIn} objects get as response.
 *
 * @author Rsl1122
 */
public class ConnectionLog {

    private final DebugLogger debugLogger;

    private final Map<String, Map<String, Entry>> log;

    @Inject
    public ConnectionLog(DebugLogger debugLogger) {
        this.debugLogger = debugLogger;
        log = new HashMap<>();
    }

    /**
     * Get a map sorted by Addresses, then Requests and then Log entries.
     *
     * @return {@code Map<"In:  "/"Out: "+Address, Map<InfoRequestClassname, ConnectionLog.Entry>>}
     */
    @Deprecated
    public static Map<String, Map<String, Entry>> getLogEntries_Old() {
        return getInstance().getLogEntries();
    }

    @Deprecated
    public static void logConnectionTo_Old(Server server, InfoRequest request, int responseCode) {
        getInstance().logConnectionTo(server, request, responseCode);
    }

    @Deprecated
    public static void logConnectionFrom_Old(String server, String requestTarget, int responseCode) {
        getInstance().logConnectionFrom(server, requestTarget, responseCode);
    }

    @Deprecated
    private static ConnectionLog getInstance() {
        return ConnectionSystem.getInstance().getConnectionLog();
    }

    public void logConnectionTo(Server server, InfoRequest request, int responseCode) {
        String requestName = request.getClass().getSimpleName();
        String address = server.getWebAddress();
        logConnection(address, "Out: " + requestName, responseCode);
        debugLogger.logOn("Connections", "ConnectionOut: " + requestName + " to " + address);
    }

    public void logConnectionFrom(String server, String requestTarget, int responseCode) {
        logConnection(server, "In:  " + requestTarget, responseCode);
        debugLogger.logOn("Connections", "ConnectionIn: " + requestTarget + " from " + server);
    }

    private void logConnection(String address, String infoRequestName, int responseCode) {
        Map<String, Entry> requestMap = log.getOrDefault(address, new HashMap<>());
        requestMap.put(infoRequestName, new Entry(responseCode, System.currentTimeMillis()));
        log.put(address, requestMap);
    }

    public Map<String, Map<String, Entry>> getLogEntries() {
        return log;
    }

    public static class Entry implements Comparable<Entry>, DateHolder {

        private final int responseCode;
        private final long date;

        public Entry(int responseCode, long date) {
            this.responseCode = responseCode;
            this.date = date;
        }

        public int getResponseCode() {
            return responseCode;
        }

        @Override
        public long getDate() {
            return date;
        }

        /**
         * Most recent first.
         *
         * @param o object
         * @return -1 or 1
         */
        @Override
        public int compareTo(Entry o) {
            return Long.compare(o.date, this.date);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entry)) return false;
            Entry entry = (Entry) o;
            return responseCode == entry.responseCode &&
                    date == entry.date;
        }

        @Override
        public int hashCode() {
            return Objects.hash(responseCode, date);
        }
    }

}
