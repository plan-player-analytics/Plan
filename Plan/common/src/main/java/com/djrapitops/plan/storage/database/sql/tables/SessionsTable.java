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

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.transactions.patches.SessionAFKTimePatch;
import com.djrapitops.plan.storage.database.transactions.patches.SessionsOptimizationPatch;
import com.djrapitops.plan.storage.database.transactions.patches.Version10Patch;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Table information about 'plan_sessions'.
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link SessionAFKTimePatch}
 * {@link SessionsOptimizationPatch}
 *
 * @author AuroraLS3
 */
public class SessionsTable {

    public static final String TABLE_NAME = "plan_sessions";

    public static final String ID = "id";
    public static final String USER_ID = "user_id";
    public static final String SERVER_ID = "server_id";
    public static final String SESSION_START = "session_start";
    public static final String SESSION_END = "session_end";
    public static final String MOB_KILLS = "mob_kills";
    public static final String DEATHS = "deaths";
    public static final String AFK_TIME = "afk_time";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " ("
            + USER_ID + ','
            + SESSION_START + ','
            + SESSION_END + ','
            + DEATHS + ','
            + MOB_KILLS + ','
            + AFK_TIME + ','
            + SERVER_ID
            + ") VALUES (" + UsersTable.SELECT_USER_ID + ", ?, ?, ?, ?, ?, " + ServerTable.SELECT_SERVER_ID + ")";

    public static final String SELECT_SESSION_ID_STATEMENT = "(SELECT " + TABLE_NAME + '.' + ID + FROM + TABLE_NAME +
            WHERE + TABLE_NAME + '.' + USER_ID + "=" + UsersTable.SELECT_USER_ID +
            AND + TABLE_NAME + '.' + SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
            AND + SESSION_START + "=?" +
            AND + SESSION_END + "=? LIMIT 1)";

    private SessionsTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_ID, Sql.INT).notNull()
                .column(SERVER_ID, Sql.INT).notNull()
                .column(SESSION_START, Sql.LONG).notNull()
                .column(SESSION_END, Sql.LONG).notNull()
                .column(MOB_KILLS, Sql.INT).notNull()
                .column(DEATHS, Sql.INT).notNull()
                .column(AFK_TIME, Sql.LONG).notNull()
                .foreignKey(USER_ID, UsersTable.TABLE_NAME, UsersTable.ID)
                .foreignKey(SERVER_ID, ServerTable.TABLE_NAME, ServerTable.ID)
                .toString();
    }
}