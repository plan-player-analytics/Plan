package com.djrapitops.plan.db.access.transactions.init;

import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.db.sql.tables.UserInfoTable;

import static com.djrapitops.plan.db.sql.parsing.Sql.*;

/**
 * Transaction for removing duplicate data in plan_user_info.
 * <p>
 * https://github.com/plan-player-analytics/Plan/issues/956
 * https://github.com/plan-player-analytics/Plan/issues/967
 *
 * @author Rsl1122
 */
public class RemoveDuplicateUserInfoTransaction extends Transaction {

    private static final String COLUMN_ID = UserInfoTable.TABLE_NAME + '.' + UserInfoTable.ID;
    private static final String STATEMENT_SELECT_DUPLICATE_IDS =
            SELECT + "MIN(" + COLUMN_ID + ") as id" + FROM + UserInfoTable.TABLE_NAME +
                    GROUP_BY + UserInfoTable.USER_UUID + ", " + UserInfoTable.SERVER_UUID;

    @Override
    protected void performOperations() {
        execute(
                "DELETE" + FROM + UserInfoTable.TABLE_NAME +
                        WHERE + COLUMN_ID +
                        " NOT IN (" + STATEMENT_SELECT_DUPLICATE_IDS + ')'
        );
    }
}