/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.api.TimeAmount;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Table that is in charge of transferring data between network servers.
 * <p>
 * Table Name: plan_transfer
 * <p>
 * For contained columns {@see Col}
 *
 * @author Rsl1122
 */
public class TransferTable extends Table {

    public static final String TABLE_NAME = "plan_transfer";
    private final String insertStatementNoParts;

    private final ServerTable serverTable;
    private final String selectStatement;

    public TransferTable(SQLDB db) {
        super(TABLE_NAME, db);

        serverTable = db.getServerTable();
        insertStatementNoParts = "REPLACE INTO " + tableName + " (" +
                Col.SENDER_ID + ", " +
                Col.EXPIRY + ", " +
                Col.INFO_TYPE + ", " +
                Col.EXTRA_VARIABLES + ", " +
                Col.CONTENT +
                ") VALUES (" +
                serverTable.statementSelectServerID + ", " +
                "?, ?, ?, ?)";

        selectStatement = "SELECT * FROM " + tableName +
                " WHERE " + Col.INFO_TYPE + "= ?" +
                " AND " + Col.EXPIRY + "> ?" +
                " ORDER BY " + Col.EXPIRY + " DESC, "
                + Col.PART + " ASC";
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .column(Col.SENDER_ID, Sql.INT).notNull()
                .column(Col.EXPIRY, Sql.LONG).notNull().defaultValue("0")
                .column(Col.INFO_TYPE, Sql.varchar(100)).notNull()
                .column(Col.EXTRA_VARIABLES, Sql.varchar(255)).defaultValue("''")
                .column(Col.CONTENT, usingMySQL ? "MEDIUMTEXT" : Sql.varchar(1)) // SQLite does not enforce varchar limits.
                .column(Col.PART, Sql.LONG).notNull().defaultValue("0")
                .foreignKey(Col.SENDER_ID, serverTable.toString(), ServerTable.Col.SERVER_ID)
                .toString()
        );
    }

    public void clean() {
        String sql = "DELETE FROM " + tableName +
                " WHERE " + Col.EXPIRY + " < ?" +
                " AND " + Col.INFO_TYPE + " != ?";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, System.currentTimeMillis());
                statement.setString(2, "onlineStatus");
            }
        });
        sql = "DELETE FROM " + tableName +
                " WHERE " + Col.SENDER_ID + " = " + serverTable.statementSelectServerID +
                " AND " + Col.INFO_TYPE + " = ?";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, ServerInfo.getServerUUID().toString());
                statement.setString(2, "onlineStatus");
            }
        });
    }

    public void storeConfigSettings(String encodedSettingString) {
        execute(new ExecStatement(insertStatementNoParts) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, ServerInfo.getServerUUID().toString());
                statement.setLong(2, System.currentTimeMillis() + TimeAmount.HOUR.ms());
                statement.setString(3, "configSettings");
                statement.setString(4, null);
                statement.setString(5, encodedSettingString);
            }
        });
    }

    public Optional<String> getConfigSettings() {
        return query(new QueryStatement<Optional<String>>(selectStatement, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, "configSettings");
                statement.setLong(2, System.currentTimeMillis());
            }

            @Override
            public Optional<String> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.ofNullable(set.getString(Col.CONTENT.get()));
                }
                return Optional.empty();
            }
        });
    }

    public enum Col implements Column {
        SENDER_ID("sender_server_id"),
        EXPIRY("expiry_date"),
        INFO_TYPE("type"),
        CONTENT("content_64"),
        EXTRA_VARIABLES("extra_variables"),
        PART("part");

        private final String column;

        Col(String column) {
            this.column = column;
        }

        @Override
        public String get() {
            return toString();
        }

        @Override
        public String toString() {
            return column;
        }
    }
}
