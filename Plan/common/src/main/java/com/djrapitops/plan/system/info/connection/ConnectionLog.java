/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.system.DebugChannels;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plugin.logging.debug.DebugLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class responsible for logging what {@link ConnectionOut} and {@link ConnectionIn} objects get as response.
 *
 * @author Rsl1122
 */
@Singleton
public class ConnectionLog {

    private final DebugLogger debugLogger;

    private final Map<String, Map<String, Entry>> log;

    @Inject
    public ConnectionLog(DebugLogger debugLogger) {
        this.debugLogger = debugLogger;
        log = new HashMap<>();
    }

    public void logConnectionTo(Server server, InfoRequest request, int responseCode) {
        String requestName = request.getClass().getSimpleName();
        String address = server.getWebAddress();
        logConnection(address, "Out: " + requestName, responseCode);
        debugLogger.logOn(DebugChannels.CONNECTIONS, "ConnectionOut: " + requestName + " to " + address);
    }

    public void logConnectionFrom(String server, String requestTarget, int responseCode) {
        logConnection(server, "In:  " + requestTarget, responseCode);
        debugLogger.logOn(DebugChannels.CONNECTIONS, "ConnectionIn: " + requestTarget + " from " + server);
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
