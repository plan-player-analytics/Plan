package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Table class representing database table plan_worlds.
 * <p>
 * Used for storing id references to world names.
 *
 * @author Rsl1122
 * @since 3.6.0 / Database version 7
 */
public class WorldTable extends Table {

    public final String statementSelectID;
    private final String columnWorldId = "id";
    private final String columnWorldName = "world_name";
    private final String columnServerID = "server_id";

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
    public void createTable() throws DBCreateTableException {
        createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(usingMySQL, columnWorldId)
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
        try (Connection connection = getConnection()) {
            statement = connection.prepareStatement("SELECT * FROM " + tableName);
            set = statement.executeQuery();
            List<String> worldNames = new ArrayList<>();
            while (set.next()) {
                String worldName = set.getString(columnWorldName);
                worldNames.add(worldName);
            }
            return worldNames;
        } finally {
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
        Set<String> worldsToSave = new HashSet<>(worlds);

        List<String> saved = getWorlds();
        worldsToSave.removeAll(saved);
        if (Verify.isEmpty(worlds)) {
            return;
        }

        PreparedStatement statement = null;
        try (Connection connection = getConnection()) {
            statement = connection.prepareStatement("INSERT INTO " + tableName + " ("
                    + columnWorldName
                    + ") VALUES (?)");
            for (String world : worldsToSave) {
                statement.setString(1, world);
                statement.addBatch();
            }

            statement.executeBatch();
            connection.commit();
        } finally {
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
