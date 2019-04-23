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
package com.djrapitops.plan.db.patches;

import com.djrapitops.plan.db.sql.tables.GeoInfoTable;

public class GeoInfoOptimizationPatch extends Patch {

    private String tempTableName;
    private String tableName;

    public GeoInfoOptimizationPatch() {
        tableName = GeoInfoTable.TABLE_NAME;
        tempTableName = "temp_ips";
    }

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(tableName, GeoInfoTable.ID)
                && hasColumn(tableName, GeoInfoTable.USER_UUID)
                && !hasColumn(tableName, "user_id")
                && !hasTable(tempTableName); // If this table exists the patch has failed to finish.
    }

    @Override
    protected void applyPatch() {
        tempOldTable();
        execute(GeoInfoTable.createTableSQL(dbType));

        execute("INSERT INTO " + tableName + " (" +
                GeoInfoTable.USER_UUID + ", " +
                GeoInfoTable.IP + ", " +
                GeoInfoTable.LAST_USED + ", " +
                GeoInfoTable.GEOLOCATION +
                ") SELECT " +
                "(SELECT plan_users.uuid FROM plan_users WHERE plan_users.id = " + tempTableName + ".user_id LIMIT 1), " +
                GeoInfoTable.IP + ", " +
                GeoInfoTable.LAST_USED + ", " +
                GeoInfoTable.GEOLOCATION +
                " FROM " + tempTableName
        );

        dropTable(tempTableName);
    }

    private void tempOldTable() {
        if (!hasTable(tempTableName)) {
            renameTable(tableName, tempTableName);
        }
    }
}
