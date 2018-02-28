/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plan.system.info.request.CacheAnalysisPageRequest;
import com.djrapitops.plan.system.info.request.CacheInspectPageRequest;
import com.djrapitops.plan.system.info.request.CacheInspectPluginsTabRequest;
import com.djrapitops.plan.system.info.request.CacheNetworkPageContentRequest;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.utilities.Base64Util;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.TimeAmount;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table that represents plan_transfer in SQL database.
 *
 * @author Rsl1122
 */
public class TransferTable extends Table {

    private final String insertStatementNoParts;

    private final ServerTable serverTable;
    private final String insertStatementParts;
    private final String selectStatement;

    public TransferTable(SQLDB db) {
        super("plan_transfer", db);

        serverTable = db.getServerTable();
        insertStatementParts = "REPLACE INTO " + tableName + " (" +
                Col.SENDER_ID + ", " +
                Col.EXPIRY + ", " +
                Col.INFO_TYPE + ", " +
                Col.EXTRA_VARIABLES + ", " +
                Col.CONTENT + ", " +
                Col.PART +
                ") VALUES (" +
                serverTable.statementSelectServerID + ", " +
                "?, ?, ?, ?, ?)";
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

    public void alterTableV14() {
        addColumns(Col.PART + " bigint NOT NULL DEFAULT 0");
    }

    public void clean() throws SQLException {
        String sql = "DELETE FROM " + tableName +
                " WHERE " + Col.EXPIRY + " < ?" +
                " AND " + Col.INFO_TYPE + " != ?";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, MiscUtils.getTime());
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

    public void storePlayerHtml(UUID player, String encodedHtml) throws SQLException {
        execute(new ExecStatement(insertStatementNoParts) {
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
        List<String> split = Base64Util.split(encodedHtml, 500000L);

        int i = 0;
        long expires = MiscUtils.getTime() + TimeAmount.MINUTE.ms();
        for (String part : split) {
            final int partNumber = i;
            execute(new ExecStatement(insertStatementParts) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setString(1, ServerInfo.getServerUUID().toString());
                    statement.setLong(2, expires);
                    statement.setString(3, CacheAnalysisPageRequest.class.getSimpleName().toLowerCase());
                    statement.setString(4, serverUUID.toString());
                    statement.setString(5, part);
                    statement.setInt(6, partNumber);
                }
            });
            i++;
        }
    }

    public void storeNetworkPageContent(UUID serverUUID, String encodedHtml) throws SQLException {
        execute(new ExecStatement(insertStatementNoParts) {
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
                Map<UUID, Long> expiry = new HashMap<>();
                while (set.next()) {
                    String uuidString = set.getString(Col.EXTRA_VARIABLES.get());
                    UUID uuid = UUID.fromString(uuidString);

                    long expires = set.getLong(Col.EXPIRY.get());

                    long correctExpiry = expiry.getOrDefault(uuid, expires);
                    if (expires == correctExpiry) {
                        htmlPerUUID.put(uuid, htmlPerUUID.getOrDefault(uuid, "") + set.getString(Col.CONTENT.get()));
                        expiry.put(uuid, correctExpiry);
                    }
                }
                return htmlPerUUID;
            }
        });
    }

    public void storePlayerPluginsTab(UUID player, String encodedHtml) throws SQLException {
        execute(new ExecStatement(insertStatementNoParts) {
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

    public Map<UUID, String> getPlayerHtml() throws SQLException {
        return getHtmlPerUUIDForCacheRequest(CacheInspectPageRequest.class);
    }

    public Map<UUID, String> getNetworkPageContent() throws SQLException {
        return getHtmlPerUUIDForCacheRequest(CacheNetworkPageContentRequest.class);
    }

    public Map<UUID, String> getServerHtml() throws SQLException {
        return getHtmlPerUUIDForCacheRequest(CacheAnalysisPageRequest.class);
    }

    public Map<UUID, String> getPlayerPluginsTabs(UUID playerUUID) throws SQLException {
        String serverIDColumn = serverTable + "." + serverTable.getColumnID();
        String serverUUIDColumn = serverTable + "." + serverTable.getColumnUUID() + " as s_uuid";
        String sql = "SELECT " +
                Col.CONTENT + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + serverTable + " on " + serverIDColumn + "=" + Col.SENDER_ID +
                " WHERE " + Col.INFO_TYPE + "= ?" +
                " AND " + Col.EXPIRY + "> ?" +
                " AND " + Col.EXTRA_VARIABLES + "=?";

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
                    String html64 = set.getString(Col.CONTENT.get());

                    htmlPerUUID.put(serverUUID, html64);
                }
                return htmlPerUUID;
            }
        });
    }

    @Deprecated
    public Optional<UUID> getServerPlayerIsOnline(UUID playerUUID) throws SQLException {
        String serverIDColumn = serverTable + "." + serverTable.getColumnID();
        String serverUUIDColumn = serverTable + "." + serverTable.getColumnUUID() + " as s_uuid";
        String sql = "SELECT " +
                serverUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + serverTable + " on " + serverIDColumn + "=" + Col.SENDER_ID +
                " WHERE " + Col.EXTRA_VARIABLES + "=?" +
                " ORDER BY " + Col.EXPIRY + " LIMIT 1";

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

    @Deprecated
    public void storePlayerOnlineOnThisServer(UUID playerUUID) throws SQLException {
        execute(new ExecStatement(insertStatementNoParts) {
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

    public void storeConfigSettings(String encodedSettingString) throws SQLException {
        execute(new ExecStatement(insertStatementNoParts) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, ServerInfo.getServerUUID().toString());
                statement.setLong(2, MiscUtils.getTime() + TimeAmount.HOUR.ms());
                statement.setString(3, "configSettings");
                statement.setString(4, null);
                statement.setString(5, encodedSettingString);
            }
        });
    }

    public Optional<String> getConfigSettings() throws SQLException {
        return query(new QueryStatement<Optional<String>>(selectStatement, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, "configSettings");
                statement.setLong(2, MiscUtils.getTime());
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

        public String get() {
            return toString();
        }

        @Override
        public String toString() {
            return column;
        }
    }
}