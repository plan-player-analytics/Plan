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

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Table information about 'plan_cookies'
 *
 * @author AuroraLS3
 */
public class CookieTable {

    public static final String TABLE_NAME = "plan_cookies";

    public static final String ID = "id";
    public static final String WEB_USERNAME = "web_username";
    public static final String COOKIE = "cookie";
    public static final String IP_ADDRESS = "ip_address";
    public static final String EXPIRES = "expires";

    public static final String INSERT_STATEMENT = INSERT_INTO + TABLE_NAME + " (" +
            WEB_USERNAME + ',' +
            COOKIE + ',' +
            EXPIRES + ',' +
            IP_ADDRESS + ") VALUES (?,?,?,?)";

    public static final String DELETE_BY_COOKIE_STATEMENT = DELETE_FROM + TABLE_NAME +
            WHERE + COOKIE + "=?";

    public static final String DELETE_BY_USER_STATEMENT = DELETE_FROM + TABLE_NAME +
            WHERE + WEB_USERNAME + "=?";

    public static final String DELETE_OLDER_STATEMENT = DELETE_FROM + TABLE_NAME +
            WHERE + EXPIRES + "<=?";

    public static final String DELETE_ALL_STATEMENT = DELETE_FROM + TABLE_NAME;

    private CookieTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(WEB_USERNAME, Sql.varchar(100)).notNull()
                .column(EXPIRES, Sql.LONG).notNull()
                .column(COOKIE, Sql.varchar(64)).notNull()
                .column(IP_ADDRESS, Sql.varchar(45)) // Max IPv6 text length 45 chars
                .toString();
    }
}
