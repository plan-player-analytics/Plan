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
package com.djrapitops.plan.db.access.queries.objects;

import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.sql.tables.KillsTable;
import com.djrapitops.plan.db.sql.tables.SessionsTable;
import com.djrapitops.plan.db.sql.tables.UsersTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Queries for {@link com.djrapitops.plan.data.container.Session} objects.
 *
 * @author Rsl1122
 */
public class SessionQueries {

    private SessionQueries() {
        /* Static method class */
    }

    /**
     * Query database for all Kill data.
     *
     * @return Map: Session ID - List of PlayerKills
     */
    public static Query<Map<Integer, List<PlayerKill>>> fetchAllPlayerKillDataBySessionID() {
        String usersUUIDColumn = UsersTable.TABLE_NAME + "." + UsersTable.USER_UUID;
        String usersNameColumn = UsersTable.TABLE_NAME + "." + UsersTable.USER_NAME + " as victim_name";
        String sql = "SELECT " +
                KillsTable.SESSION_ID + ", " +
                KillsTable.DATE + ", " +
                KillsTable.WEAPON + ", " +
                KillsTable.VICTIM_UUID + ", " +
                usersNameColumn +
                " FROM " + KillsTable.TABLE_NAME +
                " INNER JOIN " + UsersTable.TABLE_NAME + " on " + usersUUIDColumn + "=" + KillsTable.VICTIM_UUID;

        return new QueryAllStatement<Map<Integer, List<PlayerKill>>>(sql, 50000) {
            @Override
            public Map<Integer, List<PlayerKill>> processResults(ResultSet set) throws SQLException {
                Map<Integer, List<PlayerKill>> allPlayerKills = new HashMap<>();
                while (set.next()) {
                    int sessionID = set.getInt(KillsTable.SESSION_ID);

                    List<PlayerKill> playerKills = allPlayerKills.getOrDefault(sessionID, new ArrayList<>());

                    UUID victim = UUID.fromString(set.getString(KillsTable.VICTIM_UUID));
                    String victimName = set.getString("victim_name");
                    long date = set.getLong(KillsTable.DATE);
                    String weapon = set.getString(KillsTable.WEAPON);
                    playerKills.add(new PlayerKill(victim, weapon, date, victimName));

                    allPlayerKills.put(sessionID, playerKills);
                }
                return allPlayerKills;
            }
        };
    }

    /**
     * Query the database for Session data without kill, death or world data.
     *
     * @return Multimap: Server UUID - (Player UUID - List of sessions)
     */
    public static Query<Map<UUID, Map<UUID, List<Session>>>> fetchAllSessionsWithoutKillOrWorldData() {
        String sql = "SELECT " +
                SessionsTable.ID + ", " +
                SessionsTable.USER_UUID + ", " +
                SessionsTable.SERVER_UUID + ", " +
                SessionsTable.SESSION_START + ", " +
                SessionsTable.SESSION_END + ", " +
                SessionsTable.DEATHS + ", " +
                SessionsTable.MOB_KILLS + ", " +
                SessionsTable.AFK_TIME +
                " FROM " + SessionsTable.TABLE_NAME;

        return new QueryAllStatement<Map<UUID, Map<UUID, List<Session>>>>(sql, 20000) {
            @Override
            public Map<UUID, Map<UUID, List<Session>>> processResults(ResultSet set) throws SQLException {
                Map<UUID, Map<UUID, List<Session>>> map = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(SessionsTable.SERVER_UUID));
                    UUID uuid = UUID.fromString(set.getString(SessionsTable.USER_UUID));

                    Map<UUID, List<Session>> sessionsByUser = map.getOrDefault(serverUUID, new HashMap<>());
                    List<Session> sessions = sessionsByUser.getOrDefault(uuid, new ArrayList<>());

                    long start = set.getLong(SessionsTable.SESSION_START);
                    long end = set.getLong(SessionsTable.SESSION_END);

                    int deaths = set.getInt(SessionsTable.DEATHS);
                    int mobKills = set.getInt(SessionsTable.MOB_KILLS);
                    int id = set.getInt(SessionsTable.ID);

                    long timeAFK = set.getLong(SessionsTable.AFK_TIME);

                    sessions.add(new Session(id, uuid, serverUUID, start, end, mobKills, deaths, timeAFK));

                    sessionsByUser.put(uuid, sessions);
                    map.put(serverUUID, sessionsByUser);
                }
                return map;
            }
        };
    }

    /**
     * Query the database for Session data with kill, death or world data.
     *
     * @return Multimap: Server UUID - (Player UUID - List of sessions)
     */
    public static Query<Map<UUID, Map<UUID, List<Session>>>> fetchAllSessionsWithKillAndWorldData() {
        return db -> {
            Map<UUID, Map<UUID, List<Session>>> sessions = db.query(fetchAllSessionsWithoutKillOrWorldData());
            db.getKillsTable().addKillsToSessions(sessions);
            db.getWorldTimesTable().addWorldTimesToSessions(sessions);
            return sessions;
        };
    }

    /**
     * Query the database for Session data with kill, death or world data.
     *
     * @return List of sessions
     */
    public static Query<List<Session>> fetchAllSessionsFlatWithKillAndWorldData() {
        return db -> db.query(fetchAllSessionsWithKillAndWorldData())
                .values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}