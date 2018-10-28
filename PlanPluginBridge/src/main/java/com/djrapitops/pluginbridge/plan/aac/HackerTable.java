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
    public void createTable() throws DBInitException {
        createTable("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + columnUUID + " varchar(36) NOT NULL, "
                + columnDate + " bigint NOT NULL, "
                + columnHackType + " varchar(100) NOT NULL, "
                + columnViolations + " integer NOT NULL"
                + ")"
        );
    }

    public List<HackObject> getHackObjects(UUID uuid) {
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
                    String hackType = set.getString(columnHackType);
                    int violationLevel = set.getInt(columnViolations);
                    hackObjects.add(new HackObject(uuid, date, hackType, violationLevel));
                }
                return hackObjects;
            }
        });
    }

    public Map<UUID, List<HackObject>> getHackObjects() {
        return query(new QueryAllStatement<Map<UUID, List<HackObject>>>(Select.all(tableName).toString(), 5000) {
            @Override
            public Map<UUID, List<HackObject>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<HackObject>> hackObjects = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(columnUUID));
                    long date = set.getLong(columnDate);
                    String hackType = set.getString(columnHackType);
                    int violationLevel = set.getInt(columnViolations);
                    List<HackObject> list = hackObjects.getOrDefault(uuid, new ArrayList<>());
                    list.add(new HackObject(uuid, date, hackType, violationLevel));
                    hackObjects.put(uuid, list);
                }
                return hackObjects;
            }
        });
    }

    public void insertHackRow(HackObject hackObject) {
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
                statement.setString(3, hackObject.getHackType());
                statement.setInt(4, hackObject.getViolationLevel());
            }
        });
    }
}
