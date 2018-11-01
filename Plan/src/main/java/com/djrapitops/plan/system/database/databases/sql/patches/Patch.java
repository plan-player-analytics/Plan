/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.database.databases.sql.patches;

import com.djrapitops.plan.system.database.databases.DBType;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryStatement;
import com.djrapitops.plan.system.database.databases.sql.statements.TableSqlParser;
import com.djrapitops.plan.system.settings.Settings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public abstract class Patch {

    protected final SQLDB db;
    protected final DBType dbType;

    public Patch(SQLDB db) {
        this.db = db;
        this.dbType = db.getType();
    }

    public abstract boolean hasBeenApplied();

    public abstract void apply();

    public <T> T query(QueryStatement<T> query) {
        return db.query(query);
    }

    public boolean hasTable(String tableName) {
        String sql;
        if (dbType == DBType.H2) {
            sql = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME=?";
        } else if (dbType.supportsMySQLQueries()) {
            sql = "SELECT * FROM information_schema.TABLES WHERE table_name=? AND TABLE_SCHEMA=? LIMIT 1";
        } else {
            sql = "SELECT tbl_name FROM sqlite_master WHERE tbl_name=?";
        }

        return query(new QueryStatement<Boolean>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, tableName);
                if (dbType == DBType.H2) {
                    statement.setString(2, db.getConfig().getString(Settings.DB_DATABASE));
                }
            }

            @Override
            public Boolean processResults(ResultSet set) throws SQLException {
                return set.next();
            }
        });
    }

    protected boolean hasColumn(String tableName, String columnName) {
        if (dbType.supportsMySQLQueries()) {
            String query;

            if (dbType == DBType.H2) {
                query = "SELECT * FROM INFORMATION_SCHEMA.COLUMNS" +
                        " WHERE TABLE_NAME=? AND COLUMN_NAME=?";
            } else {
                query = "SELECT * FROM information_schema.COLUMNS" +
                        " WHERE TABLE_NAME=? AND COLUMN_NAME=? AND TABLE_SCHEMA=?";
            }

            return query(new QueryStatement<Boolean>(query) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setString(1, tableName);
                    statement.setString(2, columnName);
                    if (dbType != DBType.H2) {
                        statement.setString(3, db.getConfig().getString(Settings.DB_DATABASE));
                    }
                }

                @Override
                public Boolean processResults(ResultSet set) throws SQLException {
                    return set.next();
                }
            });
        } else {
            return query(new QueryAllStatement<Boolean>("PRAGMA table_info(" + tableName + ")") {
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
    }

    protected void addColumn(String tableName, String columnInfo) {
        db.executeUnsafe("ALTER TABLE " + tableName + " ADD " + (dbType.supportsMySQLQueries() ? "" : "COLUMN ") + columnInfo);
    }

    protected void dropTable(String name) {
        db.executeUnsafe(TableSqlParser.dropTable(name));
    }

    protected void renameTable(String from, String to) {
        String sql = dbType.supportsMySQLQueries() ?
                "RENAME TABLE " + from + " TO " + to :
                "ALTER TABLE " + from + " RENAME TO " + to;
        db.execute(sql);
    }

    protected UUID getServerUUID() {
        return db.getServerUUIDSupplier().get();
    }
}
