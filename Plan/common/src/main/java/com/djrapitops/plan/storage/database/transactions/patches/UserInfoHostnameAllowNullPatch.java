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

import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import static com.djrapitops.plan.storage.database.sql.building.Sql.FROM;

/**
 * Make sure hostname can be null.
 *
 * @author AuroraLS3
 */
public class UserInfoHostnameAllowNullPatch extends Patch {

    private final String tempTableName = "temp_user_info_join_address_patching";
    private final String tableName = UserInfoTable.TABLE_NAME;

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(tableName, UserInfoTable.JOIN_ADDRESS)
                && !hasColumn(tableName, "hostname")
                && !hasTable(tempTableName);
    }

    @Override
    protected void applyPatch() {
        tempOldTable();
        execute(UserInfoTable.createTableSQL(dbType));

        execute(new ExecStatement("INSERT INTO " + tableName + " (" +
                UserInfoTable.ID + ',' +
                UserInfoTable.USER_UUID + ',' +
                UserInfoTable.SERVER_UUID + ',' +
                UserInfoTable.REGISTERED + ',' +
                UserInfoTable.OP + ',' +
                UserInfoTable.BANNED + ',' +
                UserInfoTable.JOIN_ADDRESS +
                ") SELECT " +
                UserInfoTable.ID + ',' +
                UserInfoTable.USER_UUID + ',' +
                UserInfoTable.SERVER_UUID + ',' +
                UserInfoTable.REGISTERED + ',' +
                UserInfoTable.OP + ',' +
                UserInfoTable.BANNED + ',' +
                "?" +
                FROM + tempTableName
        ) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setNull(1, Types.VARCHAR);
            }
        });

        dropTable(tempTableName);
    }


    private void tempOldTable() {
        if (!hasTable(tempTableName)) {
            renameTable(tableName, tempTableName);
        }
    }
}