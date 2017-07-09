/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.viaversion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.tables.Table;

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
        super("plan_viaversion_protocol", db, db.supportsModification());
        columnUUID = "uuid";
        columnProtocolVersion = "protocol_version";
    }

    @Override
    public boolean createTable() {
        try {
            execute("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + columnUUID + " varchar(36) NOT NULL UNIQUE, "
                    + columnProtocolVersion + " integer NOT NULL"
                    + ")"
            );
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    public void saveProtocolVersion(UUID uuid, int version) throws SQLException {
        if (exists(uuid)) {
            updateProtocolVersion(uuid, version);
        } else {
            insertProtocolVerison(uuid, version);
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
            close(set);
            close(statement);
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
            close(set);
            close(statement);
        }
    }

    private boolean exists(UUID uuid) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT " + columnUUID + " FROM " + tableName + " WHERE " + columnUUID + "=?");
            statement.setString(1, uuid.toString());
            set = statement.executeQuery();
            return set.next();
        } finally {
            close(set);
            close(statement);
        }
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
        } finally {
            close(statement);
        }
    }

    private void insertProtocolVerison(UUID uuid, int version) throws SQLException {
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
        } finally {
            close(statement);
        }
    }
}
