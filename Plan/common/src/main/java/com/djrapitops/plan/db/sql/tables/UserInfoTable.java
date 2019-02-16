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
package com.djrapitops.plan.db.sql.tables;

import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.patches.UserInfoOptimizationPatch;
import com.djrapitops.plan.db.patches.Version10Patch;
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Sql;

/**
 * Table that is in charge of storing server specific player data.
 * <p>
 * Table Name: plan_user_info
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link UserInfoOptimizationPatch}
 *
 * @author Rsl1122
 */
public class UserInfoTable extends Table {

    public static final String TABLE_NAME = "plan_user_info";

    public static final String ID = "id";
    public static final String USER_UUID = "uuid";
    public static final String SERVER_UUID = "server_uuid";
    public static final String REGISTERED = "registered";
    public static final String OP = "opped";
    public static final String BANNED = "banned";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" +
            USER_UUID + ", " +
            REGISTERED + ", " +
            SERVER_UUID + ", " +
            BANNED + ", " +
            OP +
            ") VALUES (?, ?, ?, ?, ?)";

    public UserInfoTable(SQLDB db) {
        super(TABLE_NAME, db);
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_UUID, Sql.varchar(36)).notNull()
                .column(SERVER_UUID, Sql.varchar(36)).notNull()
                .column(REGISTERED, Sql.LONG).notNull()
                .column(OP, Sql.BOOL).notNull().defaultValue(false)
                .column(BANNED, Sql.BOOL).notNull().defaultValue(false)
                .toString();
    }
}
