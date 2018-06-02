package com.djrapitops.plan.system.database.databases.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.Sql;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Rsl1122
 */
public class VersionTable extends Table {

    public VersionTable(SQLDB db) {
        super("plan_version", db);
    }

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .column("version", Sql.INT).notNull()
                .toString()
        );
    }

    public boolean isNewDatabase() {
        String sql = usingMySQL ?
                "SHOW TABLES LIKE ?" :
                "SELECT tbl_name FROM sqlite_master WHERE tbl_name=?";

        return query(new QueryStatement<Boolean>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, tableName);
            }

            @Override
            public Boolean processResults(ResultSet set) throws SQLException {
                return !set.next();
            }
        });
    }

    /**
     * @return @throws SQLException
     */
    @Override
    public int getVersion() {
        String sql = "SELECT * FROM " + tableName;

        return query(new QueryAllStatement<Integer>(sql) {
            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                int version = 0;
                if (set.next()) {
                    version = set.getInt("version");
                }
                return version;
            }
        });
    }

    /**
     * Set the DB Schema version
     *
     * @param version DB Schema version
     */
    public void setVersion(int version) {
        removeAllData();

        String sql = "INSERT INTO " + tableName + " (version) VALUES (?)";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, version);
            }
        });
    }
}
