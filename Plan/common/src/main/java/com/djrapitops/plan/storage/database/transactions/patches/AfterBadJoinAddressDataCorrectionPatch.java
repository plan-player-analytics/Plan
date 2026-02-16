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

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.storage.database.queries.objects.JoinAddressQueries;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * @author AuroraLS3
 */
public class AfterBadJoinAddressDataCorrectionPatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return !hasBadAddressIds();
    }

    @Override
    protected void applyPatch() {
        Integer unknownId = getIdOfUnknownJoinAddress();
        updateOldIds(getOldIdsToNewIds(unknownId));
    }

    private void updateOldIds(List<IdRow> idRows) {
        String sql = "UPDATE " + SessionsTable.TABLE_NAME +
                " SET " + SessionsTable.JOIN_ADDRESS_ID + "=?" +
                WHERE + SessionsTable.JOIN_ADDRESS_ID + "=?" +
                AND + SessionsTable.USER_ID + "=?" +
                AND + SessionsTable.SERVER_ID + "=?";
        execute(new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (IdRow idRow : idRows) {
                    statement.setInt(1, idRow.newId);
                    statement.setInt(2, idRow.oldId);
                    statement.setInt(3, idRow.userId);
                    statement.setInt(4, idRow.serverId);
                    statement.addBatch();
                }
            }
        });
    }

    List<IdRow> getOldIdsToNewIds(int unknownId) {
        String sql = SELECT + DISTINCT +
                "s." + SessionsTable.USER_ID + ',' +
                "s." + SessionsTable.SERVER_ID + ',' +
                SessionsTable.JOIN_ADDRESS_ID + " as old_id," +
                "COALESCE(j." + JoinAddressTable.ID + ",?) as new_id" +
                FROM + SessionsTable.TABLE_NAME + " s" +
                LEFT_JOIN + UserInfoTable.TABLE_NAME + " u on " +
                "u." + UserInfoTable.USER_ID + "=s." + SessionsTable.USER_ID +
                AND + "u." + UserInfoTable.SERVER_ID + "=s." + SessionsTable.SERVER_ID +
                LEFT_JOIN + JoinAddressTable.TABLE_NAME + " j on " +
                "j." + JoinAddressTable.JOIN_ADDRESS + "=u." + UserInfoTable.JOIN_ADDRESS + lockForUpdate();

        return query(db -> db.queryList(sql, result -> {
            IdRow idRow = new IdRow();
            idRow.userId = result.getInt(SessionsTable.USER_ID);
            idRow.serverId = result.getInt(SessionsTable.SERVER_ID);
            idRow.oldId = result.getInt("old_id");
            idRow.newId = result.getInt("new_id");
            return idRow;
        }, unknownId));
    }

    private Integer getIdOfUnknownJoinAddress() {
        return query(JoinAddressQueries.getIdOfJoinAddress(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP))
                .orElseThrow(() -> new DBOpException("Could not get ID of join address properly"));
    }

    private boolean hasBadAddressIds() {
        String sql = SELECT + "COUNT(" + DISTINCT + SessionsTable.JOIN_ADDRESS_ID + ") as c" +
                FROM + SessionsTable.TABLE_NAME +
                WHERE + SessionsTable.JOIN_ADDRESS_ID + " NOT IN (" +
                SELECT + JoinAddressTable.ID + FROM + JoinAddressTable.TABLE_NAME + lockForUpdate() +
                ")" + lockForUpdate();
        return query(db -> db.queryOptional(sql, results -> results.getInt("c") > 0))
                .orElse(false);
    }

    private static class IdRow {
        int userId;
        int serverId;
        int oldId;
        int newId;
    }
}
