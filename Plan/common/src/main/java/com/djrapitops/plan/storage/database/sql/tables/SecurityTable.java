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

/**
 * Table information about 'plan_security'
 *
 * @author AuroraLS3
 */
public class SecurityTable {

    public static final String TABLE_NAME = "plan_security";

    public static final String USERNAME = "username";
    public static final String LINKED_TO = "linked_to_uuid";
    public static final String SALT_PASSWORD_HASH = "salted_pass_hash";
    public static final String PERMISSION_LEVEL = "permission_level";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" +
            USERNAME + ',' +
            LINKED_TO + ',' +
            SALT_PASSWORD_HASH + ',' +
            PERMISSION_LEVEL + ") VALUES (?,?,?,?)";

    private SecurityTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(USERNAME, Sql.varchar(100)).notNull().unique()
                .column(LINKED_TO, Sql.varchar(36)).defaultValue("''")
                .column(SALT_PASSWORD_HASH, Sql.varchar(100)).notNull().unique()
                .column(PERMISSION_LEVEL, Sql.INT).notNull()
                .toString();
    }
}
