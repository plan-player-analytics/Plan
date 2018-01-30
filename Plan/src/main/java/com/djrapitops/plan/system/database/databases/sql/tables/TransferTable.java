/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plan.system.info.request.CacheAnalysisPageRequest;
import com.djrapitops.plan.system.info.request.CacheInspectPageRequest;
import com.djrapitops.plan.system.info.request.CacheInspectPluginsTabRequest;
import com.djrapitops.plan.system.info.request.CacheNetworkPageContentRequest;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.TimeAmount;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Table that represents plan_transfer in SQL database.
 *
 * @author Rsl1122
 */
public class TransferTable extends Table {

    private static final String columnSenderID = "sender_server_id";
    private static final String columnExpiry = "expiry_date";
    private static final String columnInfoType = "type";
    private static final String columnContent = "content_64";
    private static final String columnExtraVariables = "extra_variables";

    private final ServerTable serverTable;

    private final String insertStatement;
    private final String selectStatement;

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
                "?, ?, ?, ?)";

        selectStatement = "SELECT * FROM " + tableName +
                " WHERE " + columnInfoType + "= ?" +
                " AND " + columnExpiry + "> ?";
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .column(columnSenderID, Sql.INT).notNull()
                .column(columnExpiry, Sql.LONG).notNull().defaultValue("0")
                .column(columnInfoType, Sql.varchar(100)).notNull()
                .column(columnExtraVariables, Sql.varchar(255)).defaultValue("''")
                .column(columnContent, usingMySQL ? "MEDIUMTEXT" : Sql.varchar(1)) // SQLite does not enforce varchar limits.
                .foreignKey(columnSenderID, serverTable.toString(), serverTable.getColumnID())
                .toString()
        );
    }

    public void clean() throws SQLException {
        String sql = "DELETE FROM " + tableName +
                " WHERE " + columnExpiry + " < ?" +
                " AND " + columnInfoType + " != ?";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, MiscUtils.getTime() + TimeAmount.MINUTE.ms());
                statement.setString(2, "onlineStatus");
            }
        });
        sql = "DELETE FROM " + tableName +
                " WHERE " + columnSenderID + " = " + serverTable.statementSelectServerID +
                " AND " + columnInfoType + " != ?";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, ServerInfo.getServerUUID().toString());
                statement.setString(2, "onlineStatus");
            }
        });
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

    public void storePlayerPluginsTab(UUID player, String encodedHtml) throws SQLException {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, ServerInfo.getServerUUID().toString());
                statement.setLong(2, MiscUtils.getTime() + TimeAmount.MINUTE.ms());
                statement.setString(3, CacheInspectPluginsTabRequest.class.getSimpleName().toLowerCase());
                statement.setString(4, player.toString());
                statement.setString(5, encodedHtml);
            }
        });
    }

    public Map<UUID, String> getPlayerPluginsTabs(UUID playerUUID) throws SQLException {
        String serverIDColumn = serverTable + "." + serverTable.getColumnID();
        String serverUUIDColumn = serverTable + "." + serverTable.getColumnUUID() + " as s_uuid";
        String sql = "SELECT " +
                columnContent + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + serverTable + " on " + serverIDColumn + "=" + columnSenderID +
                " WHERE " + columnInfoType + "= ?" +
                " AND " + columnExpiry + "> ?" +
                " AND " + columnExtraVariables + "=?";

        return query(new QueryStatement<Map<UUID, String>>(sql, 250) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, CacheInspectPluginsTabRequest.class.getSimpleName().toLowerCase());
                statement.setLong(2, MiscUtils.getTime());
                statement.setString(3, playerUUID.toString());
            }

            @Override
            public Map<UUID, String> processResults(ResultSet set) throws SQLException {
                Map<UUID, String> htmlPerUUID = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));
                    String html64 = set.getString(columnContent);

                    htmlPerUUID.put(serverUUID, html64);
                }
                return htmlPerUUID;
            }
        });
    }

    public Optional<UUID> getServerPlayerIsOnline(UUID playerUUID) throws SQLException {
        String serverIDColumn = serverTable + "." + serverTable.getColumnID();
        String serverUUIDColumn = serverTable + "." + serverTable.getColumnUUID() + " as s_uuid";
        String sql = "SELECT " +
                serverUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + serverTable + " on " + serverIDColumn + "=" + columnSenderID +
                " WHERE " + columnExtraVariables + "=?" +
                " ORDER BY " + columnExpiry + " LIMIT 1";

        return query(new QueryStatement<Optional<UUID>>(sql, 1) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Optional<UUID> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(UUID.fromString(set.getString("s_uuid")));
                }
                return Optional.empty();
            }
        });
    }

    public void storePlayerOnlineOnThisServer(UUID playerUUID) throws SQLException {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, ServerInfo.getServerUUID().toString());
                statement.setLong(2, MiscUtils.getTime() + TimeAmount.MINUTE.ms());
                statement.setString(3, "onlineStatus");
                statement.setString(4, playerUUID.toString());
                statement.setString(5, null);
            }
        });
    }
}