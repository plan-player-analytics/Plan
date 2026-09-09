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
package com.djrapitops.plan.storage.database.sql.tables.webuser;

import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Insert;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Represents plan_incomplete_registration table.
 *
 * @author AuroraLS3
 */
public class RegistrationTable {

    public static final String TABLE_NAME = "plan_incomplete_registration";

    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String SALT_PASSWORD_HASH = "salted_password_hash";
    public static final String CODE = "code";
    public static final String EXPIRY_TIME = "expiry_time";

    public static final String INSERT_STATEMENT = Insert.values(TABLE_NAME, USERNAME, SALT_PASSWORD_HASH, CODE, EXPIRY_TIME);

    public static final String DELETE_EXPIRED = DELETE_FROM + TABLE_NAME + WHERE + EXPIRY_TIME + "<=?";
    public static final String DELETE_BY_CODE = DELETE_FROM + TABLE_NAME + WHERE + CODE + "=?";

    public static final String SELECT_BY_CODE = SELECT + "*" + FROM + TABLE_NAME + WHERE + CODE + "=?" + AND + EXPIRY_TIME + ">?";

    private RegistrationTable() {
        /* Static SQL utility class */
    }

    public static String createTableSql(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, INT).primaryKey()
                .column(USERNAME, varchar(100)).notNull().unique()
                .column(SALT_PASSWORD_HASH, varchar(100)).notNull()
                .column(CODE, varchar(12)).notNull().unique()
                .column(EXPIRY_TIME, LONG).notNull()
                .toString();
    }
}
