package com.djrapitops.plan.system.database.databases.sql.patches;

import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.processing.ExecStatement;
import com.djrapitops.plan.system.database.databases.sql.processing.QueryAllStatement;
import com.djrapitops.plan.system.database.databases.sql.tables.GeoInfoTable;
import com.djrapitops.plan.system.database.databases.sql.tables.NicknamesTable;
import com.djrapitops.plan.system.database.databases.sql.tables.UserIDTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class NicknameLastSeenPatch extends Patch {

    public NicknameLastSeenPatch(SQLDB db) {
        super(db);
    }

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(GeoInfoTable.TABLE_NAME, GeoInfoTable.Col.LAST_USED.get());
    }

    @Override
    public void apply() {
        addColumns(NicknamesTable.TABLE_NAME,
                NicknamesTable.Col.LAST_USED + " bigint NOT NULL DEFAULT '0'"
        );

        // Create table if has failed already
        db.executeUnsafe("CREATE TABLE IF NOT EXISTS plan_actions " +
                "(action_id integer, date bigint, server_id integer, user_id integer, additional_info varchar(1))");

        Map<Integer, UUID> serverUUIDsByID = db.getServerTable().getServerUUIDsByID();
        Map<UUID, Integer> serverIDsByUUID = new HashMap<>();
        for (Map.Entry<Integer, UUID> entry : serverUUIDsByID.entrySet()) {
            serverIDsByUUID.put(entry.getValue(), entry.getKey());
        }

        Map<Integer, Set<Nickname>> nicknames = getNicknamesByUserID(serverUUIDsByID);
        updateLastUsed(serverIDsByUUID, nicknames);

        db.executeUnsafe("DROP TABLE plan_actions");
    }

    private Map<Integer, Set<Nickname>> getNicknamesByUserID(Map<Integer, UUID> serverUUIDsByID) {
        String fetchSQL = "SELECT * FROM plan_actions WHERE action_id=3 ORDER BY date DESC";
        return query(new QueryAllStatement<Map<Integer, Set<Nickname>>>(fetchSQL, 10000) {
            @Override
            public Map<Integer, Set<Nickname>> processResults(ResultSet set) throws SQLException {
                Map<Integer, Set<Nickname>> map = new HashMap<>();

                while (set.next()) {
                    long date = set.getLong("date");
                    int userID = set.getInt(UserIDTable.Col.USER_ID.get());
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
        String updateSQL = "UPDATE " + NicknamesTable.TABLE_NAME + " SET " + NicknamesTable.Col.LAST_USED + "=?" +
                " WHERE " + NicknamesTable.Col.NICKNAME + "=?" +
                " AND " + NicknamesTable.Col.USER_ID + "=?" +
                " AND " + NicknamesTable.Col.SERVER_ID + "=?";

        db.executeBatch(new ExecStatement(updateSQL) {
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
