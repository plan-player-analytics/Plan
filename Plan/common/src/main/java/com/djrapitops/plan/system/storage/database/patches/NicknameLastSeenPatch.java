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
package com.djrapitops.plan.system.storage.database.patches;

import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.system.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.system.storage.database.sql.parsing.Select;
import com.djrapitops.plan.system.storage.database.sql.tables.NicknamesTable;
import com.djrapitops.plan.system.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.system.storage.database.transactions.ExecBatchStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.system.storage.database.sql.parsing.Sql.AND;
import static com.djrapitops.plan.system.storage.database.sql.parsing.Sql.WHERE;

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

        Map<Integer, UUID> serverUUIDsByID = getServerUUIDsByID();
        Map<UUID, Integer> serverIDsByUUID = new HashMap<>();
        for (Map.Entry<Integer, UUID> entry : serverUUIDsByID.entrySet()) {
            serverIDsByUUID.put(entry.getValue(), entry.getKey());
        }

        Map<Integer, Set<Nickname>> nicknames = getNicknamesByUserID(serverUUIDsByID);
        updateLastUsed(serverIDsByUUID, nicknames);

        executeSwallowingExceptions("DROP TABLE plan_actions");
    }

    private Map<Integer, UUID> getServerUUIDsByID() {
        String sql = Select.from(ServerTable.TABLE_NAME,
                ServerTable.SERVER_ID, ServerTable.SERVER_UUID)
                .toString();

        return query(new QueryAllStatement<Map<Integer, UUID>>(sql) {
            @Override
            public Map<Integer, UUID> processResults(ResultSet set) throws SQLException {
                Map<Integer, UUID> uuids = new HashMap<>();
                while (set.next()) {
                    int id = set.getInt(ServerTable.SERVER_ID);
                    uuids.put(id, UUID.fromString(set.getString(ServerTable.SERVER_UUID)));
                }
                return uuids;
            }
        });
    }

    private Map<Integer, Set<Nickname>> getNicknamesByUserID(Map<Integer, UUID> serverUUIDsByID) {
        String fetchSQL = "SELECT * FROM plan_actions WHERE action_id=3 ORDER BY date DESC";
        return query(new QueryAllStatement<Map<Integer, Set<Nickname>>>(fetchSQL, 10000) {
            @Override
            public Map<Integer, Set<Nickname>> processResults(ResultSet set) throws SQLException {
                Map<Integer, Set<Nickname>> map = new HashMap<>();

                while (set.next()) {
                    long date = set.getLong("date");
                    int userID = set.getInt("user_id");
                    int serverID = set.getInt("server_id");
                    UUID serverUUID = serverUUIDsByID.get(serverID);
                    Nickname nick = new Nickname(set.getString("additional_info"), date, serverUUID);
                    Set<Nickname> nicknames1 = map.getOrDefault(userID, new HashSet<>());
                    if (serverUUID == null || nicknames1.contains(nick)) {
                        continue;
                    }
                    nicknames1.add(nick);
                    map.put(userID, nicknames1);
                }

                return map;
            }
        });
    }

    private void updateLastUsed(Map<UUID, Integer> serverIDsByUUID, Map<Integer, Set<Nickname>> nicknames) {
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
