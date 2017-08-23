package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Table class representing database table plan_worlds.
 * <p>
 * Used for storing id references to world names.
 *
 * @author Rsl1122
 * @since 3.6.0 / Database version 7
 */
public class WorldTable extends Table {

    private final String columnWorldId = "id";
    private final String columnWorldName = "world_name";
    private final String columnServerID = "server_id";

    public final String statementSelectID;

    /**
     * Constructor.
     *
     * @param db         Database this table is a part of.
     * @param usingMySQL Database is a MySQL database.
     */
    public WorldTable(SQLDB db, boolean usingMySQL) {
        super("plan_worlds", db, usingMySQL);
        statementSelectID = "(SELECT " + columnWorldId + " FROM " + tableName + " WHERE (" + columnWorldName + "=?))";
    }

    @Override
    public boolean createTable() {
        return createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(usingMySQL, columnWorldId, Sql.INT)
                .column(columnWorldName, Sql.varchar(100)).notNull()
                .primaryKey(usingMySQL, columnWorldId)
                .toString()
        );
    }

    /**
     * Used to get the available world names.
     *
     * @return List of all world names in the database.
     * @throws SQLException Database error occurs.
     */
    public List<String> getWorlds() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName);
            set = statement.executeQuery();
            List<String> worldNames = new ArrayList<>();
            while (set.next()) {
                String worldName = set.getString(columnWorldName);
                worldNames.add(worldName);
            }
            return worldNames;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    /**
     * Used to save a list of world names.
     * <p>
     * Already saved names will not be saved.
     *
     * @param worlds List of world names.
     * @throws SQLException Database error occurs.
     */
    public void saveWorlds(Collection<String> worlds) throws SQLException {
        Verify.nullCheck(worlds);

        List<String> saved = getWorlds();
        worlds.removeAll(saved);
        if (Verify.isEmpty(worlds)) {
            return;
        }

        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnWorldName
                    + ") VALUES (?)");
            for (String world : worlds) {
                statement.setString(1, world);
                statement.addBatch();
            }

            statement.executeBatch();
        } finally {
            endTransaction(statement);
            close(statement);
        }
    }

    public String getColumnID() {
        return columnWorldId;
    }

    public String getColumnWorldName() {
        return columnWorldName;
    }


}
