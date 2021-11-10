/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.gathering;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class ServerUptimeCalculator {

    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;

    @Inject
    public ServerUptimeCalculator(ServerInfo serverInfo, DBSystem dbSystem) {
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
    }

    public Optional<Long> getServerUptimeMillis(ServerUUID serverUUID) {
        if (serverUUID == null) throw new IllegalArgumentException("'serverUUID' can't be null");
        if (serverUUID.equals(serverInfo.getServerUUID())) {
            return Optional.of(System.currentTimeMillis() - PlanSystem.getServerEnableTime());
        } else {
            return getServerUptimeMillisFromDatabase(serverUUID);
        }
    }

    private Optional<Long> getServerUptimeMillisFromDatabase(ServerUUID serverUUID) {
        try {
            return tryToGetServerUptimeMillisFromDatabase(serverUUID);
        } catch (DBOpException windowFunctionsNotSupported) {
            return Optional.empty();
        }
    }

    private Optional<Long> tryToGetServerUptimeMillisFromDatabase(ServerUUID serverUUID) {
        long dataGapThreshold = TimeUnit.MINUTES.toMillis(3);
        Database database = dbSystem.getDatabase();
        Optional<Long> latestDataDate = database.query(TPSQueries.fetchLatestTPSEntryForServer(serverUUID)).map(TPS::getDate);
        Optional<Long> dataBlockStartDate = database.query(TPSQueries.fetchLatestServerStartTime(serverUUID, dataGapThreshold));

        if (!latestDataDate.isPresent() || !dataBlockStartDate.isPresent()) {
            return Optional.empty();
        }

        if (System.currentTimeMillis() - latestDataDate.get() > dataGapThreshold) {
            return Optional.empty();
        }

        return Optional.of(System.currentTimeMillis() - dataBlockStartDate.get());
    }

}
