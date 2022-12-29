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
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

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
        Set<Integer> badAddressIds = getBadAddressIds();
        Integer unknownId = getIdOfUnknownJoinAddress();
        updateOldIds(badAddressIds, unknownId);
    }

    private void updateOldIds(Set<Integer> badAddressIds, Integer unknownId) {
        String sql = "UPDATE " + SessionsTable.TABLE_NAME +
                " SET " + SessionsTable.JOIN_ADDRESS_ID + "=?" +
                WHERE + SessionsTable.JOIN_ADDRESS_ID + "=?";
        execute(new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Integer oldId : badAddressIds) {
                    statement.setInt(1, unknownId);
                    statement.setInt(2, oldId);
                    statement.addBatch();
                }
            }
        });
    }

    private Integer getIdOfUnknownJoinAddress() {
        return query(JoinAddressQueries.getIdOfJoinAddress(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP))
                .orElseThrow(() -> new DBOpException("Could not get ID of join address properly"));
    }

    private boolean hasBadAddressIds() {
        String sql = SELECT + "COUNT(" + DISTINCT + SessionsTable.JOIN_ADDRESS_ID + ") as c" +
                FROM + SessionsTable.TABLE_NAME +
                WHERE + SessionsTable.JOIN_ADDRESS_ID + " NOT IN (" +
                SELECT + JoinAddressTable.ID + FROM + JoinAddressTable.TABLE_NAME +
                ")";
        return query(db -> db.queryOptional(sql, results -> results.getInt("c") > 0))
                .orElse(false);
    }

    private Set<Integer> getBadAddressIds() {
        String sql = SELECT + DISTINCT + SessionsTable.JOIN_ADDRESS_ID +
                FROM + SessionsTable.TABLE_NAME +
                WHERE + SessionsTable.JOIN_ADDRESS_ID + " NOT IN (" +
                SELECT + JoinAddressTable.ID + FROM + JoinAddressTable.TABLE_NAME +
                ")";
        return query(db -> db.querySet(sql, results -> results.getInt(SessionsTable.JOIN_ADDRESS_ID)));
    }
}
