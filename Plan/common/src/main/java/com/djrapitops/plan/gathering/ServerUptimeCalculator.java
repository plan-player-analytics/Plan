package com.djrapitops.plan.gathering;

import com.djrapitops.plan.PlanSystem;
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

}
