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
import com.djrapitops.plan.db.patches.PingOptimizationPatch;
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Sql;

/**
 * Table information about 'plan_ping'.
 * <p>
 * Patches related to this table:
 * {@link PingOptimizationPatch}
 *
 * @author Rsl1122
 */
public class PingTable {

    public static final String TABLE_NAME = "plan_ping";

    public static final String ID = "id";
    public static final String USER_UUID = "uuid";
    public static final String SERVER_UUID = "server_uuid";
    public static final String DATE = "date";
    public static final String MAX_PING = "max_ping";
    public static final String AVG_PING = "avg_ping";
    public static final String MIN_PING = "min_ping";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" +
            USER_UUID + ", " +
            SERVER_UUID + ", " +
            DATE + ", " +
            MIN_PING + ", " +
            MAX_PING + ", " +
            AVG_PING +
            ") VALUES (?, ?, ?, ?, ?, ?)";

    private PingTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_UUID, Sql.varchar(36)).notNull()
                .column(SERVER_UUID, Sql.varchar(36)).notNull()
                .column(DATE, Sql.LONG).notNull()
                .column(MAX_PING, Sql.INT).notNull()
                .column(MIN_PING, Sql.INT).notNull()
                .column(AVG_PING, Sql.DOUBLE).notNull()
                .toString();
    }
}
