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

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.sql.tables.webuser.RegistrationTable;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;
import static com.djrapitops.plan.storage.database.sql.tables.webuser.RegistrationTable.*;
import static com.djrapitops.plan.storage.database.sql.tables.webuser.RegistrationTable.ID;

/**
 * Removes unique constraint from username column on plan_incomplete_registration table.
 *
 * @author Junie
 */
public class RegistrationTableUsernameUniqueConstraintPatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return (dbType == DBType.SQLITE ? !hasUniqueIndexSqlite() : !hasUniqueIndexMySQL())
                && !hasTable("temp_" + TABLE_NAME);
    }

    private boolean hasUniqueIndexSqlite() {
        return query(new QueryAllStatement<Integer>("PRAGMA index_list(" + TABLE_NAME + ')') {
            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.getInt("unique");
            }
        }) > 1;
    }

    private boolean hasUniqueIndexMySQL() {
        return query(new QueryAllStatement<Integer>("SHOW INDEX" + FROM + TABLE_NAME) {
            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                while (set.next()) {
                    if (set.getString("Key_name").equals("username_unique")) {
                        return 1;
                    }
                }
                return 0;
            }
        }) > 0;
    }

    @Override
    protected void applyPatch() {
        if (!hasTable("temp_" + TABLE_NAME)) {
            renameTable(TABLE_NAME, "temp_" + TABLE_NAME);
        } else {
            dropTable(TABLE_NAME);
        }
        execute(RegistrationTable.createTableSql(dbType));
        execute(INSERT_INTO + TABLE_NAME + "(" +
                ID + ',' +
                USERNAME + ',' +
                SALT_PASSWORD_HASH + ',' +
                CODE + ',' +
                EXPIRY_TIME +
                ")" + SELECT +
                ID + ',' +
                USERNAME + ',' +
                SALT_PASSWORD_HASH + ',' +
                CODE + ',' +
                EXPIRY_TIME +
                FROM + "temp_" + TABLE_NAME);
        dropTable("temp_" + TABLE_NAME);
    }
}
