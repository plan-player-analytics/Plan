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
package com.djrapitops.plan.query;

import com.djrapitops.plan.delivery.domain.mutators.ActivityIndex;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.containers.PlayerContainerQuery;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.storage.database.queries.schema.H2SchemaQueries;
import com.djrapitops.plan.storage.database.queries.schema.MySQLSchemaQueries;
import com.djrapitops.plan.storage.database.queries.schema.SQLiteSchemaQueries;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class CommonQueriesImplementation implements CommonQueries {

    private final Database db;
    private final PlanConfig config;

    CommonQueriesImplementation(Database db, PlanConfig config) {
        this.db = db;
        this.config = config;
    }

    @Override
    public long fetchPlaytime(UUID playerUUID, UUID serverUUID, long after, long before) {
        return db.query(SessionQueries.playtimeOfPlayer(after, before, playerUUID)).getOrDefault(serverUUID, 0L);
    }

    @Override
    public long fetchCurrentSessionPlaytime(UUID playerUUID) {
        return SessionCache.getCachedSession(playerUUID).map(Session::getLength).orElse(0L);
    }

    @Override
    public long fetchLastSeen(UUID playerUUID, UUID serverUUID) {
        return db.query(SessionQueries.lastSeen(playerUUID, serverUUID));
    }

    @Override
    public Set<UUID> fetchServerUUIDs() {
        return db.query(ServerQueries.fetchServerNames()).keySet();
    }

    @Override
    public Optional<UUID> fetchUUIDOf(String playerName) {
        return db.query(UserIdentifierQueries.fetchPlayerUUIDOf(playerName));
    }

    @Override
    public Optional<String> fetchNameOf(UUID playerUUID) {
        return db.query(UserIdentifierQueries.fetchPlayerNameOf(playerUUID));
    }

    @Override
    public boolean doesDBHaveTable(String table) {
        DBType dbType = db.getType();
        switch (dbType) {
            case H2:
                return db.query(H2SchemaQueries.doesTableExist(table));
            case SQLITE:
                return db.query(SQLiteSchemaQueries.doesTableExist(table));
            case MYSQL:
                return db.query(MySQLSchemaQueries.doesTableExist(table));
            default:
                throw new IllegalStateException("Unsupported Database Type: " + dbType.getName());
        }
    }

    @Override
    public boolean doesDBHaveTableColumn(String table, String column) {
        DBType dbType = db.getType();
        switch (dbType) {
            case H2:
                return db.query(H2SchemaQueries.doesColumnExist(table, column));
            case MYSQL:
                return db.query(MySQLSchemaQueries.doesColumnExist(table, column));
            case SQLITE:
                return db.query(SQLiteSchemaQueries.doesColumnExist(table, column));
            default:
                throw new IllegalStateException("Unsupported Database Type: " + dbType.getName());
        }
    }

    @Override
    public double fetchActivityIndexOf(UUID playerUUID, long epochMs) {
        return db.query(new PlayerContainerQuery(playerUUID)).getActivityIndex(epochMs, config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD)).getValue();
    }

    @Override
    public String getActivityGroupForIndex(double activityIndex) {
        return ActivityIndex.getGroup(activityIndex);
    }
}
