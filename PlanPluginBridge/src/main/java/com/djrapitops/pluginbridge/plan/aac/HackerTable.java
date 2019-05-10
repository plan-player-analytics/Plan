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
package com.djrapitops.pluginbridge.plan.aac;

import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Select;
import com.djrapitops.plan.db.sql.parsing.Sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table information about 'plan_aac_hack_table'.
 *
 * @author Rsl1122
 */
public class HackerTable {

    public static final String TABLE_NAME = "plan_aac_hack_table";
    public static final String COL_ID = "id";
    public static final String COL_UUID = "uuid";
    public static final String COL_DATE = "date";
    public static final String COL_HACK_TYPE = "hack_type";
    public static final String COL_VIOLATION_LEVEL = "violation_level";

    private HackerTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(COL_ID, Sql.INT).primaryKey()
                .column(COL_UUID, Sql.varchar(36)).notNull()
                .column(COL_DATE, Sql.LONG).notNull()
                .column(COL_HACK_TYPE, Sql.varchar(100)).notNull()
                .column(COL_VIOLATION_LEVEL, Sql.INT).notNull()
                .build();
    }

    public static Query<List<HackObject>> getHackObjects(UUID uuid) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_UUID + "=?";

        return new QueryStatement<List<HackObject>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public List<HackObject> processResults(ResultSet set) throws SQLException {
                List<HackObject> hackObjects = new ArrayList<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(COL_UUID));
                    long date = set.getLong(COL_DATE);
                    String hackType = set.getString(COL_HACK_TYPE);
                    int violationLevel = set.getInt(COL_VIOLATION_LEVEL);
                    hackObjects.add(new HackObject(uuid, date, hackType, violationLevel));
                }
                return hackObjects;
            }
        };
    }

    public static Query<Map<UUID, List<HackObject>>> getHackObjects() {
        return new QueryAllStatement<Map<UUID, List<HackObject>>>(Select.all(TABLE_NAME).toString(), 5000) {
            @Override
            public Map<UUID, List<HackObject>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<HackObject>> hackObjects = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(COL_UUID));
                    long date = set.getLong(COL_DATE);
                    String hackType = set.getString(COL_HACK_TYPE);
                    int violationLevel = set.getInt(COL_VIOLATION_LEVEL);
                    List<HackObject> list = hackObjects.getOrDefault(uuid, new ArrayList<>());
                    list.add(new HackObject(uuid, date, hackType, violationLevel));
                    hackObjects.put(uuid, list);
                }
                return hackObjects;
            }
        };
    }
}
