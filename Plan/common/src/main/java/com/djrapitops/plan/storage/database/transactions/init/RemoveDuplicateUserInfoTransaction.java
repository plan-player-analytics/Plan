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

import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;

import static com.djrapitops.plan.storage.database.sql.parsing.Sql.*;

/**
 * Transaction for removing duplicate data in plan_user_info.
 * <p>
 * https://github.com/plan-player-analytics/Plan/issues/956
 * https://github.com/plan-player-analytics/Plan/issues/967
 *
 * @author Rsl1122
 */
public class RemoveDuplicateUserInfoTransaction extends ThrowawayTransaction {

    private static final String COLUMN_ID = UserInfoTable.TABLE_NAME + '.' + UserInfoTable.ID;
    private static final String STATEMENT_SELECT_DUPLICATE_IDS =
            SELECT + "MIN(" + COLUMN_ID + ") as id" + FROM + UserInfoTable.TABLE_NAME +
                    GROUP_BY + UserInfoTable.USER_UUID + ',' + UserInfoTable.SERVER_UUID;

    @Override
    protected void performOperations() {
        execute(
                "DELETE" + FROM + UserInfoTable.TABLE_NAME +
                        WHERE + COLUMN_ID +
                        // Nested query here is required because MySQL limits update statements with nested queries:
                        // The nested query creates a temporary table that bypasses the same table query-update limit.
                        // Note: MySQL versions 5.6.7+ might optimize this nested query away leading to an exception.
                        " NOT IN (" + SELECT + "id" + FROM + '(' + STATEMENT_SELECT_DUPLICATE_IDS + ") as ids)"
        );
    }
}