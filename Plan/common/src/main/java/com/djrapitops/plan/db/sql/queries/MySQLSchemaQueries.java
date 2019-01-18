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
package com.djrapitops.plan.db.sql.queries;

import com.djrapitops.plan.db.access.QueryStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that contains different SELECT statements.
 *
 * @author Rsl1122
 */
public class MySQLSchemaQueries {

    private MySQLSchemaQueries() {
        /* Static method class */
    }

    public static QueryStatement<List<ForeignKeyConstraint>> foreignKeyConstraintsOf(String tableSchema, String referencedTable) {
        String keySQL = "SELECT TABLE_NAME,COLUMN_NAME,CONSTRAINT_NAME,REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE" +
                " WHERE REFERENCED_TABLE_SCHEMA = ?" +
                " AND REFERENCED_TABLE_NAME = ?";
        return new QueryStatement<List<ForeignKeyConstraint>>(keySQL) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, tableSchema);
                statement.setString(2, referencedTable);
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

    public static QueryStatement<Boolean> doesIndexExist(String indexName, String tableName) {
        String sql = "SELECT COUNT(1) as IndexIsThere FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE table_schema=DATABASE() AND table_name=? AND index_name=?";
        return new QueryStatement<Boolean>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, tableName);
                statement.setString(2, indexName);
            }

            @Override
            public Boolean processResults(ResultSet set) throws SQLException {
                return set.next() && set.getInt("IndexIsThere") > 0;
            }
        };
    }

    /**
     * Represents a FOREIGN KEY constraint in a MySQL database.
     *
     * @author Rsl1122
     */
    public static class ForeignKeyConstraint {

        private final String table;
        private final String referencedTable;
        private final String column;
        private final String refrencedColumn;
        private final String constraintName;

        public ForeignKeyConstraint(
                String table, String referencedTable,
                String column, String refrencedColumn,
                String constraintName
        ) {
            this.table = table;
            this.referencedTable = referencedTable;
            this.column = column;
            this.refrencedColumn = refrencedColumn;
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

        public String getRefrencedColumn() {
            return refrencedColumn;
        }

        public String getConstraintName() {
            return constraintName;
        }

        @Override
        public String toString() {
            return "FK '" + constraintName + "' " +
                    table + "." + column +
                    " references " +
                    referencedTable + "." + refrencedColumn;
        }
    }
}
