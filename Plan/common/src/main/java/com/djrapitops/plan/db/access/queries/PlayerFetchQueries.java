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
package com.djrapitops.plan.db.access.queries;

import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.Ping;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.db.access.HasMoreThanZeroQueryStatement;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.tables.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Static method class for queries that return information related to a single player.
 *
 * @author Rsl1122
 */
public class PlayerFetchQueries {

    private PlayerFetchQueries() {
        /* static method class */
    }

    /**
     * Query Player's name by player's UUID.
     *
     * @param playerUUID UUID of the player.
     * @return Optional, Name if found.
     */
    public static Query<Optional<String>> playerUserName(UUID playerUUID) {
        String sql = "SELECT " + UsersTable.USER_NAME +
                " FROM " + UsersTable.TABLE_NAME +
                " WHERE " + UsersTable.USER_UUID + "=?";
        return new QueryStatement<Optional<String>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Optional<String> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getString(UsersTable.USER_NAME));
                }
                return Optional.empty();
            }
        };
    }

    /**
     * Query Player's GeoInfo by player's UUID.
     *
     * @param playerUUID UUID of the player.
     * @return List of {@link GeoInfo}, empty if none are found.
     */
    public static Query<List<GeoInfo>> playerGeoInfo(UUID playerUUID) {
        String sql = "SELECT DISTINCT * FROM " + GeoInfoTable.TABLE_NAME +
                " WHERE " + GeoInfoTable.USER_UUID + "=?";

        return new QueryStatement<List<GeoInfo>>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public List<GeoInfo> processResults(ResultSet set) throws SQLException {
                List<GeoInfo> geoInfo = new ArrayList<>();
                while (set.next()) {
                    String ip = set.getString(GeoInfoTable.IP);
                    String geolocation = set.getString(GeoInfoTable.GEOLOCATION);
                    String ipHash = set.getString(GeoInfoTable.IP_HASH);
                    long lastUsed = set.getLong(GeoInfoTable.LAST_USED);
                    geoInfo.add(new GeoInfo(ip, geolocation, lastUsed, ipHash));
                }
                return geoInfo;
            }
        };
    }

    public static Query<List<UserInfo>> playerServerSpecificUserInformation(UUID playerUUID) {
        String sql = "SELECT " +
                UserInfoTable.TABLE_NAME + "." + UserInfoTable.REGISTERED + ", " +
                UserInfoTable.BANNED + ", " +
                UserInfoTable.OP + ", " +
                UserInfoTable.SERVER_UUID +
                " FROM " + UserInfoTable.TABLE_NAME +
                " WHERE " + UserInfoTable.TABLE_NAME + "." + UserInfoTable.USER_UUID + "=?";

        return new QueryStatement<List<UserInfo>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public List<UserInfo> processResults(ResultSet set) throws SQLException {
                List<UserInfo> userInformation = new ArrayList<>();
                while (set.next()) {
                    long registered = set.getLong(UserInfoTable.REGISTERED);
                    boolean op = set.getBoolean(UserInfoTable.OP);
                    boolean banned = set.getBoolean(UserInfoTable.BANNED);
                    UUID serverUUID = UUID.fromString(set.getString(UserInfoTable.SERVER_UUID));
                    userInformation.add(new UserInfo(playerUUID, serverUUID, registered, op, banned));
                }
                return userInformation;
            }
        };
    }

    /**
     * Check if the player's BaseUser is registered.
     *
     * @param playerUUID UUID of the player.
     * @return True if the player's BaseUser is found
     */
    public static Query<Boolean> isPlayerRegistered(UUID playerUUID) {
        String sql = "SELECT COUNT(1) as c FROM " + UsersTable.TABLE_NAME +
                " WHERE " + UsersTable.USER_UUID + "=?";
        return new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }
        };
    }

    /**
     * Check if the player's UserInfo is registered.
     *
     * @param playerUUID UUID of the player.
     * @param serverUUID UUID of the Plan server.
     * @return True if the player's UserInfo is found
     */
    public static Query<Boolean> isPlayerRegisteredOnServer(UUID playerUUID, UUID serverUUID) {
        String sql = "SELECT COUNT(1) as c FROM " + UserInfoTable.TABLE_NAME +
                " WHERE " + UserInfoTable.USER_UUID + "=?" +
                " AND " + UserInfoTable.SERVER_UUID + "=?";
        return new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
                statement.setString(2, serverUUID.toString());
            }
        };
    }

    public static Query<List<Ping>> playerPingData(UUID playerUUID) {
        String sql = "SELECT * FROM " + PingTable.TABLE_NAME +
                " WHERE " + PingTable.USER_UUID + "=?";

        return new QueryStatement<List<Ping>>(sql, 10000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public List<Ping> processResults(ResultSet set) throws SQLException {
                List<Ping> pings = new ArrayList<>();

                while (set.next()) {
                    pings.add(new Ping(
                                    set.getLong(PingTable.DATE),
                                    UUID.fromString(set.getString(PingTable.SERVER_UUID)),
                                    set.getInt(PingTable.MIN_PING),
                                    set.getInt(PingTable.MAX_PING),
                                    set.getDouble(PingTable.AVG_PING)
                            )
                    );
                }

                return pings;
            }
        };
    }

    public static Query<Optional<Nickname>> playersLastSeenNickname(UUID playerUUID, UUID serverUUID) {
        String subQuery = "SELECT MAX(" + NicknamesTable.LAST_USED + ") FROM " + NicknamesTable.TABLE_NAME +
                " WHERE " + NicknamesTable.USER_UUID + "=?" +
                " AND " + NicknamesTable.SERVER_UUID + "=?" +
                " GROUP BY " + NicknamesTable.USER_UUID;
        String sql = "SELECT " + NicknamesTable.LAST_USED + ", " +
                NicknamesTable.NICKNAME + " FROM " + NicknamesTable.TABLE_NAME +
                " WHERE " + NicknamesTable.USER_UUID + "=?" +
                " AND " + NicknamesTable.SERVER_UUID + "=?" +
                " AND " + NicknamesTable.LAST_USED + "=(" + subQuery + ")";
        return new QueryStatement<Optional<Nickname>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
                statement.setString(2, serverUUID.toString());
                statement.setString(3, playerUUID.toString());
                statement.setString(4, serverUUID.toString());
            }

            @Override
            public Optional<Nickname> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(new Nickname(
                            set.getString(NicknamesTable.NICKNAME),
                            set.getLong(NicknamesTable.LAST_USED),
                            serverUUID
                    ));
                }
                return Optional.empty();
            }
        };
    }

    public static Query<List<Nickname>> playersNicknameInformation(UUID playerUUID) {
        String sql = "SELECT " +
                NicknamesTable.NICKNAME + ", " +
                NicknamesTable.LAST_USED + ", " +
                NicknamesTable.SERVER_UUID +
                " FROM " + NicknamesTable.TABLE_NAME +
                " WHERE (" + NicknamesTable.USER_UUID + "=?)";

        return new QueryStatement<List<Nickname>>(sql, 5000) {

            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public List<Nickname> processResults(ResultSet set) throws SQLException {
                List<Nickname> nicknames = new ArrayList<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(NicknamesTable.SERVER_UUID));
                    String nickname = set.getString(NicknamesTable.NICKNAME);
                    nicknames.add(new Nickname(nickname, set.getLong(NicknamesTable.LAST_USED), serverUUID));
                }
                return nicknames;
            }
        };
    }
}