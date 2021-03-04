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
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.transactions.patches.KillsOptimizationPatch;
import com.djrapitops.plan.storage.database.transactions.patches.KillsServerIDPatch;
import com.djrapitops.plan.storage.database.transactions.patches.Version10Patch;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Table information about 'plan_kills'.
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link KillsServerIDPatch}
 * {@link KillsOptimizationPatch}
 *
 * @author AuroraLS3
 */
public class KillsTable {

    public static final String TABLE_NAME = "plan_kills";

    public static final String ID = "id";
    public static final String KILLER_UUID = "killer_uuid";
    public static final String VICTIM_UUID = "victim_uuid";
    public static final String SERVER_UUID = "server_uuid";
    public static final String SESSION_ID = "session_id";
    public static final String WEAPON = "weapon";
    public static final String DATE = "date";

    public static final int WEAPON_COLUMN_LENGTH = 30;

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " ("
            + SESSION_ID + ','
            + KILLER_UUID + ','
            + VICTIM_UUID + ','
            + SERVER_UUID + ','
            + DATE + ','
            + WEAPON
            + ") VALUES (" + SessionsTable.SELECT_SESSION_ID_STATEMENT + ", ?, ?, ?, ?, ?)";

    private KillsTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(KILLER_UUID, Sql.varchar(36)).notNull()
                .column(VICTIM_UUID, Sql.varchar(36)).notNull()
                .column(SERVER_UUID, Sql.varchar(36)).notNull()
                .column(WEAPON, Sql.varchar(WEAPON_COLUMN_LENGTH)).notNull()
                .column(DATE, Sql.LONG).notNull()
                .column(SESSION_ID, Sql.INT).notNull()
                .foreignKey(SESSION_ID, SessionsTable.TABLE_NAME, SessionsTable.ID)
                .toString();
    }

    public static void addSessionKillsToBatch(PreparedStatement statement, Session session) throws SQLException {
        UUID uuid = session.getUnsafe(SessionKeys.UUID);
        UUID serverUUID = session.getUnsafe(SessionKeys.SERVER_UUID);

        for (PlayerKill kill : session.getPlayerKills()) {
            // Session ID select statement parameters
            statement.setString(1, uuid.toString());
            statement.setString(2, serverUUID.toString());
            statement.setLong(3, session.getUnsafe(SessionKeys.START));
            statement.setLong(4, session.getUnsafe(SessionKeys.END));

            // Kill data
            statement.setString(5, uuid.toString());
            statement.setString(6, kill.getVictim().toString());
            statement.setString(7, serverUUID.toString());
            statement.setLong(8, kill.getDate());
            statement.setString(9, StringUtils.truncate(kill.getWeapon(), WEAPON_COLUMN_LENGTH));
            statement.addBatch();
        }
    }
}
