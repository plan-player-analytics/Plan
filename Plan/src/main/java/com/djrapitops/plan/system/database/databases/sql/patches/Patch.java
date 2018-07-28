package com.djrapitops.plan.system.database.databases.sql.patches;

import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Patch {

    protected final SQLDB db;
    protected final boolean usingMySQL;

    public Patch(SQLDB db) {
        this.db = db;
        usingMySQL = db.isUsingMySQL();
    }

    public abstract boolean hasBeenApplied();

    public abstract void apply();

    public <T> T query(QueryStatement<T> query) {
        return db.query(query);
    }

    public boolean hasTable(String tableName) {
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
                return set.next();
            }
        });
    }

    protected boolean hasColumn(String tableName, String columnName) {
        return usingMySQL ?
                query(new QueryStatement<Boolean>("SHOW COLUMNS FROM ? LIKE ?") {
                    @Override
                    public void prepare(PreparedStatement statement) throws SQLException {
                        statement.setString(1, tableName);
                        statement.setString(2, columnName);
                    }

                    @Override
                    public Boolean processResults(ResultSet set) {
                        return null;
                    }
                }) :
                query(new QueryAllStatement<Boolean>("PRAGMA table_info(" + tableName + ")") {
                    @Override
                    public Boolean processResults(ResultSet set) throws SQLException {
                        while (set.next()) {
                            if (columnName.equals(set.getString("name"))) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
    }

    protected void addColumns(String tableName, String... columnInfo) {
        for (int i = 0; i < columnInfo.length; i++) {
            columnInfo[i] = "ALTER TABLE " + tableName + " ADD " + (usingMySQL ? "" : "COLUMN ") + columnInfo[i];
        }
        db.executeUnsafe(columnInfo);
    }

    protected void dropTable(String name) {
        String sql = "DROP TABLE " + name;
        db.executeUnsafe(sql);
    }

    protected void renameTable(String from, String to) {
        String sql = usingMySQL ?
                "RENAME TABLE " + from + " TO " + to :
                "ALTER TABLE " + from + " RENAME TO " + to;
        db.execute(sql);
    }

}
