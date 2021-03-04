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

import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import com.djrapitops.plan.gathering.domain.GMTimes;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.transactions.patches.Version10Patch;
import com.djrapitops.plan.storage.database.transactions.patches.WorldTimesOptimizationPatch;
import com.djrapitops.plan.storage.database.transactions.patches.WorldTimesSeverIDPatch;
import com.djrapitops.plan.storage.database.transactions.patches.WorldsServerIDPatch;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

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
    public static final String USER_UUID = "uuid";
    public static final String SERVER_UUID = "server_uuid";
    public static final String SESSION_ID = "session_id";
    public static final String WORLD_ID = "world_id";
    public static final String SURVIVAL = "survival_time";
    public static final String CREATIVE = "creative_time";
    public static final String ADVENTURE = "adventure_time";
    public static final String SPECTATOR = "spectator_time";

    public static final String INSERT_STATEMENT = "INSERT INTO " + WorldTimesTable.TABLE_NAME + " (" +
            WorldTimesTable.SESSION_ID + ',' +
            WorldTimesTable.WORLD_ID + ',' +
            WorldTimesTable.USER_UUID + ',' +
            WorldTimesTable.SERVER_UUID + ',' +
            WorldTimesTable.SURVIVAL + ',' +
            WorldTimesTable.CREATIVE + ',' +
            WorldTimesTable.ADVENTURE + ',' +
            WorldTimesTable.SPECTATOR +
            ") VALUES ( " +
            SessionsTable.SELECT_SESSION_ID_STATEMENT + ',' +
            WorldTable.SELECT_WORLD_ID_STATEMENT + ',' +
            "?, ?, ?, ?, ?, ?)";

    private WorldTimesTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_UUID, Sql.varchar(36)).notNull()
                .column(WORLD_ID, Sql.INT).notNull()
                .column(SERVER_UUID, Sql.varchar(36)).notNull()
                .column(SESSION_ID, Sql.INT).notNull()
                .column(SURVIVAL, Sql.LONG).notNull().defaultValue("0")
                .column(CREATIVE, Sql.LONG).notNull().defaultValue("0")
                .column(ADVENTURE, Sql.LONG).notNull().defaultValue("0")
                .column(SPECTATOR, Sql.LONG).notNull().defaultValue("0")
                .foreignKey(WORLD_ID, WorldTable.TABLE_NAME, WorldTable.ID)
                .foreignKey(SESSION_ID, SessionsTable.TABLE_NAME, SessionsTable.ID)
                .toString();
    }

    public static void addSessionWorldTimesToBatch(PreparedStatement statement, Session session, String[] gms) throws SQLException {
        UUID uuid = session.getUnsafe(SessionKeys.UUID);
        UUID serverUUID = session.getUnsafe(SessionKeys.SERVER_UUID);
        Map<String, GMTimes> worldTimes = session.getUnsafe(SessionKeys.WORLD_TIMES).getWorldTimes();
        for (Map.Entry<String, GMTimes> worldTimesEntry : worldTimes.entrySet()) {
            String worldName = worldTimesEntry.getKey();
            GMTimes gmTimes = worldTimesEntry.getValue();

            // Session ID select statement
            statement.setString(1, uuid.toString());
            statement.setString(2, serverUUID.toString());
            statement.setLong(3, session.getUnsafe(SessionKeys.START));
            statement.setLong(4, session.getUnsafe(SessionKeys.END));

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
}
