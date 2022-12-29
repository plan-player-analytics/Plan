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
import com.djrapitops.plan.storage.database.queries.QueryParameterSetter;
import com.djrapitops.plan.storage.database.queries.objects.JoinAddressQueries;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.events.StoreJoinAddressTransaction;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * @author AuroraLS3
 */
public class BadJoinAddressDataCorrectionPatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return !hasBadAddressIds();
    }

    @Override
    protected void applyPatch() {
        Set<Integer> removeIds = new HashSet<>();
        Map<Integer, Integer> oldToNewIds = new HashMap<>();
        Map<String, Integer> newIds = new HashMap<>();

        Map<String, Integer> badAddressIds = getBadAddressIds();
        for (Map.Entry<String, Integer> entry : badAddressIds.entrySet()) {
            String badAddress = entry.getKey();
            Integer oldId = entry.getValue();
            String correctedAddress = StringUtils.split(badAddress, '\u0000')[0];

            Integer newIdStored = newIds.get(correctedAddress);
            int newId = newIdStored == null ? getOrAddCorrectAddressId(correctedAddress) : newIdStored;
            newIds.put(correctedAddress, newId);
            oldToNewIds.put(oldId, newId);
            removeIds.add(oldId);
        }
        updateOldIds(oldToNewIds);
        deleteOldIds(removeIds);
    }

    private void deleteOldIds(Set<Integer> removeIds) {
        String sql = DELETE_FROM + JoinAddressTable.TABLE_NAME +
                WHERE + JoinAddressTable.ID + " IN (" + Sql.nParameters(removeIds.size()) + ")";
        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                QueryParameterSetter.setParameters(statement, removeIds);
            }
        });
    }

    private void updateOldIds(Map<Integer, Integer> oldToNewIds) {
        String sql = "UPDATE " + SessionsTable.TABLE_NAME +
                " SET " + SessionsTable.JOIN_ADDRESS_ID + "=?" +
                WHERE + SessionsTable.JOIN_ADDRESS_ID + "=?";
        execute(new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<Integer, Integer> entry : oldToNewIds.entrySet()) {
                    Integer newId = entry.getValue();
                    Integer oldId = entry.getKey();
                    statement.setInt(1, newId);
                    statement.setInt(2, oldId);
                    statement.addBatch();
                }
            }
        });
    }

    private int getOrAddCorrectAddressId(String correctedAddress) {
        return query(JoinAddressQueries.getIdOfJoinAddress(correctedAddress))
                .orElseGet(() -> storeAndGetIdOfNewAddress(correctedAddress));
    }

    private Integer storeAndGetIdOfNewAddress(String correctedAddress) {
        StoreJoinAddressTransaction store = new StoreJoinAddressTransaction(correctedAddress);
        executeOther(store);
        return store.getNewId().orElseGet(this::getIdOfUnknownJoinAddress);
    }

    private Integer getIdOfUnknownJoinAddress() {
        return query(JoinAddressQueries.getIdOfJoinAddress(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP))
                .orElseThrow(() -> new DBOpException("Could not get ID of join address properly"));
    }

    private boolean hasBadAddressIds() {
        String sql = SELECT + "COUNT(*) as c" +
                FROM + JoinAddressTable.TABLE_NAME +
                WHERE + "INSTR(" + JoinAddressTable.JOIN_ADDRESS + ", CHAR(0))";
        return query(db -> db.queryOptional(sql, results -> results.getInt("c") > 0))
                .orElse(false);
    }

    private Map<String, Integer> getBadAddressIds() {
        String sql = SELECT + JoinAddressTable.ID + ',' +
                JoinAddressTable.JOIN_ADDRESS +
                FROM + JoinAddressTable.TABLE_NAME +
                WHERE + "INSTR(" + JoinAddressTable.JOIN_ADDRESS + ", CHAR(0))";
        return query(db -> db.queryMap(sql, (results, map) -> map.put(results.getString(JoinAddressTable.JOIN_ADDRESS),
                results.getInt(JoinAddressTable.ID))));
    }
}
