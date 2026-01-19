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
package com.djrapitops.plan.storage.database.sql.tables;

import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.GMTimes;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.queries.objects.lookup.ServerIdentifiable;
import com.djrapitops.plan.storage.database.queries.objects.lookup.UserIdentifiable;
import com.djrapitops.plan.storage.database.queries.objects.lookup.WorldIdentifiable;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.transactions.patches.Version10Patch;
import com.djrapitops.plan.storage.database.transactions.patches.WorldTimesOptimizationPatch;
import com.djrapitops.plan.storage.database.transactions.patches.WorldTimesSeverIDPatch;
import com.djrapitops.plan.storage.database.transactions.patches.WorldsServerIDPatch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.INSERT_INTO;

/**
 * Information about database table 'plan_world_times'.
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link WorldTimesSeverIDPatch}
 * {@link WorldsServerIDPatch}
 * {@link WorldTimesOptimizationPatch}
 *
 * @author AuroraLS3
 */
public class WorldTimesTable {

    public static final String TABLE_NAME = "plan_world_times";

    public static final String ID = "id";
    public static final String USER_ID = "user_id";
    public static final String SERVER_ID = "server_id";
    public static final String SESSION_ID = "session_id";
    public static final String WORLD_ID = "world_id";
    public static final String SURVIVAL = "survival_time";
    public static final String CREATIVE = "creative_time";
    public static final String ADVENTURE = "adventure_time";
    public static final String SPECTATOR = "spectator_time";

    public static final String INSERT_STATEMENT = INSERT_INTO + WorldTimesTable.TABLE_NAME + " (" +
            WorldTimesTable.SESSION_ID + ',' +
            WorldTimesTable.WORLD_ID + ',' +
            WorldTimesTable.USER_ID + ',' +
            WorldTimesTable.SERVER_ID + ',' +
            WorldTimesTable.SURVIVAL + ',' +
            WorldTimesTable.CREATIVE + ',' +
            WorldTimesTable.ADVENTURE + ',' +
            WorldTimesTable.SPECTATOR +
            ") VALUES ( " +
            SessionsTable.SELECT_SESSION_ID_STATEMENT + ',' +
            WorldTable.SELECT_WORLD_ID_STATEMENT + ',' +
            UsersTable.SELECT_USER_ID + ',' +
            ServerTable.SELECT_SERVER_ID + ',' +
            "?, ?, ?, ?)";

    private WorldTimesTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_ID, Sql.INT).notNull()
                .column(WORLD_ID, Sql.INT).notNull()
                .column(SERVER_ID, Sql.INT).notNull()
                .column(SESSION_ID, Sql.INT).notNull()
                .column(SURVIVAL, Sql.LONG).notNull().defaultValue("0")
                .column(CREATIVE, Sql.LONG).notNull().defaultValue("0")
                .column(ADVENTURE, Sql.LONG).notNull().defaultValue("0")
                .column(SPECTATOR, Sql.LONG).notNull().defaultValue("0")
                .foreignKey(WORLD_ID, WorldTable.TABLE_NAME, WorldTable.ID)
                .foreignKey(SESSION_ID, SessionsTable.TABLE_NAME, SessionsTable.ID)
                .foreignKey(USER_ID, UsersTable.TABLE_NAME, UsersTable.ID)
                .foreignKey(SERVER_ID, ServerTable.TABLE_NAME, ServerTable.ID)
                .toString();
    }

    public static void addSessionWorldTimesToBatch(PreparedStatement statement, FinishedSession session, String[] gms) throws SQLException {
        UUID uuid = session.getPlayerUUID();
        ServerUUID serverUUID = session.getServerUUID();
        Optional<WorldTimes> worldTimes = session.getExtraData().get(WorldTimes.class);
        if (worldTimes.isEmpty()) return;

        for (Map.Entry<String, GMTimes> worldTimesEntry : worldTimes.get().getWorldTimes().entrySet()) {
            String worldName = worldTimesEntry.getKey();
            GMTimes gmTimes = worldTimesEntry.getValue();

            // Session ID select statement
            statement.setString(1, uuid.toString());
            statement.setString(2, serverUUID.toString());
            statement.setLong(3, session.getStart());
            statement.setLong(4, session.getEnd());

            // World ID select statement
            statement.setString(5, worldName);
            statement.setString(6, serverUUID.toString());

            statement.setString(7, uuid.toString());
            statement.setString(8, serverUUID.toString());
            statement.setLong(9, gmTimes.getTime(gms[0]));
            statement.setLong(10, gmTimes.getTime(gms[1]));
            statement.setLong(11, gmTimes.getTime(gms[2]));
            statement.setLong(12, gmTimes.getTime(gms[3]));
            statement.addBatch();
        }
    }

    public static class Row implements ServerIdentifiable, UserIdentifiable, WorldIdentifiable {
        public int id;
        public int sessionId;
        public int serverId;
        public int userId;
        public int worldId;
        public long survivalTime;
        public long creativeTime;
        public long adventureTime;
        public long spectatorTime;

        public static Row extract(ResultSet set) throws SQLException {
            Row row = new Row();
            row.id = set.getInt(ID);
            row.sessionId = set.getInt(SESSION_ID);
            row.serverId = set.getInt(SERVER_ID);
            row.userId = set.getInt(USER_ID);
            row.worldId = set.getInt(WORLD_ID);
            row.survivalTime = set.getLong(SURVIVAL);
            row.creativeTime = set.getLong(CREATIVE);
            row.adventureTime = set.getLong(ADVENTURE);
            row.spectatorTime = set.getLong(SPECTATOR);
            return row;
        }

        @Override
        public int getServerId() {
            return serverId;
        }

        @Override
        public void setServerId(int serverId) {
            this.serverId = serverId;
        }

        @Override
        public int getUserId() {
            return userId;
        }

        @Override
        public void setUserId(int userId) {
            this.userId = userId;
        }

        @Override
        public int getWorldId() {
            return worldId;
        }

        @Override
        public void setWorldId(int worldId) {
            this.worldId = worldId;
        }

        public void insert(PreparedStatement statement) throws SQLException {
            statement.setInt(1, userId);
            statement.setInt(2, worldId);
            statement.setInt(3, serverId);
            statement.setInt(4, sessionId);
            statement.setLong(5, survivalTime);
            statement.setLong(6, creativeTime);
            statement.setLong(7, adventureTime);
            statement.setLong(8, spectatorTime);
        }
    }
}
