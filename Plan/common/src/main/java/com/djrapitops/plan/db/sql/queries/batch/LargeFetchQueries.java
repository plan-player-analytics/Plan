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
package com.djrapitops.plan.db.sql.queries.batch;

import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.tables.*;
import com.djrapitops.plan.system.info.server.Server;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Static method class for queries that use large amount of memory.
 *
 * @author Rsl1122
 */
public class LargeFetchQueries {

    private LargeFetchQueries() {
        /* Static method class */
    }

    /**
     * Query database for all command usage data.
     *
     * @return Multi map: Server UUID - (Command name - Usage count)
     */
    public static Query<Map<UUID, Map<String, Integer>>> fetchAllCommandUsageData() {
        String serverIDColumn = ServerTable.TABLE_NAME + "." + ServerTable.Col.SERVER_ID;
        String serverUUIDColumn = ServerTable.TABLE_NAME + "." + ServerTable.Col.SERVER_UUID + " as s_uuid";
        String sql = "SELECT " +
                CommandUseTable.Col.COMMAND + ", " +
                CommandUseTable.Col.TIMES_USED + ", " +
                serverUUIDColumn +
                " FROM " + CommandUseTable.TABLE_NAME +
                " INNER JOIN " + ServerTable.TABLE_NAME + " on " + serverIDColumn + "=" + CommandUseTable.Col.SERVER_ID;

        return new QueryAllStatement<Map<UUID, Map<String, Integer>>>(sql, 10000) {
            @Override
            public Map<UUID, Map<String, Integer>> processResults(ResultSet set) throws SQLException {
                Map<UUID, Map<String, Integer>> map = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));

                    Map<String, Integer> serverMap = map.getOrDefault(serverUUID, new HashMap<>());

                    String command = set.getString(CommandUseTable.Col.COMMAND.get());
                    int timesUsed = set.getInt(CommandUseTable.Col.TIMES_USED.get());

                    serverMap.put(command, timesUsed);
                    map.put(serverUUID, serverMap);
                }
                return map;
            }
        };
    }

    /**
     * Query database for all GeoInfo data.
     *
     * @return Map: Server UUID - List of GeoInfo
     */
    public static Query<Map<UUID, List<GeoInfo>>> fetchAllGeoInfoData() {
        String sql = "SELECT " +
                GeoInfoTable.Col.IP + ", " +
                GeoInfoTable.Col.GEOLOCATION + ", " +
                GeoInfoTable.Col.LAST_USED + ", " +
                GeoInfoTable.Col.IP_HASH + ", " +
                GeoInfoTable.Col.UUID +
                " FROM " + GeoInfoTable.TABLE_NAME;

        return new QueryAllStatement<Map<UUID, List<GeoInfo>>>(sql, 50000) {
            @Override
            public Map<UUID, List<GeoInfo>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<GeoInfo>> geoLocations = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(GeoInfoTable.Col.UUID.get()));

                    List<GeoInfo> userGeoInfo = geoLocations.getOrDefault(uuid, new ArrayList<>());

                    String ip = set.getString(GeoInfoTable.Col.IP.get());
                    String geolocation = set.getString(GeoInfoTable.Col.GEOLOCATION.get());
                    String ipHash = set.getString(GeoInfoTable.Col.IP_HASH.get());
                    long lastUsed = set.getLong(GeoInfoTable.Col.LAST_USED.get());
                    userGeoInfo.add(new GeoInfo(ip, geolocation, lastUsed, ipHash));

                    geoLocations.put(uuid, userGeoInfo);
                }
                return geoLocations;
            }
        };
    }

    /**
     * Query database for all Kill data.
     *
     * @return Map: Session ID - List of PlayerKills
     */
    public static Query<Map<Integer, List<PlayerKill>>> fetchAllPlayerKillDataBySessionID() {
        String usersUUIDColumn = UsersTable.TABLE_NAME + "." + UsersTable.Col.UUID;
        String usersNameColumn = UsersTable.TABLE_NAME + "." + UsersTable.Col.USER_NAME + " as victim_name";
        String sql = "SELECT " +
                KillsTable.Col.SESSION_ID + ", " +
                KillsTable.Col.DATE + ", " +
                KillsTable.Col.WEAPON + ", " +
                KillsTable.Col.VICTIM_UUID + ", " +
                usersNameColumn +
                " FROM " + KillsTable.TABLE_NAME +
                " INNER JOIN " + UsersTable.TABLE_NAME + " on " + usersUUIDColumn + "=" + KillsTable.Col.VICTIM_UUID;

        return new QueryAllStatement<Map<Integer, List<PlayerKill>>>(sql, 50000) {
            @Override
            public Map<Integer, List<PlayerKill>> processResults(ResultSet set) throws SQLException {
                Map<Integer, List<PlayerKill>> allPlayerKills = new HashMap<>();
                while (set.next()) {
                    int sessionID = set.getInt(KillsTable.Col.SESSION_ID.get());

                    List<PlayerKill> playerKills = allPlayerKills.getOrDefault(sessionID, new ArrayList<>());

                    UUID victim = UUID.fromString(set.getString(KillsTable.Col.VICTIM_UUID.get()));
                    String victimName = set.getString("victim_name");
                    long date = set.getLong(KillsTable.Col.DATE.get());
                    String weapon = set.getString(KillsTable.Col.WEAPON.get());
                    playerKills.add(new PlayerKill(victim, weapon, date, victimName));

                    allPlayerKills.put(sessionID, playerKills);
                }
                return allPlayerKills;
            }
        };
    }

    /**
     * Query database for all nickname data.
     *
     * @return Multimap: Server UUID - (Player UUID - List of nicknames)
     */
    public static Query<Map<UUID, Map<UUID, List<Nickname>>>> fetchAllNicknameData() {
        String sql = "SELECT " +
                NicknamesTable.Col.NICKNAME + ", " +
                NicknamesTable.Col.LAST_USED + ", " +
                NicknamesTable.Col.UUID + ", " +
                NicknamesTable.Col.SERVER_UUID +
                " FROM " + NicknamesTable.TABLE_NAME;

        return new QueryAllStatement<Map<UUID, Map<UUID, List<Nickname>>>>(sql, 5000) {
            @Override
            public Map<UUID, Map<UUID, List<Nickname>>> processResults(ResultSet set) throws SQLException {
                Map<UUID, Map<UUID, List<Nickname>>> map = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(NicknamesTable.Col.SERVER_UUID.get()));
                    UUID uuid = UUID.fromString(set.getString(NicknamesTable.Col.UUID.get()));

                    Map<UUID, List<Nickname>> serverMap = map.getOrDefault(serverUUID, new HashMap<>());
                    List<Nickname> nicknames = serverMap.getOrDefault(uuid, new ArrayList<>());

                    nicknames.add(new Nickname(
                            set.getString(NicknamesTable.Col.NICKNAME.get()),
                            set.getLong(NicknamesTable.Col.LAST_USED.get()),
                            serverUUID
                    ));

                    serverMap.put(uuid, nicknames);
                    map.put(serverUUID, serverMap);
                }
                return map;
            }
        };
    }

    /**
     * Query database for all nickname data.
     *
     * @return Map: Player UUID - List of nicknames.
     */
    public static Query<Map<UUID, List<Nickname>>> fetchAllNicknameDataByPlayerUUIDs() {
        String sql = "SELECT " +
                NicknamesTable.Col.NICKNAME + ", " +
                NicknamesTable.Col.LAST_USED + ", " +
                NicknamesTable.Col.UUID + ", " +
                NicknamesTable.Col.SERVER_UUID +
                " FROM " + NicknamesTable.TABLE_NAME;
        return new QueryAllStatement<Map<UUID, List<Nickname>>>(sql, 5000) {
            @Override
            public Map<UUID, List<Nickname>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<Nickname>> map = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(NicknamesTable.Col.UUID.get()));
                    UUID serverUUID = UUID.fromString(set.getString(NicknamesTable.Col.SERVER_UUID.get()));
                    List<Nickname> nicknames = map.computeIfAbsent(uuid, x -> new ArrayList<>());
                    nicknames.add(new Nickname(
                            set.getString(NicknamesTable.Col.NICKNAME.get()), set.getLong(NicknamesTable.Col.LAST_USED.get()), serverUUID
                    ));
                }
                return map;
            }
        };
    }

    /**
     * Query database for all Ping data.
     *
     * @return Map: Player UUID - List of ping data.
     */
    public static Query<Map<UUID, List<Ping>>> fetchAllPingData() {
        String sql = "SELECT " +
                PingTable.Col.DATE + ", " +
                PingTable.Col.MAX_PING + ", " +
                PingTable.Col.MIN_PING + ", " +
                PingTable.Col.AVG_PING + ", " +
                PingTable.Col.UUID + ", " +
                PingTable.Col.SERVER_UUID +
                " FROM " + PingTable.TABLE_NAME;
        return new QueryAllStatement<Map<UUID, List<Ping>>>(sql, 100000) {
            @Override
            public Map<UUID, List<Ping>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<Ping>> userPings = new HashMap<>();

                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(PingTable.Col.UUID.get()));
                    UUID serverUUID = UUID.fromString(set.getString(PingTable.Col.SERVER_UUID.get()));
                    long date = set.getLong(PingTable.Col.DATE.get());
                    double avgPing = set.getDouble(PingTable.Col.AVG_PING.get());
                    int minPing = set.getInt(PingTable.Col.MIN_PING.get());
                    int maxPing = set.getInt(PingTable.Col.MAX_PING.get());

                    List<Ping> pings = userPings.getOrDefault(uuid, new ArrayList<>());
                    pings.add(new Ping(date, serverUUID,
                            minPing,
                            maxPing,
                            avgPing));
                    userPings.put(uuid, pings);
                }

                return userPings;
            }
        };
    }

    /**
     * Query database for all Plan WebUsers.
     *
     * @return Set of Plan WebUsers.
     */
    public static Query<List<WebUser>> fetchAllPlanWebUsers() {
        String sql = "SELECT * FROM " + SecurityTable.TABLE_NAME + " ORDER BY " + SecurityTable.Col.PERMISSION_LEVEL + " ASC";

        return new QueryAllStatement<List<WebUser>>(sql, 5000) {
            @Override
            public List<WebUser> processResults(ResultSet set) throws SQLException {
                List<WebUser> list = new ArrayList<>();
                while (set.next()) {
                    String user = set.getString(SecurityTable.Col.USERNAME.get());
                    String saltedPassHash = set.getString(SecurityTable.Col.SALT_PASSWORD_HASH.get());
                    int permissionLevel = set.getInt(SecurityTable.Col.PERMISSION_LEVEL.get());
                    WebUser info = new WebUser(user, saltedPassHash, permissionLevel);
                    list.add(info);
                }
                return list;
            }
        };
    }

    /**
     * Query database for all Plan server information.
     *
     * @return Map: Server UUID - Plan Server Information
     */
    public static Query<Map<UUID, Server>> fetchPlanServerInformation() {
        String sql = "SELECT * FROM " + ServerTable.TABLE_NAME + " WHERE " + ServerTable.Col.INSTALLED + "=?";

        return new QueryStatement<Map<UUID, Server>>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, true);
            }

            @Override
            public Map<UUID, Server> processResults(ResultSet set) throws SQLException {
                Map<UUID, Server> servers = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(ServerTable.Col.SERVER_UUID.get()));
                    servers.put(serverUUID, new Server(
                            set.getInt(ServerTable.Col.SERVER_ID.get()),
                            serverUUID,
                            set.getString(ServerTable.Col.NAME.get()),
                            set.getString(ServerTable.Col.WEBSERVER_ADDRESS.get()),
                            set.getInt(ServerTable.Col.MAX_PLAYERS.get())));
                }
                return servers;
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
                SessionsTable.Col.ID + ", " +
                SessionsTable.Col.UUID + ", " +
                SessionsTable.Col.SERVER_UUID + ", " +
                SessionsTable.Col.SESSION_START + ", " +
                SessionsTable.Col.SESSION_END + ", " +
                SessionsTable.Col.DEATHS + ", " +
                SessionsTable.Col.MOB_KILLS + ", " +
                SessionsTable.Col.AFK_TIME +
                " FROM " + SessionsTable.TABLE_NAME;

        return new QueryAllStatement<Map<UUID, Map<UUID, List<Session>>>>(sql, 20000) {
            @Override
            public Map<UUID, Map<UUID, List<Session>>> processResults(ResultSet set) throws SQLException {
                Map<UUID, Map<UUID, List<Session>>> map = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(SessionsTable.Col.SERVER_UUID.get()));
                    UUID uuid = UUID.fromString(set.getString(SessionsTable.Col.UUID.get()));

                    Map<UUID, List<Session>> sessionsByUser = map.getOrDefault(serverUUID, new HashMap<>());
                    List<Session> sessions = sessionsByUser.getOrDefault(uuid, new ArrayList<>());

                    long start = set.getLong(SessionsTable.Col.SESSION_START.get());
                    long end = set.getLong(SessionsTable.Col.SESSION_END.get());

                    int deaths = set.getInt(SessionsTable.Col.DEATHS.get());
                    int mobKills = set.getInt(SessionsTable.Col.MOB_KILLS.get());
                    int id = set.getInt(SessionsTable.Col.ID.get());

                    long timeAFK = set.getLong(SessionsTable.Col.AFK_TIME.get());

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
     * Query database for TPS data.
     *
     * @return Map: Server UUID - List of TPS data
     */
    public static Query<Map<UUID, List<TPS>>> fetchAllTPSData() {
        String serverIDColumn = ServerTable.TABLE_NAME + "." + ServerTable.Col.SERVER_ID;
        String serverUUIDColumn = ServerTable.TABLE_NAME + "." + ServerTable.Col.SERVER_UUID + " as s_uuid";
        String sql = "SELECT " +
                TPSTable.Col.DATE + ", " +
                TPSTable.Col.TPS + ", " +
                TPSTable.Col.PLAYERS_ONLINE + ", " +
                TPSTable.Col.CPU_USAGE + ", " +
                TPSTable.Col.RAM_USAGE + ", " +
                TPSTable.Col.ENTITIES + ", " +
                TPSTable.Col.CHUNKS + ", " +
                TPSTable.Col.FREE_DISK + ", " +
                serverUUIDColumn +
                " FROM " + TPSTable.TABLE_NAME +
                " INNER JOIN " + ServerTable.TABLE_NAME + " on " + serverIDColumn + "=" + TPSTable.Col.SERVER_ID;

        return new QueryAllStatement<Map<UUID, List<TPS>>>(sql, 50000) {
            @Override
            public Map<UUID, List<TPS>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<TPS>> serverMap = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));

                    List<TPS> tpsList = serverMap.getOrDefault(serverUUID, new ArrayList<>());

                    TPS tps = TPSBuilder.get()
                            .date(set.getLong(TPSTable.Col.DATE.get()))
                            .tps(set.getDouble(TPSTable.Col.TPS.get()))
                            .playersOnline(set.getInt(TPSTable.Col.PLAYERS_ONLINE.get()))
                            .usedCPU(set.getDouble(TPSTable.Col.CPU_USAGE.get()))
                            .usedMemory(set.getLong(TPSTable.Col.RAM_USAGE.get()))
                            .entities(set.getInt(TPSTable.Col.ENTITIES.get()))
                            .chunksLoaded(set.getInt(TPSTable.Col.CHUNKS.get()))
                            .freeDiskSpace(set.getLong(TPSTable.Col.FREE_DISK.get()))
                            .toTPS();

                    tpsList.add(tps);
                    serverMap.put(serverUUID, tpsList);
                }
                return serverMap;
            }
        };
    }
}