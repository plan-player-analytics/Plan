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
 * Takes care of data without foreign keys that is missing the foreign key target in plan_servers.
 */
public class RemoveDanglingServerDataPatch extends Patch {

    private boolean userInfoTableOk;
    private boolean pingTableOk;
    private boolean worldTimesTableOk;
    private boolean sessionsTableOk;
    private boolean pingOptimizationFailed;
    private boolean userInfoOptimizationFailed;
    private boolean worldTimesOptimizationFailed;
    private boolean sessionsOptimizationFailed;

    @Override
    public boolean hasBeenApplied() {
        userInfoTableOk = hasColumn(UserInfoTable.TABLE_NAME, UserInfoTable.SERVER_ID);
        pingTableOk = hasColumn(PingTable.TABLE_NAME, PingTable.SERVER_ID);
        worldTimesTableOk = hasColumn(WorldTimesTable.TABLE_NAME, WorldTimesTable.SERVER_ID);
        sessionsTableOk = hasColumn(SessionsTable.TABLE_NAME, SessionsTable.SERVER_ID);
        pingOptimizationFailed = hasTable("temp_ping");
        userInfoOptimizationFailed = hasTable("temp_user_info");
        worldTimesOptimizationFailed = hasTable("temp_world_times");
        sessionsOptimizationFailed = hasTable("temp_sessions");

        return userInfoTableOk
                && pingTableOk
                && worldTimesTableOk
                && sessionsTableOk
                && !pingOptimizationFailed
                && !userInfoOptimizationFailed
                && !worldTimesOptimizationFailed
                && !sessionsOptimizationFailed;
    }

    @Override
    protected void applyPatch() {
        Set<String> serverUuids = query(getServerUuids());

        if (!userInfoTableOk) fixTable(UserInfoTable.TABLE_NAME, serverUuids);
        if (!pingTableOk) fixTable(PingTable.TABLE_NAME, serverUuids);
        if (!worldTimesTableOk) fixTable(WorldTimesTable.TABLE_NAME, serverUuids);
        if (!sessionsTableOk) fixTable(SessionsTable.TABLE_NAME, serverUuids);

        if (pingOptimizationFailed) fixTable("temp_ping", serverUuids);
        if (userInfoOptimizationFailed) fixTable("temp_user_info", serverUuids);
        if (worldTimesOptimizationFailed) fixTable("temp_world_times", serverUuids);
        if (sessionsOptimizationFailed) fixTable("temp_sessions", serverUuids);
    }

    private void fixTable(String tableName, Set<String> serverUuids) {
        Set<String> badUuids = query(getServerUuids(tableName));
        badUuids.removeAll(serverUuids);

        if (!badUuids.isEmpty()) {
            execute(deleteBadUuids(tableName, badUuids));
        }
    }

    private Executable deleteBadUuids(String tableName, Set<String> badUuids) {
        String sql = "DELETE FROM " + tableName + " WHERE server_uuid=?";
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

    private Query<Set<String>> getServerUuids() {
        String sql = "SELECT uuid FROM " + ServerTable.TABLE_NAME;

        return new QueryAllStatement<Set<String>>(sql) {
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

    private Query<Set<String>> getServerUuids(String tableName) {
        String sql = "SELECT DISTINCT server_uuid FROM " + tableName;

        return new QueryAllStatement<Set<String>>(sql) {
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
