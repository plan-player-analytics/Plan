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
package com.djrapitops.plan.storage.database.sql.tables;

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.transactions.patches.UserInfoOptimizationPatch;
import com.djrapitops.plan.storage.database.transactions.patches.Version10Patch;

/**
 * Table information about 'plan_user_info'.
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link UserInfoOptimizationPatch}
 * {@link com.djrapitops.plan.storage.database.transactions.patches.RegisterDateMinimizationPatch}
 *
 * @author AuroraLS3
 */
public class UserInfoTable {

    public static final String TABLE_NAME = "plan_user_info";

    public static final String ID = "id";
    public static final String USER_ID = "user_id";
    public static final String SERVER_ID = "server_id";
    public static final String REGISTERED = "registered";
    public static final String OP = "opped";
    public static final String BANNED = "banned";
    public static final String JOIN_ADDRESS = "join_address";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" +
            USER_ID + ',' +
            REGISTERED + ',' +
            SERVER_ID + ',' +
            BANNED + ',' +
            JOIN_ADDRESS + ',' +
            OP +
            ") VALUES (" + UsersTable.SELECT_USER_ID + ", ?, " + ServerTable.SELECT_SERVER_ID + ", ?, ?, ?)";

    private UserInfoTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_ID, Sql.INT).notNull()
                .column(SERVER_ID, Sql.INT).notNull()
                .column(JOIN_ADDRESS, Sql.varchar(255))
                .column(REGISTERED, Sql.LONG).notNull()
                .column(OP, Sql.BOOL).notNull().defaultValue(false)
                .column(BANNED, Sql.BOOL).notNull().defaultValue(false)
                .foreignKey(USER_ID, UsersTable.TABLE_NAME, UsersTable.ID)
                .foreignKey(SERVER_ID, ServerTable.TABLE_NAME, ServerTable.ID)
                .toString();
    }
}
