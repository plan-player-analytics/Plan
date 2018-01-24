/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.aac;

import com.djrapitops.plan.api.exceptions.database.DBCreateTableException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Select;
import com.djrapitops.plan.system.database.databases.sql.tables.Table;
import me.konsolas.aac.api.HackType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Class responsible for AAC kick information in Plan database.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class HackerTable extends Table {

    private final String columnUUID;
    private final String columnDate;
    private final String columnHackType;
    private final String columnViolations;

    public HackerTable(SQLDB db) {
        super("plan_aac_hack_table", db);
        columnUUID = "uuid";
        columnDate = "date";
        columnHackType = "hack_type";
        columnViolations = "violation_level";
    }

    @Override
    public void createTable() throws DBCreateTableException {
        createTable("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + columnUUID + " varchar(36) NOT NULL, "
                + columnDate + " bigint NOT NULL, "
                + columnHackType + " varchar(100) NOT NULL, "
                + columnViolations + " integer NOT NULL"
                + ")"
        );
    }

    public List<HackObject> getHackObjects(UUID uuid) throws SQLException {
        String sql = "SELECT * FROM " + tableName + " WHERE " + columnUUID + "=?";

        return query(new QueryStatement<List<HackObject>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public List<HackObject> processResults(ResultSet set) throws SQLException {
                List<HackObject> hackObjects = new ArrayList<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(columnUUID));
                    long date = set.getLong(columnDate);
                    HackType hackType = HackType.valueOf(set.getString(columnHackType));
                    int violationLevel = set.getInt(columnViolations);
                    hackObjects.add(new HackObject(uuid, date, hackType, violationLevel));
                }
                return hackObjects;
            }
        });
    }

    public Map<UUID, List<HackObject>> getHackObjects() throws SQLException {
        return query(new QueryAllStatement<Map<UUID, List<HackObject>>>(Select.all(tableName).toString(), 5000) {
            @Override
            public Map<UUID, List<HackObject>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<HackObject>> hackObjects = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(columnUUID));
                    long date = set.getLong(columnDate);
                    HackType hackType = HackType.valueOf(set.getString(columnHackType));
                    int violationLevel = set.getInt(columnViolations);
                    List<HackObject> list = hackObjects.getOrDefault(uuid, new ArrayList<>());
                    list.add(new HackObject(uuid, date, hackType, violationLevel));
                    hackObjects.put(uuid, list);
                }
                return hackObjects;
            }
        });
    }

    public void insertHackRow(HackObject hackObject) throws SQLException {
        String sql = "INSERT INTO " + tableName + " ("
                + columnUUID + ", "
                + columnDate + ", "
                + columnHackType + ", "
                + columnViolations
                + ") VALUES (?, ?, ?, ?)";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, hackObject.getUuid().toString());
                statement.setLong(2, hackObject.getDate());
                statement.setString(3, hackObject.getHackType().name());
                statement.setInt(4, hackObject.getViolationLevel());
            }
        });
    }
}
