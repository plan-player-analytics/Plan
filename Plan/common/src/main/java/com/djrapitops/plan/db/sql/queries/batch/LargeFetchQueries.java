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

import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryAllStatement;
import com.djrapitops.plan.db.sql.tables.*;

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

}