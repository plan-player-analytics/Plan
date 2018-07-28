/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
