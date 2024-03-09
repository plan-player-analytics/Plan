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
package com.djrapitops.plan.storage.database.transactions.init;

import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Transaction for removing duplicate data in plan_user_info.
 * <p>
 * Related issues:
 * <a href="https://github.com/plan-player-analytics/Plan/issues/956">Issue #956</a>
 * <a href="https://github.com/plan-player-analytics/Plan/issues/967">Issue #967</a>
 *
 * @author AuroraLS3
 */
public class RemoveDuplicateUserInfoTransaction extends ThrowawayTransaction {

    private static final String STATEMENT_SELECT_DUPLICATE_IDS =
            SELECT + DISTINCT + "u2." + UserInfoTable.ID + " as id" +
                    FROM + UserInfoTable.TABLE_NAME + " u1" +
                    INNER_JOIN + UserInfoTable.TABLE_NAME + " u2 on " +
                    "u1." + UserInfoTable.USER_ID + "=u2." + UserInfoTable.USER_ID + AND +
                    "u1." + UserInfoTable.SERVER_ID + "=u2." + UserInfoTable.SERVER_ID + AND +
                    "u1." + UserInfoTable.ID + "<u2." + UserInfoTable.ID;

    @Override
    protected void performOperations() {
        Collection<Integer> duplicateIDs = getDuplicates();
        if (duplicateIDs.isEmpty()) return;

        execute(new ExecBatchStatement(DELETE_FROM + UserInfoTable.TABLE_NAME + WHERE + UserInfoTable.ID + "=?") {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Integer id : duplicateIDs) {
                    statement.setInt(1, id);
                    statement.addBatch();
                }
            }
        });
    }

    private Collection<Integer> getDuplicates() {
        return query(new QueryAllStatement<>(STATEMENT_SELECT_DUPLICATE_IDS) {
            @Override
            public Collection<Integer> processResults(ResultSet set) throws SQLException {
                Set<Integer> duplicateIDs = new HashSet<>();
                while (set.next()) {
                    duplicateIDs.add(set.getInt("id"));
                }
                return duplicateIDs;
            }
        });
    }
}