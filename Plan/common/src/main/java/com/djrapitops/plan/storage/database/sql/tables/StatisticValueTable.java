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
import com.djrapitops.plan.storage.database.queries.objects.lookup.ServerIdentifiable;
import com.djrapitops.plan.storage.database.queries.objects.lookup.StatisticIdentifiable;
import com.djrapitops.plan.storage.database.queries.objects.lookup.UserIdentifiable;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Insert;
import com.djrapitops.plan.storage.database.sql.building.Sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Table for plan_minecraft_statistic_value.
 *
 * @author AuroraLS3
 */
public class StatisticValueTable {

    public static final String TABLE_NAME = "plan_minecraft_statistic_value";

    public static final String ID = "id";
    public static final String STATISTIC_ID = "statistic_id";
    public static final String USER_ID = "user_id";
    public static final String SERVER_ID = "server_id";
    public static final String VALUE = "value";

    public static final String INSERT_STATEMENT = INSERT_INTO + TABLE_NAME + '(' +
            VALUE + ',' +
            STATISTIC_ID + ',' +
            USER_ID + ',' +
            SERVER_ID +
            ") VALUES (?,?,?,?)";
    public static final String UPDATE_STATEMENT = UPDATE + TABLE_NAME +
            SET + VALUE + "=?" +
            WHERE + STATISTIC_ID + "=?" +
            AND + USER_ID + "=?" +
            AND + SERVER_ID + "=?";

    private StatisticValueTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(STATISTIC_ID, Sql.INT).notNull()
                .column(USER_ID, Sql.INT).notNull()
                .column(SERVER_ID, Sql.INT).notNull()
                .column(VALUE, Sql.INT).notNull()
                .foreignKey(STATISTIC_ID, StatisticTable.TABLE_NAME, StatisticTable.ID)
                .foreignKey(USER_ID, UsersTable.TABLE_NAME, UsersTable.ID)
                .foreignKey(SERVER_ID, ServerTable.TABLE_NAME, ServerTable.ID)
                .toString();
    }

    public static class Row implements UserIdentifiable, ServerIdentifiable, StatisticIdentifiable {
        public static final String INSERT_STATEMENT = Insert.values(TABLE_NAME, STATISTIC_ID, USER_ID, SERVER_ID, VALUE);

        private int id;
        private int statisticId;
        private int userId;
        private int serverId;
        private int value;

        public static Row extract(ResultSet set) throws SQLException {
            Row row = new Row();
            row.id = set.getInt(ID);
            row.statisticId = set.getInt(STATISTIC_ID);
            row.userId = set.getInt(USER_ID);
            row.serverId = set.getInt(SERVER_ID);
            row.value = set.getInt(VALUE);
            return row;
        }

        public void insert(PreparedStatement statement) throws SQLException {
            statement.setInt(1, statisticId);
            statement.setInt(2, userId);
            statement.setInt(3, serverId);
            statement.setInt(4, value);
        }

        public int getId() {
            return id;
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
        public int getStatisticId() {
            return statisticId;
        }

        @Override
        public void setStatisticId(int statisticId) {
            this.statisticId = statisticId;
        }
    }

}
