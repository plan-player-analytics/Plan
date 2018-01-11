/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.viaversion;

import com.djrapitops.plan.api.exceptions.DBCreateTableException;
import com.djrapitops.plan.database.databases.SQLDB;
import com.djrapitops.plan.database.processing.ExecStatement;
import com.djrapitops.plan.database.processing.QueryAllStatement;
import com.djrapitops.plan.database.processing.QueryStatement;
import com.djrapitops.plan.database.sql.Select;
import com.djrapitops.plan.database.tables.Table;

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
        super("plan_version_protocol", db, db.isUsingMySQL());
        columnUUID = "uuid";
        columnProtocolVersion = "protocol_version";
    }

    @Override
    public void createTable() throws DBCreateTableException {
        createTable("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + columnUUID + " varchar(36) NOT NULL UNIQUE, "
                + columnProtocolVersion + " integer NOT NULL"
                + ")"
        );
    }

    public void saveProtocolVersion(UUID uuid, int version) throws SQLException {
        if (exists(uuid)) {
            updateProtocolVersion(uuid, version);
        } else {
            insertProtocolVersion(uuid, version);
        }
    }

    public int getProtocolVersion(UUID uuid) throws SQLException {
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

    public Map<UUID, Integer> getProtocolVersions() throws SQLException {
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

    private boolean exists(UUID uuid) throws SQLException {
        return getProtocolVersion(uuid) != -1;
    }

    private void updateProtocolVersion(UUID uuid, int version) throws SQLException {
        String sql = "UPDATE " + tableName + " SET "
                + columnProtocolVersion + "=? "
                + " WHERE (" + columnUUID + "=?)";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, version);
                statement.setString(2, uuid.toString());
            }
        });
    }

    private void insertProtocolVersion(UUID uuid, int version) throws SQLException {
        String sql = "INSERT INTO " + tableName + " ("
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
