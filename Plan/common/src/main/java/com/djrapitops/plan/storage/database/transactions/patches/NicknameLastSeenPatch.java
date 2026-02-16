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

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.tables.NicknamesTable;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.utilities.java.Maps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.djrapitops.plan.storage.database.sql.building.Sql.AND;
import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;

/**
 * Adds last_seen to nickname table by populating it with the data in actions table, and removes the actions table.
 * <p>
 * Actions table contained nickname change events and change to "last seen" saved space on the interface.
 *
 * @author AuroraLS3
 */
public class NicknameLastSeenPatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(NicknamesTable.TABLE_NAME, NicknamesTable.LAST_USED);
    }

    @Override
    protected void applyPatch() {
        addColumn(NicknamesTable.TABLE_NAME,
                NicknamesTable.LAST_USED + " bigint NOT NULL DEFAULT '0'"
        );

        if (hasColumn(NicknamesTable.TABLE_NAME, NicknamesTable.USER_UUID)) {
            // NicknamesOptimizationPatch makes this patch incompatible with newer patch versions.
            return;
        }

        // Create table if has failed already
        executeSwallowingExceptions("CREATE TABLE IF NOT EXISTS plan_actions " +
                "(action_id integer, date bigint, server_id integer, user_id integer, additional_info varchar(1))");

        Map<Integer, ServerUUID> serverUUIDsByID = getServerUUIDsByID();
        Map<ServerUUID, Integer> serverIDsByUUID = new HashMap<>();
        for (Map.Entry<Integer, ServerUUID> entry : serverUUIDsByID.entrySet()) {
            serverIDsByUUID.put(entry.getValue(), entry.getKey());
        }

        Map<Integer, Set<Nickname>> nicknames = getNicknamesByUserID(serverUUIDsByID);
        updateLastUsed(serverIDsByUUID, nicknames);

        executeSwallowingExceptions("DROP TABLE plan_actions");
    }

    private Map<Integer, ServerUUID> getServerUUIDsByID() {
        String sql = Select.from(ServerTable.TABLE_NAME,
                        ServerTable.ID, ServerTable.SERVER_UUID)
                .toString();

        return query(new QueryAllStatement<>(sql) {
            @Override
            public Map<Integer, ServerUUID> processResults(ResultSet set) throws SQLException {
                Map<Integer, ServerUUID> uuids = new HashMap<>();
                while (set.next()) {
                    int id = set.getInt(ServerTable.ID);
                    uuids.put(id, ServerUUID.fromString(set.getString(ServerTable.SERVER_UUID)));
                }
                return uuids;
            }
        });
    }

    private Map<Integer, Set<Nickname>> getNicknamesByUserID(Map<Integer, ServerUUID> serverUUIDsByID) {
        String fetchSQL = "SELECT * FROM plan_actions WHERE action_id=3 ORDER BY date DESC" + lockForUpdate();
        return query(new QueryAllStatement<>(fetchSQL, 10000) {
            @Override
            public Map<Integer, Set<Nickname>> processResults(ResultSet set) throws SQLException {
                Map<Integer, Set<Nickname>> map = new HashMap<>();

                while (set.next()) {
                    long date = set.getLong("date");
                    int userID = set.getInt("user_id");
                    int serverID = set.getInt("server_id");
                    ServerUUID serverUUID = serverUUIDsByID.get(serverID);
                    Nickname nick = new Nickname(set.getString("additional_info"), date, serverUUID);
                    Set<Nickname> foundNicknames = map.computeIfAbsent(userID, Maps::createSet);
                    if (serverUUID == null || foundNicknames.contains(nick)) {
                        continue;
                    }
                    foundNicknames.add(nick);
                }

                return map;
            }
        });
    }

    private void updateLastUsed(Map<ServerUUID, Integer> serverIDsByUUID, Map<Integer, Set<Nickname>> nicknames) {
        String updateSQL = "UPDATE " + NicknamesTable.TABLE_NAME + " SET " + NicknamesTable.LAST_USED + "=?" +
                WHERE + NicknamesTable.NICKNAME + "=?" +
                AND + "user_id=?" +
                AND + "server_id=?";

        execute(new ExecBatchStatement(updateSQL) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<Integer, Set<Nickname>> entry : nicknames.entrySet()) {
                    Integer userId = entry.getKey();
                    Set<Nickname> nicks = entry.getValue();
                    for (Nickname nick : nicks) {
                        Integer serverID = serverIDsByUUID.get(nick.getServerUUID());
                        statement.setLong(1, nick.getDate());
                        statement.setString(2, nick.getName());
                        statement.setInt(3, userId);
                        statement.setInt(4, serverID);
                        statement.addBatch();
                    }
                }
            }
        });
    }
}
