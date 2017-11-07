/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.data.Action;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.processing.ExecStatement;
import main.java.com.djrapitops.plan.database.processing.QueryAllStatement;
import main.java.com.djrapitops.plan.database.processing.QueryStatement;
import main.java.com.djrapitops.plan.database.sql.Select;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table that is in charge of storing actions.
 * <p>
 * plan_actions contains columns:
 * <ul>
 * <li>user_id (plan_users: id)</li>
 * <li>server_id (plan_servers: id)</li>
 * <li>action_id</li>
 * <li>date</li>
 * <li>additional_info</li>
 * </ul>
 *
 * @author Rsl1122
 */
public class ActionsTable extends UserIDTable {

    private final String columnServerID = "server_id";
    private final String columnDate = "date";
    private final String columnActionID = "action_id";
    private final String columnAdditionalInfo = "additional_info";

    private final ServerTable serverTable;
    private String insertStatement;

    public ActionsTable(SQLDB db, boolean usingMySQL) {
        super("plan_actions", db, usingMySQL);
        serverTable = db.getServerTable();
        insertStatement = "INSERT INTO " + tableName + " ("
                + columnUserID + ", "
                + columnServerID + ", "
                + columnActionID + ", "
                + columnDate + ", "
                + columnAdditionalInfo
                + ") VALUES ("
                + usersTable.statementSelectID + ", "
                + serverTable.statementSelectServerID + ", "
                + "?, ?, ?)";
    }

    @Override
    public void createTable() throws DBCreateTableException {
        ServerTable serverTable = db.getServerTable();
        createTable(TableSqlParser.createTable(tableName)
                .column(columnUserID, Sql.INT).notNull()
                .column(columnServerID, Sql.INT).notNull()
                .column(columnDate, Sql.LONG).notNull()
                .column(columnActionID, Sql.INT).notNull()
                .column(columnAdditionalInfo, Sql.varchar(300))
                .foreignKey(columnUserID, usersTable.toString(), usersTable.getColumnID())
                .foreignKey(columnServerID, serverTable.toString(), serverTable.getColumnID())
                .toString());
    }

    public void alterTableV12() {
        if (usingMySQL) {
            executeUnsafe("ALTER TABLE " + tableName + " MODIFY " + columnAdditionalInfo + " VARCHAR(300)");
        }
    }

    public void insertAction(UUID uuid, Action action) throws SQLException {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setString(2, Plan.getServerUUID().toString());
                statement.setInt(3, action.getDoneAction().getId());
                statement.setLong(4, action.getDate());
                statement.setString(5, action.getAdditionalInfo());
            }
        });
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
                .where(columnUserID + "=" + usersTable.statementSelectID)
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
                    int serverID = set.getInt(columnServerID);
                    long date = set.getLong(columnDate);
                    Actions doneAction = Actions.getById(set.getInt(columnActionID));
                    String additionalInfo = set.getString(columnAdditionalInfo);
                    actions.add(new Action(date, doneAction, additionalInfo, serverID));
                }
                return actions;
            }
        });
    }

    public Map<UUID, Map<UUID, List<Action>>> getAllActions() throws SQLException {
        String usersIDColumn = usersTable + "." + usersTable.getColumnID();
        String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as uuid";
        String serverIDColumn = serverTable + "." + serverTable.getColumnID();
        String serverUUIDColumn = serverTable + "." + serverTable.getColumnUUID() + " as s_uuid";
        String sql = "SELECT " +
                columnActionID + ", " +
                columnDate + ", " +
                columnAdditionalInfo + ", " +
                usersUUIDColumn + ", " +
                serverUUIDColumn +
                " FROM " + tableName +
                " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID +
                " JOIN " + serverTable + " on " + serverIDColumn + "=" + columnServerID;

        return query(new QueryAllStatement<Map<UUID, Map<UUID, List<Action>>>>(sql, 20000) {
            @Override
            public Map<UUID, Map<UUID, List<Action>>> processResults(ResultSet set) throws SQLException {
                Map<UUID, Map<UUID, List<Action>>> map = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));
                    UUID uuid = UUID.fromString(set.getString("uuid"));

                    Map<UUID, List<Action>> serverMap = map.getOrDefault(serverUUID, new HashMap<>());
                    List<Action> actions = serverMap.getOrDefault(uuid, new ArrayList<>());

                    long date = set.getLong(columnDate);
                    Actions doneAction = Actions.getById(set.getInt(columnActionID));
                    String additionalInfo = set.getString(columnAdditionalInfo);

                    actions.add(new Action(date, doneAction, additionalInfo, -1));

                    serverMap.put(uuid, actions);
                    map.put(serverUUID, serverMap);
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
}