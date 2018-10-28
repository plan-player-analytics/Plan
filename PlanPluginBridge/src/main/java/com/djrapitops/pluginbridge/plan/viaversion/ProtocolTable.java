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
package com.djrapitops.pluginbridge.plan.viaversion;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Select;
import com.djrapitops.plan.system.database.databases.sql.tables.Table;

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
 * @since 3.5.0
 */
public class ProtocolTable extends Table {

    private final String columnUUID;
    private final String columnProtocolVersion;

    public ProtocolTable(SQLDB db) {
        super("plan_version_protocol", db);
        columnUUID = "uuid";
        columnProtocolVersion = "protocol_version";
    }

    @Override
    public void createTable() throws DBInitException {
        createTable("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + columnUUID + " varchar(36) NOT NULL UNIQUE, "
                + columnProtocolVersion + " integer NOT NULL"
                + ")"
        );
    }

    public int getProtocolVersion(UUID uuid) {
        String sql = "SELECT " + columnProtocolVersion + " FROM " + tableName + " WHERE " + columnUUID + "=?";

        return query(new QueryStatement<Integer>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return set.getInt(columnProtocolVersion);
                } else {
                    return -1;
                }
            }
        });
    }

    public Map<UUID, Integer> getProtocolVersions() {
        return query(new QueryAllStatement<Map<UUID, Integer>>(Select.all(tableName).toString(), 5000) {
            @Override
            public Map<UUID, Integer> processResults(ResultSet set) throws SQLException {
                Map<UUID, Integer> versions = new HashMap<>();
                while (set.next()) {
                    String uuidS = set.getString(columnUUID);
                    UUID uuid = UUID.fromString(uuidS);
                    versions.put(uuid, set.getInt(columnProtocolVersion));
                }
                return versions;
            }
        });
    }

    public void saveProtocolVersion(UUID uuid, int version) {
        String sql = "REPLACE INTO " + tableName + " ("
                + columnUUID + ", "
                + columnProtocolVersion
                + ") VALUES (?, ?)";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setInt(2, version);
            }
        });
    }
}
