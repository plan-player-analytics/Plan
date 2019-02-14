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
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.tables.*;
import com.djrapitops.plan.utilities.comparators.DateHolderRecentComparator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.djrapitops.plan.db.sql.parsing.Sql.WHERE;

/**
 * Queries for {@link com.djrapitops.plan.data.container.Session} objects.
 *
 * @author Rsl1122
 */
public class SessionQueries {

    private SessionQueries() {
        /* Static method class */
    }

    private static final String SELECT_SESSIONS_STATEMENT = "SELECT " +
            SessionsTable.TABLE_NAME + "." + SessionsTable.ID + ", " +
            SessionsTable.TABLE_NAME + "." + SessionsTable.USER_UUID + ", " +
            SessionsTable.TABLE_NAME + "." + SessionsTable.SERVER_UUID + ", " +
            SessionsTable.SESSION_START + ", " +
            SessionsTable.SESSION_END + ", " +
            SessionsTable.MOB_KILLS + ", " +
            SessionsTable.DEATHS + ", " +
            SessionsTable.AFK_TIME + ", " +
            WorldTimesTable.SURVIVAL + ", " +
            WorldTimesTable.CREATIVE + ", " +
            WorldTimesTable.ADVENTURE + ", " +
            WorldTimesTable.SPECTATOR + ", " +
            WorldTable.NAME + ", " +
            KillsTable.VICTIM_UUID + ", " +
            UsersTable.USER_NAME + " as victim_name, " +
            KillsTable.DATE + ", " +
            KillsTable.WEAPON +
            " FROM " + SessionsTable.TABLE_NAME +
            " LEFT JOIN " + KillsTable.TABLE_NAME + " ON " + SessionsTable.TABLE_NAME + "." + SessionsTable.ID + "=" + KillsTable.TABLE_NAME + "." + KillsTable.SESSION_ID +
            " LEFT JOIN " + UsersTable.TABLE_NAME + " on " + UsersTable.TABLE_NAME + "." + UsersTable.USER_UUID + "=" + KillsTable.VICTIM_UUID +
            " INNER JOIN " + WorldTimesTable.TABLE_NAME + " ON " + SessionsTable.TABLE_NAME + "." + SessionsTable.ID + "=" + WorldTimesTable.TABLE_NAME + "." + WorldTimesTable.SESSION_ID +
            " INNER JOIN " + WorldTable.TABLE_NAME + " ON " + WorldTimesTable.TABLE_NAME + "." + WorldTimesTable.WORLD_ID + "=" + WorldTable.TABLE_NAME + "." + WorldTable.ID;

    private static final String ORDER_BY_SESSION_START_DESC = " ORDER BY " + SessionsTable.SESSION_START + " DESC";

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
     * @return List of sessions
     */
    public static Query<List<Session>> fetchAllSessions() {
        String sql = SELECT_SESSIONS_STATEMENT +
                ORDER_BY_SESSION_START_DESC;
        return new QueryAllStatement<List<Session>>(sql, 50000) {
            @Override
            public List<Session> processResults(ResultSet set) throws SQLException {
                return extractDataFromSessionSelectStatement(set);
            }
        };
    }

    /**
     * Query the database for Session data of a server with kill and world data.
     *
     * @param serverUUID UUID of the Plan server.
     * @return Map: Player UUID - List of sessions on the server.
     */
    public static Query<Map<UUID, List<Session>>> fetchSessionsOfServer(UUID serverUUID) {
        String sql = SELECT_SESSIONS_STATEMENT +
                WHERE + SessionsTable.TABLE_NAME + "." + SessionsTable.SERVER_UUID + "=?" +
                ORDER_BY_SESSION_START_DESC;
        return new QueryStatement<Map<UUID, List<Session>>>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Map<UUID, List<Session>> processResults(ResultSet set) throws SQLException {
                List<Session> sessions = extractDataFromSessionSelectStatement(set);
                return SessionsMutator.sortByPlayers(sessions);
            }
        };
    }

    /**
     * Query the database for Session data of a player with kill and world data.
     *
     * @param playerUUID UUID of the Player.
     * @return Map: Server UUID - List of sessions on the server.
     */
    public static Query<Map<UUID, List<Session>>> fetchSessionsOfPlayer(UUID playerUUID) {
        String sql = SELECT_SESSIONS_STATEMENT +
                WHERE + SessionsTable.TABLE_NAME + "." + SessionsTable.USER_UUID + "=?" +
                ORDER_BY_SESSION_START_DESC;
        return new QueryStatement<Map<UUID, List<Session>>>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Map<UUID, List<Session>> processResults(ResultSet set) throws SQLException {
                List<Session> sessions = extractDataFromSessionSelectStatement(set);
                return SessionsMutator.sortByServers(sessions);
            }
        };
    }

    private static List<Session> extractDataFromSessionSelectStatement(ResultSet set) throws SQLException {
        // Server UUID - Player UUID - Session Start - Session
        Map<UUID, Map<UUID, SortedMap<Long, Session>>> tempSessionMap = new HashMap<>();

        // Utilities
        String[] gms = GMTimes.getGMKeyArray();
        Comparator<DateHolder> dateColderRecentComparator = new DateHolderRecentComparator();
        Comparator<Long> longRecentComparator = (one, two) -> Long.compare(two, one); // Descending order, most recent first.

        while (set.next()) {
            UUID serverUUID = UUID.fromString(set.getString(SessionsTable.SERVER_UUID));
            Map<UUID, SortedMap<Long, Session>> serverSessions = tempSessionMap.getOrDefault(serverUUID, new HashMap<>());

            UUID playerUUID = UUID.fromString(set.getString(SessionsTable.USER_UUID));
            SortedMap<Long, Session> playerSessions = serverSessions.getOrDefault(playerUUID, new TreeMap<>(longRecentComparator));

            long sessionStart = set.getLong(SessionsTable.SESSION_START);
            // id, uuid, serverUUID, sessionStart, sessionEnd, mobKills, deaths, afkTime
            Session session = playerSessions.getOrDefault(sessionStart, new Session(
                    set.getInt(SessionsTable.ID),
                    playerUUID,
                    serverUUID,
                    sessionStart,
                    set.getLong(SessionsTable.SESSION_END),
                    set.getInt(SessionsTable.MOB_KILLS),
                    set.getInt(SessionsTable.DEATHS),
                    set.getLong(SessionsTable.AFK_TIME)
            ));

            WorldTimes worldTimes = session.getUnsafe(SessionKeys.WORLD_TIMES);
            String worldName = set.getString(WorldTable.NAME);

            if (!worldTimes.contains(worldName)) {
                Map<String, Long> gmMap = new HashMap<>();
                gmMap.put(gms[0], set.getLong(WorldTimesTable.SURVIVAL));
                gmMap.put(gms[1], set.getLong(WorldTimesTable.CREATIVE));
                gmMap.put(gms[2], set.getLong(WorldTimesTable.ADVENTURE));
                gmMap.put(gms[3], set.getLong(WorldTimesTable.SPECTATOR));
                GMTimes gmTimes = new GMTimes(gmMap);
                worldTimes.setGMTimesForWorld(worldName, gmTimes);
            }

            String victimName = set.getString("victim_name");
            if (victimName != null) {
                UUID victim = UUID.fromString(set.getString(KillsTable.VICTIM_UUID));
                long date = set.getLong(KillsTable.DATE);
                String weapon = set.getString(KillsTable.WEAPON);
                List<PlayerKill> playerKills = session.getPlayerKills();
                playerKills.add(new PlayerKill(victim, weapon, date, victimName));
                playerKills.sort(dateColderRecentComparator);
            }

            playerSessions.put(sessionStart, session);
            serverSessions.put(playerUUID, playerSessions);
            tempSessionMap.put(serverUUID, serverSessions);
        }

        return tempSessionMap.values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(SortedMap::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}