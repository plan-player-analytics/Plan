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

import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.sql.tables.GeoInfoTable;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Patch that replaces plan_ips with plan_geolocations table.
 * <p>
 * Prepares Plan to be GDPR compliant through the easiest to make change.
 *
 * @author AuroraLS3
 */
public class DeleteIPsPatch extends Patch {

    private final String oldTableName;
    private final String tempTableName;

    public DeleteIPsPatch() {
        oldTableName = "plan_ips";
        tempTableName = "temp_ips";
    }

    @Override
    public boolean hasBeenApplied() {
        return !hasTable(oldTableName);
    }

    @Override
    protected void applyPatch() {
        if (hasTable(GeoInfoTable.TABLE_NAME) && hasLessDataInPlanIPs()) {
            dropTable(oldTableName);
            return;
        }
        tempOldTable();

        dropTable(GeoInfoTable.TABLE_NAME);
        execute(GeoInfoTable.createTableSQL(dbType));

        execute(INSERT_INTO + GeoInfoTable.TABLE_NAME + " (" +
                GeoInfoTable.USER_ID + ',' +
                GeoInfoTable.LAST_USED + ',' +
                GeoInfoTable.GEOLOCATION +
                ") SELECT " + DISTINCT +
                GeoInfoTable.USER_ID + ',' +
                GeoInfoTable.LAST_USED + ',' +
                GeoInfoTable.GEOLOCATION +
                FROM + tempTableName
        );

        dropTable(tempTableName);
    }

    private boolean hasLessDataInPlanIPs() {
        Integer inIPs = query(new QueryAllStatement<>(SELECT + "COUNT(1) as c" + FROM + oldTableName + lockForUpdate()) {
            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt("c") : 0;
            }
        });
        Integer inGeoInfo = query(new QueryAllStatement<>(SELECT + "COUNT(1) as c" + FROM + GeoInfoTable.TABLE_NAME + lockForUpdate()) {
            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt("c") : 0;
            }
        });
        return inIPs <= inGeoInfo;
    }

    private void tempOldTable() {
        if (!hasTable(tempTableName)) {
            renameTable(oldTableName, tempTableName);
        }
    }
}