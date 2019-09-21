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
import com.djrapitops.plan.storage.database.sql.parsing.CreateTableParser;
import com.djrapitops.plan.storage.database.sql.parsing.Sql;
import com.djrapitops.plan.storage.database.transactions.patches.DeleteIPHashesPatch;
import com.djrapitops.plan.storage.database.transactions.patches.GeoInfoLastUsedPatch;
import com.djrapitops.plan.storage.database.transactions.patches.GeoInfoOptimizationPatch;
import com.djrapitops.plan.storage.database.transactions.patches.Version10Patch;

import static com.djrapitops.plan.storage.database.sql.parsing.Sql.AND;
import static com.djrapitops.plan.storage.database.sql.parsing.Sql.WHERE;

/**
 * Table information about 'plan_ips'.
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link GeoInfoLastUsedPatch}
 * {@link GeoInfoOptimizationPatch}
 * {@link DeleteIPHashesPatch}
 *
 * @author Rsl1122
 */
public class GeoInfoTable {

    public static final String TABLE_NAME = "plan_ips";

    public static final String ID = "id";
    public static final String USER_UUID = "uuid";
    @Deprecated
    public static final String IP = "ip";
    public static final String GEOLOCATION = "geolocation";
    public static final String LAST_USED = "last_used";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " ("
            + USER_UUID + ','
            + IP + ','
            + GEOLOCATION + ','
            + LAST_USED
            + ") VALUES (?, ?, ?, ?)";

    public static final String UPDATE_STATEMENT = "UPDATE " + TABLE_NAME + " SET "
            + LAST_USED + "=?" +
            WHERE + USER_UUID + "=?" +
            AND + GEOLOCATION + "=?";

    private GeoInfoTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_UUID, Sql.varchar(36)).notNull()
                .column(IP, Sql.varchar(39)).notNull()
                .column(GEOLOCATION, Sql.varchar(50)).notNull()
                .column(LAST_USED, Sql.LONG).notNull().defaultValue("0")
                .toString();
    }

}
