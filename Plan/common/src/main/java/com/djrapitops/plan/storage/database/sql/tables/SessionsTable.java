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
import com.djrapitops.plan.storage.database.queries.objects.lookup.JoinAddressIdentifiable;
import com.djrapitops.plan.storage.database.queries.objects.lookup.ServerIdentifiable;
import com.djrapitops.plan.storage.database.queries.objects.lookup.UserIdentifiable;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Insert;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.transactions.patches.Patch;
import com.djrapitops.plan.storage.database.transactions.patches.SessionAFKTimePatch;
import com.djrapitops.plan.storage.database.transactions.patches.SessionsOptimizationPatch;
import com.djrapitops.plan.storage.database.transactions.patches.Version10Patch;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Table information about 'plan_sessions'.
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link SessionAFKTimePatch}
 * {@link SessionsOptimizationPatch}
 * {@link com.djrapitops.plan.storage.database.transactions.patches.SessionJoinAddressPatch}
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
    public static final String JOIN_ADDRESS_ID = "join_address_id";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " ("
            + USER_ID + ','
            + SESSION_START + ','
            + SESSION_END + ','
            + DEATHS + ','
            + MOB_KILLS + ','
            + AFK_TIME + ','
            + SERVER_ID + ','
            + JOIN_ADDRESS_ID
            + ") VALUES (" + UsersTable.SELECT_USER_ID + ", ?, ?, ?, ?, ?, " + ServerTable.SELECT_SERVER_ID + ", " + JoinAddressTable.SELECT_ID + ")";

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
                .column(JOIN_ADDRESS_ID, INT).notNull().defaultValue("1") // References JoinAddressTable.ID, but no foreign key to allow null values.
                .foreignKey(USER_ID, UsersTable.TABLE_NAME, UsersTable.ID)
                .foreignKey(SERVER_ID, ServerTable.TABLE_NAME, ServerTable.ID)
                .toString();
    }

    public static class Row implements ServerIdentifiable, UserIdentifiable, JoinAddressIdentifiable {
        public static final String OLD_ID = "old_id";

        public static String INSERT_STATEMENT_WITH_OLD_ID = Insert.values(TABLE_NAME, USER_ID, SERVER_ID, SESSION_START, SESSION_END,
                MOB_KILLS, DEATHS, AFK_TIME, JOIN_ADDRESS_ID, OLD_ID);

        public int id;
        public int userId;
        public int serverId;
        public long sessionStart;
        public long sessionEnd;
        public int mobKills;
        public int deaths;
        public long afkTime;
        public Integer joinAddressId;

        public static Row extract(ResultSet set) throws SQLException {
            Row row = new Row();
            row.id = set.getInt(ID);
            row.userId = set.getInt(USER_ID);
            row.serverId = set.getInt(SERVER_ID);
            row.sessionStart = set.getLong(SESSION_START);
            row.sessionEnd = set.getLong(SESSION_END);
            row.mobKills = set.getInt(MOB_KILLS);
            row.deaths = set.getInt(DEATHS);
            row.afkTime = set.getLong(AFK_TIME);
            row.joinAddressId = Sql.getIntOrNull(set, JOIN_ADDRESS_ID);
            return row;
        }

        public void insert(PreparedStatement statement, boolean withOldId) throws SQLException {
            statement.setInt(1, userId);
            statement.setInt(2, serverId);
            statement.setLong(3, sessionStart);
            statement.setLong(4, sessionEnd);
            statement.setInt(5, mobKills);
            statement.setInt(6, deaths);
            statement.setLong(7, afkTime);
            Sql.setIntOrNull(statement, 8, joinAddressId);
            if (withOldId) {
                statement.setInt(9, id);
            }
        }

        public static Patch addOldIdPatch() {
            return new Patch() {
                @Override
                public boolean hasBeenApplied() {
                    return hasColumn(TABLE_NAME, OLD_ID);
                }

                @Override
                protected void applyPatch() {
                    addColumn(TABLE_NAME, OLD_ID + " " + INT);
                }
            };
        }

        public static Patch removeOldIdPatch() {
            return new Patch() {
                @Override
                public boolean hasBeenApplied() {
                    return !hasColumn(TABLE_NAME, OLD_ID);
                }

                @Override
                protected void applyPatch() {
                    dropColumn(TABLE_NAME, OLD_ID);
                }
            };
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
        public @Nullable Integer getJoinAddressId() {
            return joinAddressId;
        }

        @Override
        public void setJoinAddressId(Integer joinAddressId) {
            this.joinAddressId = joinAddressId;
        }
    }

    public static class TemporaryIdLookupTable {
        public static String TABLE_NAME = "plan_temp_session_id_lookup";
        public static String OLD_ID = "old_id";
        public static String NEW_ID = "new_id";

        public static String REMOVE_ALL_STATEMENT = "DELETE FROM " + TABLE_NAME;
        public static String INSERT_ALL_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" + OLD_ID + ", " + NEW_ID + ")" +
                SELECT + Row.OLD_ID + ',' + SessionsTable.ID + FROM + SessionsTable.TABLE_NAME;
        public static String DROP_TABLE_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;

        private TemporaryIdLookupTable() {
            /* Static method class */
        }

        public static String createTableSQL(DBType dbType) {
            return CreateTableBuilder.create(TABLE_NAME, dbType)
                    .column(OLD_ID, Sql.INT).primaryKey()
                    .column(NEW_ID, Sql.INT).notNull()
                    .toString();
        }
    }
}