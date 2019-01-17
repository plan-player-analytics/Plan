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
package com.djrapitops.plan.system.database.databases.sql.objects;

/**
 * Represents a FOREIGN KEY constraint in a MySQL database.
 *
 * @author Rsl1122
 */
public class ForeignKeyConstraint {

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
