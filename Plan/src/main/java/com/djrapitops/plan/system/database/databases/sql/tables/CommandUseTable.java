package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBCreateTableException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Select;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plan.system.info.server.ServerInfo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Rsl1122
 */
public class CommandUseTable extends Table {

    private final String columnCommandId = "id";
    private final String columnCommand = "command";
    private final String columnTimesUsed = "times_used";
    private final String columnServerID = "server_id";

    private final ServerTable serverTable;
    private String insertStatement;

    public CommandUseTable(SQLDB db) {
        super("plan_commandusages", db);
        serverTable = db.getServerTable();
        insertStatement = "INSERT INTO " + tableName + " ("
                + columnCommand + ", "
                + columnTimesUsed + ", "
                + columnServerID
                + ") VALUES (?, ?, " + serverTable.statementSelectServerID + ")";
    }

    @Override
    public void createTable() throws DBCreateTableException {
        ServerTable serverTable = db.getServerTable();
        createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(usingMySQL, columnCommandId)
                .column(columnCommand, Sql.varchar(20)).notNull()
                .column(columnTimesUsed, Sql.INT).notNull()
                .column(columnServerID, Sql.INT).notNull()
                .primaryKey(usingMySQL, columnCommandId)
                .foreignKey(columnServerID, serverTable.toString(), serverTable.getColumnID())
                .toString()
        );
    }

    /**
     * Used to get all commands used in this server.
     *
     * @return command - times used Map
     * @throws SQLException DB Error
     */
    public Map<String, Integer> getCommandUse() throws SQLException {
        return getCommandUse(ServerInfo.getServerUUID());
    }

    /**
     * Used to get all commands used in a server.
     *
     * @param serverUUID UUID of the server.
     * @return command - times used Map
     * @throws SQLException DB Error
     */
    public Map<String, Integer> getCommandUse(UUID serverUUID) throws SQLException {
        String sql = Select.from(tableName,
                columnCommand, columnTimesUsed)
                .where(columnServerID + "=" + serverTable.statementSelectServerID)
                .toString();

        return query(new QueryStatement<Map<String, Integer>>(sql, 5000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Map<String, Integer> processResults(ResultSet set) throws SQLException {
                Map<String, Integer> commandUse = new HashMap<>();
                while (set.next()) {
                    String cmd = set.getString(columnCommand).toLowerCase();
                    int amountUsed = set.getInt(columnTimesUsed);
                    commandUse.put(cmd, amountUsed);
                }
                return commandUse;
            }
        });
    }

    public void commandUsed(String command) throws SQLException {
        if (command.length() > 20) {
            return;
        }

        String sql = "UPDATE " + tableName + " SET "
                + columnTimesUsed + "=" + columnTimesUsed + "+ 1" +
                " WHERE " + columnServerID + "=" + serverTable.statementSelectServerID +
                " AND " + columnCommand + "=?";

        boolean updated = execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, ServerInfo.getServerUUID().toString());
                statement.setString(2, command);
            }
        });
        if (!updated) {
            insertCommand(command);
        }
    }

    private void insertCommand(String command) throws SQLException {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, command);
                statement.setInt(2, 1);
                statement.setString(3, ServerInfo.getServerUUID().toString());
            }
        });
    }

    public Optional<String> getCommandByID(int id) throws SQLException {
        String sql = Select.from(tableName, columnCommand).where(columnCommandId + "=?").toString();

        return query(new QueryStatement<Optional<String>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, id);
            }

            @Override
            public Optional<String> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getString(columnCommand));
                }
                return Optional.empty();
            }
        });
    }

    public Optional<Integer> getCommandID(String command) throws SQLException {
        String sql = Select.from(tableName, columnCommandId).where(columnCommand + "=?").toString();

        return query(new QueryStatement<Optional<Integer>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, command);
            }

            @Override
            public Optional<Integer> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getInt(columnCommandId));
                }
                return Optional.empty();
            }
        });
    }

    public Map<UUID, Map<String, Integer>> getAllCommandUsages() throws SQLException {
        String serverIDColumn = serverTable + "." + serverTable.getColumnID();
        String serverUUIDColumn = serverTable + "." + serverTable.getColumnUUID() + " as s_uuid";
        String sql = "SELECT " +
                columnCommand + ", " +
                columnTimesUsed + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " JOIN " + serverTable + " on " + serverIDColumn + "=" + columnServerID;

        return query(new QueryAllStatement<Map<UUID, Map<String, Integer>>>(sql, 10000) {
            @Override
            public Map<UUID, Map<String, Integer>> processResults(ResultSet set) throws SQLException {
                Map<UUID, Map<String, Integer>> map = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));

                    Map<String, Integer> serverMap = map.getOrDefault(serverUUID, new HashMap<>());

                    String command = set.getString(columnCommand);
                    int timesUsed = set.getInt(columnTimesUsed);

                    serverMap.put(command, timesUsed);
                    map.put(serverUUID, serverMap);
                }
                return map;
            }
        });
    }

    public void insertCommandUsage(Map<UUID, Map<String, Integer>> allCommandUsages) throws SQLException {
        if (allCommandUsages.isEmpty()) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every Server
                for (UUID serverUUID : allCommandUsages.keySet()) {
                    // Every Command
                    for (Map.Entry<String, Integer> entry : allCommandUsages.get(serverUUID).entrySet()) {
                        String command = entry.getKey();
                        int timesUsed = entry.getValue();

                        statement.setString(1, command);
                        statement.setInt(2, timesUsed);
                        statement.setString(3, serverUUID.toString());
                        statement.addBatch();
                    }
                }
            }
        });
    }
}
