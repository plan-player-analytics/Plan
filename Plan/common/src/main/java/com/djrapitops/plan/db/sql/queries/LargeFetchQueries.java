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
package com.djrapitops.plan.db.sql.queries;

import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.parsing.Select;
import com.djrapitops.plan.db.sql.tables.*;
import com.djrapitops.plan.system.info.server.Server;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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
        String serverIDColumn = ServerTable.TABLE_NAME + "." + ServerTable.SERVER_ID;
        String serverUUIDColumn = ServerTable.TABLE_NAME + "." + ServerTable.SERVER_UUID + " as s_uuid";
        String sql = "SELECT " +
                CommandUseTable.COMMAND + ", " +
                CommandUseTable.TIMES_USED + ", " +
                serverUUIDColumn +
                " FROM " + CommandUseTable.TABLE_NAME +
                " INNER JOIN " + ServerTable.TABLE_NAME + " on " + serverIDColumn + "=" + CommandUseTable.SERVER_ID;

        return new QueryAllStatement<Map<UUID, Map<String, Integer>>>(sql, 10000) {
            @Override
            public Map<UUID, Map<String, Integer>> processResults(ResultSet set) throws SQLException {
                Map<UUID, Map<String, Integer>> map = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));

                    Map<String, Integer> serverMap = map.getOrDefault(serverUUID, new HashMap<>());

                    String command = set.getString(CommandUseTable.COMMAND);
                    int timesUsed = set.getInt(CommandUseTable.TIMES_USED);

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
     * @return Map: Player UUID - List of GeoInfo
     */
    public static Query<Map<UUID, List<GeoInfo>>> fetchAllGeoInfoData() {
        String sql = "SELECT " +
                GeoInfoTable.IP + ", " +
                GeoInfoTable.GEOLOCATION + ", " +
                GeoInfoTable.LAST_USED + ", " +
                GeoInfoTable.IP_HASH + ", " +
                GeoInfoTable.USER_UUID +
                " FROM " + GeoInfoTable.TABLE_NAME;

        return new QueryAllStatement<Map<UUID, List<GeoInfo>>>(sql, 50000) {
            @Override
            public Map<UUID, List<GeoInfo>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<GeoInfo>> geoLocations = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(GeoInfoTable.USER_UUID));

                    List<GeoInfo> userGeoInfo = geoLocations.getOrDefault(uuid, new ArrayList<>());

                    String ip = set.getString(GeoInfoTable.IP);
                    String geolocation = set.getString(GeoInfoTable.GEOLOCATION);
                    String ipHash = set.getString(GeoInfoTable.IP_HASH);
                    long lastUsed = set.getLong(GeoInfoTable.LAST_USED);
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
     * Query database for all nickname data.
     *
     * @return Multimap: Server UUID - (Player UUID - List of nicknames)
     */
    public static Query<Map<UUID, Map<UUID, List<Nickname>>>> fetchAllNicknameData() {
        String sql = "SELECT " +
                NicknamesTable.NICKNAME + ", " +
                NicknamesTable.LAST_USED + ", " +
                NicknamesTable.USER_UUID + ", " +
                NicknamesTable.SERVER_UUID +
                " FROM " + NicknamesTable.TABLE_NAME;

        return new QueryAllStatement<Map<UUID, Map<UUID, List<Nickname>>>>(sql, 5000) {
            @Override
            public Map<UUID, Map<UUID, List<Nickname>>> processResults(ResultSet set) throws SQLException {
                Map<UUID, Map<UUID, List<Nickname>>> map = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(NicknamesTable.SERVER_UUID));
                    UUID uuid = UUID.fromString(set.getString(NicknamesTable.USER_UUID));

                    Map<UUID, List<Nickname>> serverMap = map.getOrDefault(serverUUID, new HashMap<>());
                    List<Nickname> nicknames = serverMap.getOrDefault(uuid, new ArrayList<>());

                    nicknames.add(new Nickname(
                            set.getString(NicknamesTable.NICKNAME),
                            set.getLong(NicknamesTable.LAST_USED),
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
                NicknamesTable.NICKNAME + ", " +
                NicknamesTable.LAST_USED + ", " +
                NicknamesTable.USER_UUID + ", " +
                NicknamesTable.SERVER_UUID +
                " FROM " + NicknamesTable.TABLE_NAME;
        return new QueryAllStatement<Map<UUID, List<Nickname>>>(sql, 5000) {
            @Override
            public Map<UUID, List<Nickname>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<Nickname>> map = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(NicknamesTable.USER_UUID));
                    UUID serverUUID = UUID.fromString(set.getString(NicknamesTable.SERVER_UUID));
                    List<Nickname> nicknames = map.computeIfAbsent(uuid, x -> new ArrayList<>());
                    nicknames.add(new Nickname(
                            set.getString(NicknamesTable.NICKNAME), set.getLong(NicknamesTable.LAST_USED), serverUUID
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
                PingTable.DATE + ", " +
                PingTable.MAX_PING + ", " +
                PingTable.MIN_PING + ", " +
                PingTable.AVG_PING + ", " +
                PingTable.USER_UUID + ", " +
                PingTable.SERVER_UUID +
                " FROM " + PingTable.TABLE_NAME;
        return new QueryAllStatement<Map<UUID, List<Ping>>>(sql, 100000) {
            @Override
            public Map<UUID, List<Ping>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<Ping>> userPings = new HashMap<>();

                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(PingTable.USER_UUID));
                    UUID serverUUID = UUID.fromString(set.getString(PingTable.SERVER_UUID));
                    long date = set.getLong(PingTable.DATE);
                    double avgPing = set.getDouble(PingTable.AVG_PING);
                    int minPing = set.getInt(PingTable.MIN_PING);
                    int maxPing = set.getInt(PingTable.MAX_PING);

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
     * @return List of Plan WebUsers.
     */
    public static Query<List<WebUser>> fetchAllPlanWebUsers() {
        String sql = "SELECT * FROM " + SecurityTable.TABLE_NAME + " ORDER BY " + SecurityTable.PERMISSION_LEVEL + " ASC";

        return new QueryAllStatement<List<WebUser>>(sql, 5000) {
            @Override
            public List<WebUser> processResults(ResultSet set) throws SQLException {
                List<WebUser> list = new ArrayList<>();
                while (set.next()) {
                    String user = set.getString(SecurityTable.USERNAME);
                    String saltedPassHash = set.getString(SecurityTable.SALT_PASSWORD_HASH);
                    int permissionLevel = set.getInt(SecurityTable.PERMISSION_LEVEL);
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
        String sql = "SELECT * FROM " + ServerTable.TABLE_NAME + " WHERE " + ServerTable.INSTALLED + "=?";

        return new QueryStatement<Map<UUID, Server>>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, true);
            }

            @Override
            public Map<UUID, Server> processResults(ResultSet set) throws SQLException {
                Map<UUID, Server> servers = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(ServerTable.SERVER_UUID));
                    servers.put(serverUUID, new Server(
                            set.getInt(ServerTable.SERVER_ID),
                            serverUUID,
                            set.getString(ServerTable.NAME),
                            set.getString(ServerTable.WEB_ADDRESS),
                            set.getInt(ServerTable.MAX_PLAYERS)));
                }
                return servers;
            }
        };
    }

    public static Query<Collection<Server>> fetchPlanServerInformationCollection() {
        return db -> db.query(fetchPlanServerInformation()).values();
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

    /**
     * Query database for TPS data.
     *
     * @return Map: Server UUID - List of TPS data
     */
    public static Query<Map<UUID, List<TPS>>> fetchAllTPSData() {
        String serverIDColumn = ServerTable.TABLE_NAME + "." + ServerTable.SERVER_ID;
        String serverUUIDColumn = ServerTable.TABLE_NAME + "." + ServerTable.SERVER_UUID + " as s_uuid";
        String sql = "SELECT " +
                TPSTable.DATE + ", " +
                TPSTable.TPS + ", " +
                TPSTable.PLAYERS_ONLINE + ", " +
                TPSTable.CPU_USAGE + ", " +
                TPSTable.RAM_USAGE + ", " +
                TPSTable.ENTITIES + ", " +
                TPSTable.CHUNKS + ", " +
                TPSTable.FREE_DISK + ", " +
                serverUUIDColumn +
                " FROM " + TPSTable.TABLE_NAME +
                " INNER JOIN " + ServerTable.TABLE_NAME + " on " + serverIDColumn + "=" + TPSTable.SERVER_ID;

        return new QueryAllStatement<Map<UUID, List<TPS>>>(sql, 50000) {
            @Override
            public Map<UUID, List<TPS>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<TPS>> serverMap = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString("s_uuid"));

                    List<TPS> tpsList = serverMap.getOrDefault(serverUUID, new ArrayList<>());

                    TPS tps = TPSBuilder.get()
                            .date(set.getLong(TPSTable.DATE))
                            .tps(set.getDouble(TPSTable.TPS))
                            .playersOnline(set.getInt(TPSTable.PLAYERS_ONLINE))
                            .usedCPU(set.getDouble(TPSTable.CPU_USAGE))
                            .usedMemory(set.getLong(TPSTable.RAM_USAGE))
                            .entities(set.getInt(TPSTable.ENTITIES))
                            .chunksLoaded(set.getInt(TPSTable.CHUNKS))
                            .freeDiskSpace(set.getLong(TPSTable.FREE_DISK))
                            .toTPS();

                    tpsList.add(tps);
                    serverMap.put(serverUUID, tpsList);
                }
                return serverMap;
            }
        };
    }

    /**
     * Query database for user information.
     * <p>
     * The user information does not contain player names.
     *
     * @return Map: Server UUID - List of user information
     */
    public static Query<Map<UUID, List<UserInfo>>> fetchPerServerUserInformation() {
        String sql = "SELECT " +
                UserInfoTable.REGISTERED + ", " +
                UserInfoTable.BANNED + ", " +
                UserInfoTable.OP + ", " +
                UserInfoTable.USER_UUID + ", " +
                UserInfoTable.SERVER_UUID +
                " FROM " + UserInfoTable.TABLE_NAME;

        return new QueryAllStatement<Map<UUID, List<UserInfo>>>(sql, 50000) {
            @Override
            public Map<UUID, List<UserInfo>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<UserInfo>> serverMap = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(UserInfoTable.SERVER_UUID));
                    UUID uuid = UUID.fromString(set.getString(UserInfoTable.USER_UUID));

                    List<UserInfo> userInfos = serverMap.getOrDefault(serverUUID, new ArrayList<>());

                    long registered = set.getLong(UserInfoTable.REGISTERED);
                    boolean banned = set.getBoolean(UserInfoTable.BANNED);
                    boolean op = set.getBoolean(UserInfoTable.OP);

                    userInfos.add(new UserInfo(uuid, "", registered, op, banned));

                    serverMap.put(serverUUID, userInfos);
                }
                return serverMap;
            }
        };
    }

    /**
     * Query database for common user information.
     * <p>
     * This is the base for any user information.
     *
     * @return Map: Player UUID - BaseUser
     */
    public static Query<Collection<BaseUser>> fetchAllCommonUserInformation() {
        String sql = Select.all(UsersTable.TABLE_NAME).toString();

        return new QueryAllStatement<Collection<BaseUser>>(sql, 20000) {
            @Override
            public Collection<BaseUser> processResults(ResultSet set) throws SQLException {
                Collection<BaseUser> users = new HashSet<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(UsersTable.USER_UUID));
                    String name = set.getString(UsersTable.USER_NAME);
                    long registered = set.getLong(UsersTable.REGISTERED);
                    int kicked = set.getInt(UsersTable.TIMES_KICKED);

                    users.add(new BaseUser(uuid, name, registered, kicked));
                }
                return users;
            }
        };
    }

    /**
     * Query database for world names.
     *
     * @return Map: Server UUID - List of world names
     */
    public static Query<Map<UUID, Collection<String>>> fetchAllWorldNames() {
        String sql = "SELECT * FROM " + WorldTable.TABLE_NAME;

        return new QueryAllStatement<Map<UUID, Collection<String>>>(sql, 1000) {
            @Override
            public Map<UUID, Collection<String>> processResults(ResultSet set) throws SQLException {
                Map<UUID, Collection<String>> worldMap = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(WorldTable.SERVER_UUID));
                    Collection<String> worlds = worldMap.getOrDefault(serverUUID, new HashSet<>());
                    worlds.add(set.getString(WorldTable.NAME));
                    worldMap.put(serverUUID, worlds);
                }
                return worldMap;
            }
        };
    }
}