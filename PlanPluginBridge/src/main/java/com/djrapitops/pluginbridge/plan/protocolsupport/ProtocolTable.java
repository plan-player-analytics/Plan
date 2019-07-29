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
package com.djrapitops.pluginbridge.plan.protocolsupport;

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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class responsible for version protocol information in Plan database.
 *
 * @author Rsl1122

 */
public class ProtocolTable {

    public static final String TABLE_NAME = "plan_version_protocol";
    public static final String COL_ID = "id";
    public static final String COL_UUID = "uuid";
    public static final String COL_PROTOCOL_VERSION = "protocol_version";

    private ProtocolTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(COL_ID, Sql.INT).primaryKey()
                .column(COL_UUID, Sql.varchar(36)).notNull().unique()
                .column(COL_PROTOCOL_VERSION, Sql.INT).notNull()
                .build();
    }

    public static Query<Integer> getProtocolVersion(UUID uuid) {
        String sql = "SELECT " + COL_PROTOCOL_VERSION + " FROM " + TABLE_NAME + " WHERE " + COL_UUID + "=?";

        return new QueryStatement<Integer>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getInt(COL_PROTOCOL_VERSION);
                } else {
                    return -1;
                }
            }
        };
    }

    public static Query<Map<UUID, Integer>> getProtocolVersions() {
        return new QueryAllStatement<Map<UUID, Integer>>(Select.all(TABLE_NAME).toString(), 5000) {
            @Override
            public Map<UUID, Integer> processResults(ResultSet set) throws SQLException {
                Map<UUID, Integer> versions = new HashMap<>();
                while (set.next()) {
                    String uuidS = set.getString(COL_UUID);
                    UUID uuid = UUID.fromString(uuidS);
                    versions.put(uuid, set.getInt(COL_PROTOCOL_VERSION));
                }
                return versions;
            }
        };
    }
}
