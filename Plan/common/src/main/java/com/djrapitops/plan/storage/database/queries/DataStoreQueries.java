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
import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.storage.database.sql.tables.*;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plugin.utilities.Verify;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

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
     * @param session Session, of which {@link Session#endSession(long)} has been called.
     * @return Executable, use inside a {@link com.djrapitops.plan.storage.database.transactions.Transaction}
     * @throws IllegalArgumentException If {@link Session#endSession(long)} has not yet been called.
     */
    public static Executable storeSession(Session session) {
        Verify.isTrue(session.supports(SessionKeys.END), () -> new IllegalArgumentException("Attempted to save a session that has not ended."));
        return connection -> {
            storeSessionInformation(session).execute(connection);
            storeSessionKills(session).execute(connection);
            return storeSessionWorldTimes(session).execute(connection);
        };
    }

    private static Executable storeSessionInformation(Session session) {
        return new ExecStatement(SessionsTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, session.getUnsafe(SessionKeys.UUID).toString());
                statement.setLong(2, session.getUnsafe(SessionKeys.START));
                statement.setLong(3, session.getUnsafe(SessionKeys.END));
                statement.setInt(4, session.getValue(SessionKeys.DEATH_COUNT).orElse(0));
                statement.setInt(5, session.getValue(SessionKeys.MOB_KILL_COUNT).orElse(0));
                statement.setLong(6, session.getValue(SessionKeys.AFK_TIME).orElse(0L));
                statement.setString(7, session.getUnsafe(SessionKeys.SERVER_UUID).toString());
            }
        };
    }

    private static Executable storeSessionKills(Session session) {
        return new ExecBatchStatement(KillsTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                KillsTable.addSessionKillsToBatch(statement, session);
            }
        };
    }

    public static Executable insertWorldName(UUID serverUUID, String worldName) {
        return new ExecStatement(WorldTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, StringUtils.truncate(worldName, 100));
                statement.setString(2, serverUUID.toString());
            }
        };
    }

    private static Executable storeSessionWorldTimes(Session session) {
        if (session.getValue(SessionKeys.WORLD_TIMES).map(times -> times.getWorldTimes().isEmpty()).orElse(true)) {
            return Executable.empty();
        }
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
    public static Executable registerBaseUser(UUID playerUUID, long registered, String playerName) {
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
    public static Executable registerUserInfo(UUID playerUUID, long registered, UUID serverUUID) {
        return new ExecStatement(UserInfoTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
                statement.setLong(2, registered);
                statement.setString(3, serverUUID.toString());
                statement.setBoolean(4, false); // Banned
                statement.setBoolean(5, false); // Operator
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
    public static Executable storePing(UUID playerUUID, UUID serverUUID, Ping ping) {
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
    public static Executable storeTPS(UUID serverUUID, TPS tps) {
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
}