package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Rsl1122
 */
public class VersionTable extends Table {

    /**
     * @param db
     * @param usingMySQL
     */
    public VersionTable(SQLDB db, boolean usingMySQL) {
        super("plan_version", db, usingMySQL);
    }

    /**
     * @return
     */
    @Override
    public boolean createTable() {
        return createTable(TableSqlParser.createTable(tableName)
                .column("version", Sql.INT).notNull()
                .toString()
        );
    }

    public boolean isNewDatabase() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            if (usingMySQL) {
                statement = prepareStatement("SHOW TABLES LIKE ?");
            } else {
                statement = prepareStatement("SELECT tbl_name FROM sqlite_master WHERE tbl_name=?");
            }

            statement.setString(1, tableName);

            set = statement.executeQuery();

            return !set.next();
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    /**
     * @return @throws SQLException
     */
    @Override
    public int getVersion() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            int version = 0;
            statement = prepareStatement("SELECT * FROM " + tableName);
            set = statement.executeQuery();
            if (set.next()) {
                version = set.getInt("version");
            }
            return version;
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            endTransaction(statement);
            close(set, statement);
        }

        return 1;
    }

    /**
     * @param version
     * @throws SQLException
     */
    public void setVersion(int version) throws SQLException {
        removeAllData();
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " (version) VALUES (" + version + ")");

            statement.executeUpdate();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }
}
