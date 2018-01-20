/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBCreateTableException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plan.system.info.request.CacheAnalysisPageRequest;
import com.djrapitops.plan.system.info.request.CacheInspectPageRequest;
import com.djrapitops.plan.system.info.request.CacheNetworkPageContentRequest;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.TimeAmount;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Table that represents plan_transfer in SQL database.
 *
 * @author Rsl1122
 */
public class TransferTable extends Table {

    private final String columnSenderID = "sender_server_id";
    private final String columnExpiry = "expiry_date";
    private final String columnInfoType = "type";
    private final String columnContent = "content_64";
    private final String columnExtraVariables = "extra_variables";

    private final ServerTable serverTable;

    private final String insertStatement;
    private String selectStatement;

    public TransferTable(SQLDB db) {
        super("plan_transfer", db);

        serverTable = db.getServerTable();
        insertStatement = "INSERT INTO " + tableName + " (" +
                columnSenderID + ", " +
                columnExpiry + ", " +
                columnInfoType + ", " +
                columnExtraVariables + ", " +
                columnContent +
                ") VALUES (" +
                serverTable.statementSelectServerID + ", " +
                "?, ?, ?, ?, ?)";

        selectStatement = "SELECT * FROM " + tableName +
                " WHERE " + columnInfoType + "= ?" +
                " AND " + columnExpiry + "> ?";
    }

    @Override
    public void createTable() throws DBCreateTableException {
        createTable(TableSqlParser.createTable(tableName)
                .column(columnSenderID, Sql.INT).notNull()
                .column(columnExpiry, Sql.LONG).notNull().defaultValue("0")
                .column(columnInfoType, Sql.varchar(100)).notNull()
                .column(columnExtraVariables, Sql.varchar(255)).defaultValue("")
                .column(columnContent, usingMySQL ? "MEDIUMTEXT" : Sql.varchar(1)) // SQLite does not enforce varchar limits.
                .foreignKey(columnSenderID, serverTable.toString(), serverTable.getColumnID())
                .toString()
        );
    }

    public void storePlayerHtml(UUID player, String encodedHtml) throws SQLException {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, ServerInfo.getServerUUID().toString());
                statement.setLong(2, MiscUtils.getTime() + TimeAmount.MINUTE.ms());
                statement.setString(3, CacheInspectPageRequest.class.getSimpleName().toLowerCase());
                statement.setString(4, player.toString());
                statement.setString(5, encodedHtml);
            }
        });
    }

    public void storeServerHtml(UUID serverUUID, String encodedHtml) throws SQLException {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, ServerInfo.getServerUUID().toString());
                statement.setLong(2, MiscUtils.getTime() + TimeAmount.MINUTE.ms());
                statement.setString(3, CacheAnalysisPageRequest.class.getSimpleName().toLowerCase());
                statement.setString(4, serverUUID.toString());
                statement.setString(5, encodedHtml);
            }
        });
    }

    public void storeNetworkPageContent(UUID serverUUID, String encodedHtml) throws SQLException {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, ServerInfo.getServerUUID().toString());
                statement.setLong(2, MiscUtils.getTime() + TimeAmount.MINUTE.ms());
                statement.setString(3, CacheNetworkPageContentRequest.class.getSimpleName().toLowerCase());
                statement.setString(4, serverUUID.toString());
                statement.setString(5, encodedHtml);
            }
        });
    }

    private Map<UUID, String> getHtmlPerUUIDForCacheRequest(Class c) throws SQLException {
        return query(new QueryStatement<Map<UUID, String>>(selectStatement, 250) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, c.getSimpleName().toLowerCase());
                statement.setLong(2, MiscUtils.getTime());
            }

            @Override
            public Map<UUID, String> processResults(ResultSet set) throws SQLException {
                Map<UUID, String> htmlPerUUID = new HashMap<>();
                while (set.next()) {
                    String uuidString = set.getString(columnExtraVariables);
                    UUID uuid = UUID.fromString(uuidString);
                    String html64 = set.getString(columnContent);

                    htmlPerUUID.put(uuid, html64);
                }
                return htmlPerUUID;
            }
        });
    }

    public Map<UUID, String> getPlayerHtml() throws SQLException {
        return getHtmlPerUUIDForCacheRequest(CacheInspectPageRequest.class);
    }

    public Map<UUID, String> getNetworkPageContent() throws SQLException {
        return getHtmlPerUUIDForCacheRequest(CacheNetworkPageContentRequest.class);
    }

    public Map<UUID, String> getServerHtml() throws SQLException {
        return getHtmlPerUUIDForCacheRequest(CacheAnalysisPageRequest.class);
    }
}