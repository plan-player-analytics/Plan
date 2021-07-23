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
package com.djrapitops.plan.storage.database.transactions.patches;

import com.djrapitops.plan.storage.database.sql.tables.GeoInfoTable;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Replaces user_id foreign keys with user_uuid fields in geolocation table.
 *
 * @author AuroraLS3
 */
public class GeoInfoOptimizationPatch extends Patch {

    private final String tempTableName;
    private final String oldTableName;

    public GeoInfoOptimizationPatch() {
        oldTableName = "plan_ips";
        tempTableName = "temp_ips";
    }

    @Override
    public boolean hasBeenApplied() {
        return !hasTable(oldTableName)
                || hasColumn(oldTableName, GeoInfoTable.ID)
                && hasColumn(oldTableName, GeoInfoTable.USER_UUID)
                && !hasColumn(oldTableName, "user_id")
                && !hasTable(tempTableName); // If this table exists the patch has failed to finish.
    }

    @Override
    protected void applyPatch() {
        tempOldTable();
        execute(GeoInfoTable.createTableSQL(dbType));

        execute("INSERT INTO " + GeoInfoTable.TABLE_NAME + " (" +
                GeoInfoTable.USER_UUID + ',' +
                GeoInfoTable.LAST_USED + ',' +
                GeoInfoTable.GEOLOCATION +
                ") " + SELECT + DISTINCT +
                "(SELECT plan_users.uuid FROM plan_users WHERE plan_users.id = " + tempTableName + ".user_id LIMIT 1), " +
                GeoInfoTable.LAST_USED + ',' +
                GeoInfoTable.GEOLOCATION +
                FROM + tempTableName
        );

        dropTable(tempTableName);
    }

    private void tempOldTable() {
        if (!hasTable(tempTableName)) {
            renameTable(oldTableName, tempTableName);
        }
    }
}
