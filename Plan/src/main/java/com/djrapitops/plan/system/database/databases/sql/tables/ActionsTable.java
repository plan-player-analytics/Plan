/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.Actions;
import com.djrapitops.plan.data.container.Action;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Column;
import com.djrapitops.plan.system.database.databases.sql.statements.Select;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table that is in charge of storing actions.
 *
 * Table Name: plan_actions
 *
 * For contained columns {@see Col}
 *
 * @author Rsl1122
 * @see Action
 */
public class ActionsTable extends UserIDTable {

    public ActionsTable(SQLDB db) {
        super("plan_actions", db);
        serverTable = db.getServerTable();
        insertStatement = "INSERT INTO " + tableName + " ("
                + Col.USER_ID + ", "
                + Col.SERVER_ID + ", "
                + Col.ACTION_ID + ", "
                + Col.DATE + ", "
                + Col.ADDITIONAL_INFO
                + ") VALUES ("
                + usersTable.statementSelectID + ", "
                + serverTable.statementSelectServerID + ", "
                + "?, ?, ?)";
    }

    private final ServerTable serverTable;
    private String insertStatement;

    @Override
    public void createTable() throws DBInitException {
        ServerTable serverTable = db.getServerTable();
        createTable(TableSqlParser.createTable(tableName)
                .column(Col.USER_ID, Sql.INT).notNull()
                .column(Col.SERVER_ID, Sql.INT).notNull()
                .column(Col.DATE, Sql.LONG).notNull()
                .column(Col.ACTION_ID, Sql.INT).notNull()
                .column(Col.ADDITIONAL_INFO, Sql.varchar(300))
                .foreignKey(Col.USER_ID, usersTable.toString(), UsersTable.Col.ID)
                .foreignKey(Col.SERVER_ID, serverTable.toString(), ServerTable.Col.SERVER_ID)
                .toString());
    }

    public void alterTableV12() {
        if (usingMySQL) {
            executeUnsafe("ALTER TABLE " + tableName + " MODIFY " + Col.ADDITIONAL_INFO + " VARCHAR(300)");
        }
    }

    /**
     * Used to get all Actions done by a user on this server.
     *
     * @param uuid UUID of the player
     * @return List of actions done by the player. Does not include the kills.
     * @throws SQLException DB Error
     */
    public List<Action> getActions(UUID uuid) throws SQLException {
        String sql = Select.all(tableName)
                .where(Col.USER_ID + "=" + usersTable.statementSelectID)
                .toString();

        return query(new QueryStatement<List<Action>>(sql, 5000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public List<Action> processResults(ResultSet set) throws SQLException {
                List<Action> actions = new ArrayList<>();
                while (set.next()) {
                    int serverID = set.getInt(Col.SERVER_ID.get());
                    long date = set.getLong(Col.DATE.get());
                    Actions doneAction = Actions.getById(set.getInt(Col.ACTION_ID.get()));
                    String additionalInfo = set.getString(Col.ADDITIONAL_INFO.get());
                    actions.add(new Action(date, doneAction, additionalInfo, serverID));
                }
                return actions;
            }
        });
    }

    public void insertAction(UUID uuid, Action action) throws SQLException {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setString(2, ServerInfo.getServerUUID().toString());
                statement.setInt(3, action.getDoneAction().getId());
                statement.setLong(4, action.getDate());
                statement.setString(5, action.getAdditionalInfo());
            }
        });
    }

    public Map<UUID, Map<UUID, List<Action>>> getAllActions() throws SQLException {
        String usersIDColumn = usersTable + "." + UsersTable.Col.ID;
        String usersUUIDColumn = usersTable + "." + UsersTable.Col.UUID + " as uuid";
        String serverIDColumn = serverTable + "." + ServerTable.Col.SERVER_ID;
        String serverUUIDColumn = serverTable + "." + ServerTable.Col.SERVER_UUID + " as s_uuid";
        String sql = "SELECT " +
                Col.ACTION_ID + ", " +
                Col.DATE + ", " +
                Col.ADDITIONAL_INFO + ", " +
                usersUUIDColumn + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersIDColumn + "=" + Col.USER_ID +
                " INNER JOIN " + serverTable + " on " + serverIDColumn + "=" + Col.SERVER_ID;

        return query(new QueryAllStatement<Map<UUID, Map<UUID, List<Action>>>>(sql, 20000) {
            @Override
            public Map<UUID, Map<UUID, List<Action>>> processResults(ResultSet set) throws SQLException {
                Map<UUID, Map<UUID, List<Action>>> map = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));
                    UUID uuid = UUID.fromString(set.getString("uuid"));

                    Map<UUID, List<Action>> serverMap = map.getOrDefault(serverUUID, new HashMap<>());
                    List<Action> actions = serverMap.getOrDefault(uuid, new ArrayList<>());

                    long date = set.getLong(Col.DATE.get());
                    Actions doneAction = Actions.getById(set.getInt(Col.ACTION_ID.get()));
                    String additionalInfo = set.getString(Col.ADDITIONAL_INFO.get());

                    actions.add(new Action(date, doneAction, additionalInfo, -1));

                    serverMap.put(uuid, actions);
                    map.put(serverUUID, serverMap);
                }
                return map;
            }
        });
    }

    public Map<UUID, List<Action>> getServerActions(UUID serverUUID) throws SQLException {
        String usersIDColumn = usersTable + "." + UsersTable.Col.ID;
        String usersUUIDColumn = usersTable + "." + UsersTable.Col.UUID + " as uuid";
        String sql = "SELECT " +
                Col.ACTION_ID + ", " +
                Col.DATE + ", " +
                Col.ADDITIONAL_INFO + ", " +
                usersUUIDColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersIDColumn + "=" + Col.USER_ID +
                " WHERE " + serverTable.statementSelectServerID + "=" + Col.SERVER_ID;

        return query(new QueryStatement<Map<UUID, List<Action>>>(sql, 20000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Map<UUID, List<Action>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<Action>> map = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString("uuid"));

                    List<Action> actions = map.getOrDefault(uuid, new ArrayList<>());

                    long date = set.getLong(Col.DATE.get());
                    Actions doneAction = Actions.getById(set.getInt(Col.ACTION_ID.get()));
                    String additionalInfo = set.getString(Col.ADDITIONAL_INFO.get());

                    actions.add(new Action(date, doneAction, additionalInfo, -1));

                    map.put(uuid, actions);
                }
                return map;
            }
        });
    }

    public void insertActions(Map<UUID, Map<UUID, List<Action>>> allActions) throws SQLException {
        if (Verify.isEmpty(allActions)) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (UUID serverUUID : allActions.keySet()) {
                    // Every User
                    for (Map.Entry<UUID, List<Action>> entry : allActions.get(serverUUID).entrySet()) {
                        UUID uuid = entry.getKey();
                        // Every Action
                        List<Action> actions = entry.getValue();
                        for (Action action : actions) {
                            statement.setString(1, uuid.toString());
                            statement.setString(2, serverUUID.toString());
                            statement.setInt(3, action.getDoneAction().getId());
                            statement.setLong(4, action.getDate());
                            statement.setString(5, action.getAdditionalInfo());
                            statement.addBatch();
                        }
                    }
                }
            }
        });
    }

    public enum Col implements Column {
        USER_ID(UserIDTable.Col.USER_ID.get()),
        SERVER_ID("server_id"),
        DATE("date"),
        ACTION_ID("action_id"),
        ADDITIONAL_INFO("additional_info");

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