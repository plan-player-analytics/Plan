/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.Action;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Select;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public ActionsTable(SQLDB db, boolean usingMySQL) {
        super("plan_actions", db, usingMySQL);
    }

    @Override
    public boolean createTable() {
        ServerTable serverTable = db.getServerTable();
        return createTable(TableSqlParser.createTable(tableName)
                .column(columnUserID, Sql.INT).notNull()
                .column(columnServerID, Sql.INT).notNull()
                .column(columnDate, Sql.LONG).notNull()
                .column(columnActionID, Sql.INT).notNull()
                .column(columnAdditionalInfo, Sql.varchar(100))
                .foreignKey(columnUserID, usersTable.toString(), usersTable.getColumnID())
                .foreignKey(columnServerID, serverTable.toString(), serverTable.getColumnID())
                .toString());
    }

    public void insertAction(UUID uuid, Action action) throws SQLException {
        PreparedStatement statement = null;
        try {
            ServerTable serverTable = db.getServerTable();
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnUserID + ", "
                    + columnServerID + ", "
                    + columnActionID + ", "
                    + columnDate + ", "
                    + columnAdditionalInfo
                    + ") VALUES ("
                    + usersTable.statementSelectID + ", "
                    + serverTable.statementSelectServerID + ", "
                    + "?, ?, ?)"
            );
            statement.setString(1, uuid.toString());
            statement.setString(2, Plan.getServerUUID().toString());
            statement.setInt(3, action.getDoneAction().getId());
            statement.setLong(4, action.getDate());
            statement.setString(5, action.getAdditionalInfo());
            statement.execute();
        } finally {
            endTransaction(statement);
            close(statement);
        }
    }

    /**
     * Used to get all Actions done by a user on this server.
     *
     * @param uuid
     * @return
     * @throws SQLException
     */
    public List<Action> getActions(UUID uuid) throws SQLException {
        List<Action> actions = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            ServerTable serverTable = db.getServerTable();
            statement = prepareStatement(Select.from(tableName, "*")
                    .where(columnUserID + "=" + usersTable.statementSelectID)
                    .toString());
            set = statement.executeQuery();
            while (set.next()) {
                int serverID = set.getInt(columnServerID);
                long date = set.getLong(columnDate);
                Actions doneAction = Actions.getById(set.getInt(columnActionID));
                String additionalInfo = set.getString(columnAdditionalInfo);
                actions.add(new Action(date, doneAction, additionalInfo, serverID));
            }
            return actions;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }
}