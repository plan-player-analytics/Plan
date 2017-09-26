/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.viaversion;

import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.tables.Table;

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
        super("plan_viaversion_protocol", db, db.isUsingMySQL());
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
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT " + columnProtocolVersion + " FROM " + tableName + " WHERE " + columnUUID + "=?");
            statement.setString(1, uuid.toString());
            set = statement.executeQuery();
            if (set.next()) {
                return set.getInt(columnProtocolVersion);
            } else {
                return -1;
            }
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public Map<UUID, Integer> getProtocolVersions() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName);
            set = statement.executeQuery();
            Map<UUID, Integer> versions = new HashMap<>();
            while (set.next()) {
                String uuidS = set.getString(columnUUID);
                UUID uuid = UUID.fromString(uuidS);
                versions.put(uuid, set.getInt(columnProtocolVersion));
            }
            return versions;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    private boolean exists(UUID uuid) throws SQLException {
        return getProtocolVersion(uuid) != -1;
    }

    private void updateProtocolVersion(UUID uuid, int version) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("UPDATE " + tableName + " SET "
                    + columnProtocolVersion + "=? "
                    + " WHERE (" + columnUUID + "=?)");
            statement.setInt(1, version);
            statement.setString(2, uuid.toString());
            statement.execute();

            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    private void insertProtocolVersion(UUID uuid, int version) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(
                    "INSERT INTO " + tableName + " ("
                            + columnUUID + ", "
                            + columnProtocolVersion
                            + ") VALUES (?, ?)");
            statement.setString(1, uuid.toString());
            statement.setInt(2, version);
            statement.execute();

            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }
}
