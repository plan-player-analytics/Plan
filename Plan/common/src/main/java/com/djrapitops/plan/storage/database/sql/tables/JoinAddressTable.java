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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

public class JoinAddressTable {

    public static final String TABLE_NAME = "plan_join_address";
    public static final String ID = "id";
    public static final String JOIN_ADDRESS = "join_address";
    public static final int JOIN_ADDRESS_MAX_LENGTH = 191;

    public static final String SELECT_ID = '(' + SELECT + ID + FROM + TABLE_NAME + WHERE + JOIN_ADDRESS + "=?)";
    public static final String INSERT_STATEMENT = INSERT_INTO + TABLE_NAME +
            " (" + JOIN_ADDRESS + ") VALUES (?)";
    public static final String DEFAULT_VALUE_FOR_LOOKUP = "unknown";

    private JoinAddressTable() {}

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(JOIN_ADDRESS, Sql.varchar(JOIN_ADDRESS_MAX_LENGTH)).unique()
                .toString();
    }

    public static class Row {
        private int id;
        private String joinAddress;

        public static Row extract(ResultSet set) throws SQLException {
            Row row = new Row();
            row.id = set.getInt(ID);
            row.joinAddress = set.getString(JOIN_ADDRESS);
            return row;
        }

        public String getJoinAddress() {
            return joinAddress;
        }

        public int getId() {
            return id;
        }

        public void insert(PreparedStatement statement) throws SQLException {
            statement.setString(1, joinAddress);
        }
    }
}
