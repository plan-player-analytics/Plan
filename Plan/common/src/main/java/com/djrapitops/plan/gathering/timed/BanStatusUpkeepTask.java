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
package com.djrapitops.plan.gathering.timed;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.storage.database.transactions.events.BatchBanStatusTransaction;
import net.playeranalytics.plugin.scheduling.RunnableFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author AuroraLS3
 */
@Singleton
public class BanStatusUpkeepTask extends TaskSystem.Task {

    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final ServerSensor<?> serverSensor;
    private Integer currentId;

    @Inject
    public BanStatusUpkeepTask(DBSystem dbSystem, ServerInfo serverInfo, ServerSensor<?> serverSensor) {
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.serverSensor = serverSensor;
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        if (!serverSensor.supportsBans()) return; // Don't register this task if server doesn't have bans.

        int randomStartDelay = ThreadLocalRandom.current().nextInt(30);
        runnableFactory.create(this)
                .runTaskTimerAsynchronously(randomStartDelay, 120, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        updateBanStatus();
    }

    CompletableFuture<?> updateBanStatus() {
        ServerUUID serverUUID = serverInfo.getServerUUID();
        Database database = dbSystem.getDatabase();
        Integer maxId = database.query(UserIdentifierQueries.fetchMaxUserId(serverUUID));
        if (currentId == null) {
            currentId = ThreadLocalRandom.current().nextInt(maxId);
        }

        List<UUID> toProcess = database.query(UserIdentifierQueries.fetchUUIDsStartingFromId(currentId, serverUUID, 51));
        if (!toProcess.isEmpty()) {
            UUID lastUUID = toProcess.get(toProcess.size() - 1);
            currentId = database.query(UserIdentifierQueries.fetchUserId(lastUUID)).orElse(0);
            if (currentId >= maxId) {
                currentId = 0;
            }
        } else {
            currentId = 0;
        }

        List<UUID> bannedPlayers = new ArrayList<>();
        List<UUID> unbannedPlayers = new ArrayList<>();

        int i = 0;
        for (UUID playerUUID : toProcess) {
            if (i > 50) break; // Skip the UUID next batch starts with
            boolean banned = serverSensor.isBanned(playerUUID);
            if (banned) {
                bannedPlayers.add(playerUUID);
            } else {
                unbannedPlayers.add(playerUUID);
            }
            i++;
        }

        if (bannedPlayers.isEmpty() && unbannedPlayers.isEmpty()) return null;
        return database.executeTransaction(new BatchBanStatusTransaction(bannedPlayers, unbannedPlayers, serverUUID));
    }
}
