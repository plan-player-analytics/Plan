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

import com.djrapitops.plan.storage.database.queries.schema.SessionIDServerIDRelationQuery;
import com.djrapitops.plan.storage.database.sql.tables.KillsTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * Adds server_id field to kills table.
 * <p>
 * The field is populated by querying the session table for server ids.
 *
 * @author AuroraLS3
 * @see KillsOptimizationPatch for removal of this field later
 */
public class KillsServerIDPatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        String tableName = KillsTable.TABLE_NAME;
        String columnName = "server_id";

        // KillsOptimizationPatch makes this patch incompatible with newer patch versions.
        return hasColumn(tableName, KillsTable.SERVER_UUID)
                || hasColumn(tableName, columnName) && allValuesHaveValueZero(tableName, columnName);
    }

    @Override
    protected void applyPatch() {
        if (hasColumn(KillsTable.TABLE_NAME, KillsTable.SERVER_UUID)) {
            return;
        }

        addColumn(KillsTable.TABLE_NAME, "server_id integer NOT NULL DEFAULT 0");

        Map<Integer, Integer> sessionIDServerIDRelation = query(new SessionIDServerIDRelationQuery());

        String sql = "UPDATE " + KillsTable.TABLE_NAME + " SET server_id=? WHERE " + KillsTable.SESSION_ID + "=?";

        execute(new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<Integer, Integer> entry : sessionIDServerIDRelation.entrySet()) {
                    Integer sessionID = entry.getKey();
                    Integer serverID = entry.getValue();
                    statement.setInt(1, serverID);
                    statement.setInt(2, sessionID);
                    statement.addBatch();
                }
            }
        });
    }
}
