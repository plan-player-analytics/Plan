/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.storage.database.queries.schema;

import com.djrapitops.plan.storage.database.queries.HasMoreThanZeroQueryStatement;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import org.intellij.lang.annotations.Language;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Static method class for MySQL Schema related queries.
 *
 * @author AuroraLS3
 */
public class MySQLSchemaQueries {

    private MySQLSchemaQueries() {
        /* Static method class */
    }

    public static Query<Optional<String>> getVersion() {
        @Language("MySQL")
        String sql = "SELECT VERSION()";
        return db -> db.queryOptional(sql, row -> row.getString(1));
    }

    public static Query<Boolean> doesTableExist(String tableName) {
        String sql = SELECT + "COUNT(1) as c FROM information_schema.TABLES WHERE table_name=? AND TABLE_SCHEMA=DATABASE()";
        return new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, tableName);
            }
        };
    }

    public static Query<List<ForeignKeyConstraint>> foreignKeyConstraintsOf(String referencedTable) {
        String keySQL = SELECT + "TABLE_NAME,COLUMN_NAME,CONSTRAINT_NAME,REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME" +
                FROM + "INFORMATION_SCHEMA.KEY_COLUMN_USAGE" +
                WHERE + "REFERENCED_TABLE_SCHEMA = DATABASE()" +
                AND + "REFERENCED_TABLE_NAME = ?";
        return new QueryStatement<>(keySQL) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, referencedTable);
            }

            @Override
            public List<ForeignKeyConstraint> processResults(ResultSet set) throws SQLException {
                List<ForeignKeyConstraint> constraints = new ArrayList<>();

                while (set.next()) {
                    String table = set.getString("TABLE_NAME");
                    String referencedTable = set.getString("REFERENCED_TABLE_NAME");
                    String column = set.getString("COLUMN_NAME");
                    String referencedColumn = set.getString("REFERENCED_COLUMN_NAME");
                    String constraintName = set.getString("CONSTRAINT_NAME");

                    constraints.add(new ForeignKeyConstraint(
                            table, referencedTable,
                            column, referencedColumn,
                            constraintName
                    ));
                }

                return constraints;
            }
        };
    }

    public static Query<Boolean> doesIndexExist(String indexName, String tableName) {
        String sql = SELECT + "COUNT(1) as c" +
                FROM + "INFORMATION_SCHEMA.STATISTICS" +
                WHERE + "table_schema=DATABASE()" + AND + "table_name=?" + AND + "index_name=?";
        return new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, tableName);
                statement.setString(2, indexName);
            }
        };
    }

    public static Query<Boolean> doesColumnExist(String tableName, String columnName) {
        String sql = SELECT + "COUNT(1) as c" +
                FROM + "information_schema.COLUMNS" +
                WHERE + "TABLE_NAME=? AND COLUMN_NAME=? AND TABLE_SCHEMA=DATABASE()";
        return new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, tableName);
                statement.setString(2, columnName);
            }
        };
    }

    public static Query<Integer> columnVarcharLength(String table, String column) {
        String sql = SELECT + "CHARACTER_MAXIMUM_LENGTH" +
                FROM + "information_schema.COLUMNS" +
                WHERE + "TABLE_NAME=? AND COLUMN_NAME=? AND TABLE_SCHEMA=DATABASE()";

        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, table);
                statement.setString(2, column);
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt("CHARACTER_MAXIMUM_LENGTH") : Integer.MAX_VALUE;
            }
        };
    }

    /**
     * Represents a FOREIGN KEY constraint in a MySQL database.
     *
     * @author AuroraLS3
     */
    public static class ForeignKeyConstraint {

        private final String table;
        private final String referencedTable;
        private final String column;
        private final String referencedColumn;
        private final String constraintName;

        public ForeignKeyConstraint(
                String table, String referencedTable,
                String column, String referencedColumn,
                String constraintName
        ) {
            this.table = table;
            this.referencedTable = referencedTable;
            this.column = column;
            this.referencedColumn = referencedColumn;
            this.constraintName = constraintName;
        }

        public String getTable() {
            return table;
        }

        public String getReferencedTable() {
            return referencedTable;
        }

        public String getColumn() {
            return column;
        }

        public String getReferencedColumn() {
            return referencedColumn;
        }

        public String getConstraintName() {
            return constraintName;
        }

        @Override
        public String toString() {
            return "FK '" + constraintName + "' " +
                    table + '.' + column +
                    " references " +
                    referencedTable + '.' + referencedColumn;
        }
    }
}
