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
package com.djrapitops.plan.storage.database.queries;

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.*;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.AND;
import static com.djrapitops.plan.storage.database.sql.building.Sql.WHERE;

/**
 * Static method class for single item store queries.
 *
 * @author AuroraLS3
 */
public class DataStoreQueries {

    private DataStoreQueries() {
        /* static method class */
    }

    /**
     * Store a finished session in the database.
     *
     * @param session a finished session
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     */
    public static Executable storeSession(FinishedSession session) {
        return connection -> {
            storeSessionInformation(session).execute(connection);
            storeSessionKills(session).execute(connection);
            return storeSessionWorldTimes(session).execute(connection);
        };
    }

    private static Executable storeSessionInformation(FinishedSession session) {
        return new ExecStatement(SessionsTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, session.getPlayerUUID().toString());
                statement.setLong(2, session.getStart());
                statement.setLong(3, session.getEnd());
                statement.setInt(4, session.getDeathCount());
                statement.setInt(5, session.getMobKillCount());
                statement.setLong(6, session.getAfkTime());
                statement.setString(7, session.getServerUUID().toString());
                statement.setString(8, session.getExtraData(JoinAddress.class)
                        .map(JoinAddress::getAddress).orElse(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP));
            }
        };
    }

    private static Executable storeSessionKills(FinishedSession session) {
        return new ExecBatchStatement(KillsTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                KillsTable.addSessionKillsToBatch(statement, session);
            }
        };
    }

    public static Executable insertWorldName(ServerUUID serverUUID, String worldName) {
        return new ExecStatement(WorldTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, StringUtils.truncate(worldName, 100));
                statement.setString(2, serverUUID.toString());
            }
        };
    }

    private static Executable storeSessionWorldTimes(FinishedSession session) {
        return new ExecBatchStatement(WorldTimesTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                WorldTimesTable.addSessionWorldTimesToBatch(statement, session, GMTimes.getGMKeyArray());
            }
        };
    }

    /**
     * Store player's Geo Information in the database.
     *
     * @param playerUUID UUID of the player.
     * @param geoInfo    GeoInfo of the player.
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     */
    public static Executable storeGeoInfo(UUID playerUUID, GeoInfo geoInfo) {
        return connection -> {
            if (!updateGeoInfo(playerUUID, geoInfo).execute(connection)) {
                return insertGeoInfo(playerUUID, geoInfo).execute(connection);
            }
            return false;
        };
    }

    private static Executable updateGeoInfo(UUID playerUUID, GeoInfo geoInfo) {
        return new ExecStatement(GeoInfoTable.UPDATE_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, geoInfo.getDate());
                statement.setString(2, playerUUID.toString());
                statement.setString(3, geoInfo.getGeolocation());
            }
        };
    }

    private static Executable insertGeoInfo(UUID playerUUID, GeoInfo geoInfo) {
        return new ExecStatement(GeoInfoTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
                statement.setString(2, geoInfo.getGeolocation());
                statement.setLong(3, geoInfo.getDate());
            }
        };
    }

    /**
     * Store a BaseUser for the player in the database.
     *
     * @param playerUUID UUID of the player.
     * @param registered Time the player registered on the server for the first time.
     * @param playerName Name of the player.
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     */
    public static ExecStatement registerBaseUser(UUID playerUUID, long registered, String playerName) {
        return new ExecStatement(UsersTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
                statement.setString(2, playerName);
                statement.setLong(3, registered);
                statement.setInt(4, 0); // times kicked
            }
        };
    }

    /**
     * Update player's name in the database in case they have changed it.
     *
     * @param playerUUID UUID of the player.
     * @param playerName Name of the player.
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     */
    public static Executable updatePlayerName(UUID playerUUID, String playerName) {
        String sql = "UPDATE " + UsersTable.TABLE_NAME + " SET " + UsersTable.USER_NAME + "=?" +
                WHERE + UsersTable.USER_UUID + "=?";
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerName);
                statement.setString(2, playerUUID.toString());
            }
        };
    }

    /**
     * Store UserInfo about a player on a server in the database.
     *
     * @param playerUUID UUID of the player.
     * @param registered Time the player registered on the server.
     * @param serverUUID UUID of the Plan server.
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     */
    public static Executable registerUserInfo(UUID playerUUID, long registered, ServerUUID serverUUID, String joinAddress) {
        return new ExecStatement(UserInfoTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
                statement.setLong(2, registered);
                statement.setString(3, serverUUID.toString());
                statement.setBoolean(4, false); // Banned
                statement.setString(5, StringUtils.truncate(joinAddress, JoinAddressTable.JOIN_ADDRESS_MAX_LENGTH));
                statement.setBoolean(6, false); // Operator
            }
        };
    }

    public static Executable updateMainRegisterDate(UUID playerUUID, long registered) {
        String sql = "UPDATE " + UsersTable.TABLE_NAME +
                " SET " + UsersTable.REGISTERED + "=?" +
                WHERE + UsersTable.USER_UUID + "=?";
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, registered);
                statement.setString(2, playerUUID.toString());
            }
        };
    }

    /**
     * Store Ping data of a player on a server.
     *
     * @param playerUUID UUID of the player.
     * @param serverUUID UUID of the Plan server.
     * @param ping       Ping data entry
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     */
    public static Executable storePing(UUID playerUUID, ServerUUID serverUUID, Ping ping) {
        return new ExecStatement(PingTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
                statement.setString(2, serverUUID.toString());
                statement.setLong(3, ping.getDate());
                statement.setInt(4, ping.getMin());
                statement.setInt(5, ping.getMax());
                statement.setDouble(6, ping.getAverage());
            }
        };
    }

    /**
     * Store TPS data of a server.
     *
     * @param serverUUID UUID of the Plan server.
     * @param tps        TPS data entry
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     */
    public static Executable storeTPS(ServerUUID serverUUID, TPS tps) {
        return new ExecStatement(TPSTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, tps.getDate());
                statement.setDouble(3, tps.getTicksPerSecond());
                statement.setInt(4, tps.getPlayers());
                statement.setDouble(5, tps.getCPUUsage());
                statement.setLong(6, tps.getUsedMemory());
                statement.setDouble(7, tps.getEntityCount());
                statement.setDouble(8, tps.getChunksLoaded());
                statement.setLong(9, tps.getFreeDiskSpace());
                Sql.setDoubleOrNull(statement, 10, tps.getMsptAverage());
                Sql.setDoubleOrNull(statement, 11, tps.getMspt95thPercentile());
            }
        };
    }

    /**
     * Store nickname information of a player on a server.
     *
     * @param playerUUID UUID of the player.
     * @param nickname   Nickname information.
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     */
    public static Executable storePlayerNickname(UUID playerUUID, Nickname nickname) {
        return connection -> {
            if (!updatePlayerNickname(playerUUID, nickname).execute(connection)) {
                insertPlayerNickname(playerUUID, nickname).execute(connection);
            }
            return false;
        };
    }

    private static Executable updatePlayerNickname(UUID playerUUID, Nickname nickname) {
        return new ExecStatement(NicknamesTable.UPDATE_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, nickname.getDate());
                statement.setString(2, nickname.getName());
                statement.setString(3, playerUUID.toString());
                statement.setString(4, nickname.getServerUUID().toString());
            }
        };
    }

    private static Executable insertPlayerNickname(UUID playerUUID, Nickname nickname) {
        return new ExecStatement(NicknamesTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
                statement.setString(2, nickname.getServerUUID().toString());
                statement.setString(3, nickname.getName());
                statement.setLong(4, nickname.getDate());
            }
        };
    }

    public static Executable updateJoinAddress(UUID playerUUID, ServerUUID serverUUID, String joinAddress) {
        String sql = "UPDATE " + UserInfoTable.TABLE_NAME + " SET " +
                UserInfoTable.JOIN_ADDRESS + "=?" +
                WHERE + UserInfoTable.USER_ID + "=" + UsersTable.SELECT_USER_ID +
                AND + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID;
        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, joinAddress);
                statement.setString(2, playerUUID.toString());
                statement.setString(3, serverUUID.toString());
            }
        };
    }
}