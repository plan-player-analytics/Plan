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
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Changes register dates on networks to the smallest number found in the database.
 * <p>
 * Proxy servers do not store player register date information, so Game servers can hold earlier
 * join date than the first session Plan sees. This patch changes the register date in
 * plan_users if a smaller register date in plan_user_info is found.
 *
 * @author AuroraLS3
 */
public class RegisterDateMinimizationPatch extends Patch {

    private Map<UUID, Long> registerDates;

    @Override
    public boolean hasBeenApplied() {
        registerDates = query(fetchSmallestServerRegisterDates());
        return registerDates.isEmpty();
    }

    private Query<Map<UUID, Long>> fetchSmallestServerRegisterDates() {
        String selectSmallestRegisterDates = SELECT +
                UserInfoTable.USER_ID + ',' +
                "MIN(" + UserInfoTable.REGISTERED + ") as min_registered" +
                FROM + UserInfoTable.TABLE_NAME +
                WHERE + UserInfoTable.REGISTERED + "!=0" +
                GROUP_BY + UserInfoTable.USER_ID + lockForUpdate();

        String sql = SELECT + UsersTable.USER_UUID + ",u1." + UsersTable.REGISTERED + ",min_registered" +
                FROM + '(' + selectSmallestRegisterDates + ") u2" +
                INNER_JOIN + UsersTable.TABLE_NAME + " u1 on u1." + UsersTable.ID + "=u2." + UserInfoTable.USER_ID +
                WHERE + "u1." + UsersTable.REGISTERED + ">min_registered OR u1." + UsersTable.REGISTERED + "=0" + lockForUpdate();

        return new QueryAllStatement<>(sql, 500) {
            @Override
            public Map<UUID, Long> processResults(ResultSet set) throws SQLException {
                Map<UUID, Long> dates = new HashMap<>();
                while (set.next()) {
                    UUID playerUUID = UUID.fromString(set.getString(UsersTable.USER_UUID));
                    long newRegisterDate = set.getLong("min_registered");
                    dates.put(playerUUID, newRegisterDate);
                }
                return dates;
            }
        };

    }

    @Override
    protected void applyPatch() {
        if (registerDates.isEmpty()) return;

        String sql = "UPDATE " + UsersTable.TABLE_NAME + " SET " + UsersTable.REGISTERED + "=?" +
                WHERE + UsersTable.USER_UUID + "=?" +
                AND + UsersTable.REGISTERED + ">? OR " + UsersTable.REGISTERED + "=0";

        execute(new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<UUID, Long> entry : registerDates.entrySet()) {
                    UUID playerUUID = entry.getKey();
                    Long registerDate = entry.getValue();
                    statement.setLong(1, registerDate);
                    statement.setString(2, playerUUID.toString());
                    statement.setLong(3, registerDate);
                    statement.addBatch();
                }

            }
        });
    }
}
