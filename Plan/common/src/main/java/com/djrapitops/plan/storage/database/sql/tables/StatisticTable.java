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
import com.djrapitops.plan.storage.database.sql.building.Insert;
import com.djrapitops.plan.storage.database.sql.building.Sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.FROM;
import static com.djrapitops.plan.storage.database.sql.building.Sql.SELECT;

/**
 * Table for plan_minecraft_statistic.
 *
 * @author AuroraLS3
 */
public class StatisticTable {

    public static final String TABLE_NAME = "plan_minecraft_statistic";

    public static final String ID = "id";
    public static final String STATISTIC_NAME = "statistic_name";

    public static final String SELECT_STATISTICS = SELECT + ID + ',' + STATISTIC_NAME + FROM + TABLE_NAME;

    private StatisticTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(STATISTIC_NAME, Sql.varchar(1000)).notNull().unique()
                .toString();
    }

    public static class Row {
        public static final String INSERT_STATEMENT = Insert.values(TABLE_NAME, STATISTIC_NAME);

        private int id;
        private String statisticName;

        public static Row extract(ResultSet set) throws SQLException {
            Row row = new Row();
            row.id = set.getInt(ID);
            row.statisticName = set.getString(STATISTIC_NAME);
            return row;
        }

        public void insert(PreparedStatement statement) throws SQLException {
            statement.setString(1, statisticName);
        }

        public int getId() {
            return id;
        }

        public String getStatisticName() {
            return statisticName;
        }
    }
}
