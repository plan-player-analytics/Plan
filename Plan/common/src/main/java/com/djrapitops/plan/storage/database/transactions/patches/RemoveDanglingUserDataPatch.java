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

import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.sql.tables.*;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Takes care of data without foreign keys that is missing the foreign key target in plan_users.
 */
public class RemoveDanglingUserDataPatch extends Patch {

    private boolean userInfoTableOk;
    private boolean geolocationsTableOk;
    private boolean pingTableOk;
    private boolean worldTimesTableOk;
    private boolean sessionsTableOk;
    private boolean pingOptimizationFailed;
    private boolean userInfoOptimizationFailed;
    private boolean worldTimesOptimizationFailed;
    private boolean sessionsOptimizationFailed;
    private boolean geolocationOptimizationFailed;

    @Override
    public boolean hasBeenApplied() {
        userInfoTableOk = hasColumn(UserInfoTable.TABLE_NAME, UserInfoTable.USER_ID);
        geolocationsTableOk = hasColumn(GeoInfoTable.TABLE_NAME, GeoInfoTable.USER_ID);
        pingTableOk = hasColumn(PingTable.TABLE_NAME, PingTable.USER_ID);
        worldTimesTableOk = hasColumn(WorldTimesTable.TABLE_NAME, WorldTimesTable.USER_ID);
        sessionsTableOk = hasColumn(SessionsTable.TABLE_NAME, SessionsTable.USER_ID);
        pingOptimizationFailed = hasTable("temp_ping");
        userInfoOptimizationFailed = hasTable("temp_user_info");
        worldTimesOptimizationFailed = hasTable("temp_world_times");
        sessionsOptimizationFailed = hasTable("temp_sessions");
        geolocationOptimizationFailed = hasTable("temp_geoinformation");

        return userInfoTableOk
                && geolocationsTableOk
                && pingTableOk
                && worldTimesTableOk
                && sessionsTableOk
                && !pingOptimizationFailed
                && !userInfoOptimizationFailed
                && !worldTimesOptimizationFailed
                && !sessionsOptimizationFailed
                && !geolocationOptimizationFailed;
    }

    @Override
    protected void applyPatch() {
        Set<String> uuids = query(getUuids(UsersTable.TABLE_NAME));

        if (!userInfoTableOk) fixTable(UserInfoTable.TABLE_NAME, uuids);
        if (!geolocationsTableOk) fixTable(GeoInfoTable.TABLE_NAME, uuids);
        if (!pingTableOk) fixTable(PingTable.TABLE_NAME, uuids);
        if (!worldTimesTableOk) fixTable(WorldTimesTable.TABLE_NAME, uuids);
        if (!sessionsTableOk) fixTable(SessionsTable.TABLE_NAME, uuids);

        if (pingOptimizationFailed) fixTable("temp_ping", uuids);
        if (userInfoOptimizationFailed) fixTable("temp_user_info", uuids);
        if (worldTimesOptimizationFailed) fixTable("temp_world_times", uuids);
        if (sessionsOptimizationFailed) fixTable("temp_sessions", uuids);
        if (geolocationOptimizationFailed) fixTable("temp_geoinformation", uuids);
    }

    private void fixTable(String tableName, Set<String> uuids) {
        Set<String> badUuids = query(getUuids(tableName));
        badUuids.removeAll(uuids);

        if (!badUuids.isEmpty()) {
            execute(deleteBadUuids(tableName, badUuids));
        }
    }

    private Executable deleteBadUuids(String tableName, Set<String> badUuids) {
        String sql = "DELETE FROM " + tableName + " WHERE uuid=?";
        return new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (String badUuid : badUuids) {
                    statement.setString(1, badUuid);
                    statement.addBatch();
                }
            }
        };
    }

    private Query<Set<String>> getUuids(String tableName) {
        String sql = "SELECT DISTINCT uuid FROM " + tableName + lockForUpdate();

        return new QueryAllStatement<>(sql) {
            @Override
            public Set<String> processResults(ResultSet set) throws SQLException {
                HashSet<String> uuids = new HashSet<>();
                while (set.next()) {
                    uuids.add(set.getString("uuid"));
                }
                return uuids;
            }
        };
    }
}
