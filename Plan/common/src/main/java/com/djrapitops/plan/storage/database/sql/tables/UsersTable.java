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
import com.djrapitops.plan.storage.database.sql.building.Insert;
import com.djrapitops.plan.storage.database.sql.building.Sql;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Table information about 'plan_users'.
 * <p>
 * This table is used to store Player information that applies to all servers.
 * <p>
 * Patches that apply to this table:
 * {@link com.djrapitops.plan.storage.database.transactions.patches.Version10Patch}
 * {@link com.djrapitops.plan.storage.database.transactions.patches.RegisterDateMinimizationPatch}
 * {@link com.djrapitops.plan.storage.database.transactions.patches.UsersTableNameLengthPatch}
 *
 * @author AuroraLS3
 */
public class UsersTable {

    public static final String TABLE_NAME = "plan_users";

    public static final String ID = "id";
    public static final String USER_UUID = "uuid";
    public static final String REGISTERED = "registered";
    public static final String USER_NAME = "name";
    public static final String TIMES_KICKED = "times_kicked";

    public static final String INSERT_STATEMENT = Insert.values(TABLE_NAME, USER_UUID, USER_NAME, REGISTERED, TIMES_KICKED);
    public static final String UPDATE_MERGE_STATEMENT = "UPDATE " + TABLE_NAME + " SET " +
            TIMES_KICKED + "=" + TIMES_KICKED + "+?," +
            REGISTERED + "= CASE WHEN " + REGISTERED + "<=? THEN " + REGISTERED + " ELSE ? END" +
            WHERE + ID + "=?";
    public static final String SELECT_USER_ID = '(' + SELECT + TABLE_NAME + '.' + ID +
            FROM + TABLE_NAME +
            WHERE + TABLE_NAME + '.' + USER_UUID + "=?" + LIMIT + "1)";

    private UsersTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_UUID, Sql.varchar(36)).notNull().unique()
                .column(REGISTERED, Sql.LONG).notNull()
                .column(USER_NAME, Sql.varchar(36)).notNull()
                .column(TIMES_KICKED, Sql.INT).notNull().defaultValue("0")
                .toString();
    }
}
