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
package com.djrapitops.plan.db.patches;

import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.ExecBatchStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.access.queries.schema.SessionIDServerIDRelationQuery;
import com.djrapitops.plan.db.sql.tables.KillsTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class KillsServerIDPatch extends Patch {

    public KillsServerIDPatch(SQLDB db) {
        super(db);
    }

    @Override
    public boolean hasBeenApplied() {
        String tableName = KillsTable.TABLE_NAME;
        String columnName = "server_id";

        // KillsOptimizationPatch makes this patch incompatible with newer patch versions.
        return hasColumn(tableName, KillsTable.SERVER_UUID)
                || (hasColumn(tableName, columnName) && allValuesHaveServerID(tableName, columnName));
    }

    private Boolean allValuesHaveServerID(String tableName, String columnName) {
        String sql = "SELECT * FROM " + tableName + " WHERE " + columnName + "=? LIMIT 1";
        return query(new QueryStatement<Boolean>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, 0);
            }

            @Override
            public Boolean processResults(ResultSet set) throws SQLException {
                return !set.next();
            }
        });
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
