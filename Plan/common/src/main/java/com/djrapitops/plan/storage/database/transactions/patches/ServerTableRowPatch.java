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
package com.djrapitops.plan.storage.database.transactions.patches;

import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.utilities.java.Lists;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;
import static com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionServerTableValueTable.*;
import static com.djrapitops.plan.storage.database.sql.tables.extension.ExtensionServerTableValueTable.ID;

public class ServerTableRowPatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(TABLE_NAME, TABLE_ROW)
                && tableRowIdsAreUniquePerTableId();
    }

    private boolean tableRowIdsAreUniquePerTableId() {
        String columnCountPerTableSql = SELECT +
                SERVER_UUID + ',' + TABLE_ID + ",COUNT(1) as c" +
                FROM + TABLE_NAME +
                WHERE + TABLE_ROW + "=?" +
                GROUP_BY + TABLE_ID + ',' + SERVER_UUID + lockForUpdate();
        return query(new QueryStatement<>(columnCountPerTableSql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, 0);
            }

            @Override
            public Boolean processResults(ResultSet set) throws SQLException {
                while (set.next()) {
                    if (set.getInt("c") > 1) return false;
                }
                return true;
            }
        });
    }

    @Override
    protected void applyPatch() {
        if (!hasColumn(TABLE_NAME, TABLE_ROW)) {
            addColumn(TABLE_NAME, TABLE_ROW + ' ' + Sql.INT + " NOT NULL DEFAULT 0");
        }

        updateRowIds();
    }

    private void updateRowIds() {
        String updateRowId = "UPDATE " + TABLE_NAME + " SET " +
                TABLE_ROW + "=?" +
                WHERE + ID + "=?";
        for (List<Integer> rowIds : fetchTableRowIds().values()) {
            execute(new ExecBatchStatement(updateRowId) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    for (int i = 0; i < rowIds.size(); i++) {
                        statement.setInt(1, i);
                        statement.setInt(2, rowIds.get(i));
                        statement.addBatch();
                    }
                }
            });
        }
    }

    public Map<Integer, List<Integer>> fetchTableRowIds() {
        String columnCountPerTableSql = SELECT + TABLE_ID + ',' + ID + FROM + TABLE_NAME + lockForUpdate();
        return query(new QueryAllStatement<>(columnCountPerTableSql) {
            @Override
            public Map<Integer, List<Integer>> processResults(ResultSet set) throws SQLException {
                HashMap<Integer, List<Integer>> rowsPerTableId = new HashMap<>();
                while (set.next()) {
                    int tableId = set.getInt(TABLE_ID);
                    int id = set.getInt(ID);
                    List<Integer> ids = rowsPerTableId.computeIfAbsent(tableId, Lists::create);
                    ids.add(id);
                }
                return rowsPerTableId;
            }
        });
    }
}
