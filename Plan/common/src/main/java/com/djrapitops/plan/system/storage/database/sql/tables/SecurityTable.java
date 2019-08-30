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
package com.djrapitops.plan.system.storage.database.sql.tables;

import com.djrapitops.plan.system.storage.database.DBType;
import com.djrapitops.plan.system.storage.database.sql.parsing.CreateTableParser;
import com.djrapitops.plan.system.storage.database.sql.parsing.Insert;
import com.djrapitops.plan.system.storage.database.sql.parsing.Sql;

/**
 * Table information about 'plan_security'
 *
 * @author Rsl1122
 */
public class SecurityTable {

    public static final String TABLE_NAME = "plan_security";

    public static final String USERNAME = "username";
    public static final String SALT_PASSWORD_HASH = "salted_pass_hash";
    public static final String PERMISSION_LEVEL = "permission_level";

    public static final String INSERT_STATEMENT = Insert.values(TABLE_NAME, USERNAME, SALT_PASSWORD_HASH, PERMISSION_LEVEL);

    private SecurityTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(USERNAME, Sql.varchar(100)).notNull().unique()
                .column(SALT_PASSWORD_HASH, Sql.varchar(100)).notNull().unique()
                .column(PERMISSION_LEVEL, Sql.INT).notNull()
                .toString();
    }
}
