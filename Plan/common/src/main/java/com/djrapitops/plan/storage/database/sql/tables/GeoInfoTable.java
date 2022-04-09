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
import com.djrapitops.plan.storage.database.transactions.patches.GeoInfoLastUsedPatch;
import com.djrapitops.plan.storage.database.transactions.patches.GeoInfoOptimizationPatch;
import com.djrapitops.plan.storage.database.transactions.patches.Version10Patch;

import static com.djrapitops.plan.storage.database.sql.building.Sql.AND;
import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;

/**
 * Table information about 'plan_ips'.
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link GeoInfoLastUsedPatch}
 * {@link GeoInfoOptimizationPatch}
 *
 * @author AuroraLS3
 */
public class GeoInfoTable {

    public static final String TABLE_NAME = "plan_geolocations";

    public static final String ID = "id";
    public static final String USER_ID = "user_id";
    public static final String GEOLOCATION = "geolocation";
    public static final String LAST_USED = "last_used";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " ("
            + USER_ID + ','
            + GEOLOCATION + ','
            + LAST_USED
            + ") VALUES (" + UsersTable.SELECT_USER_ID + ", ?, ?)";

    public static final String UPDATE_STATEMENT = "UPDATE " + TABLE_NAME + " SET " +
            LAST_USED + "=?" +
            WHERE + USER_ID + "=" + UsersTable.SELECT_USER_ID +
            AND + GEOLOCATION + "=?";

    private GeoInfoTable() {
        /* Static information class */
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableBuilder.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(USER_ID, Sql.INT).notNull()
                .column(GEOLOCATION, Sql.varchar(50)).notNull()
                .column(LAST_USED, Sql.LONG).notNull().defaultValue("0")
                .foreignKey(USER_ID, UsersTable.TABLE_NAME, UsersTable.ID)
                .toString();
    }

}
