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
package com.djrapitops.plan.storage.database.queries.objects;

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.tables.NicknamesTable;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.utilities.dev.Untrusted;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for fetching different user identifiers in the database.
 *
 * @author AuroraLS3
 */
public class UserIdentifierQueries {

    private UserIdentifierQueries() {
        /* Static method class */
    }

    /**
     * Query database for all player UUIDs stored in the Plan database.
     *
     * @return Set of UUIDs.
     */
    public static Query<Set<UUID>> fetchAllPlayerUUIDs() {
        String sql = Select.from(UsersTable.TABLE_NAME, UsersTable.USER_UUID).toString();

        return new QueryAllStatement<>(sql, 20000) {
            @Override
            public Set<UUID> processResults(ResultSet set) throws SQLException {
                Set<UUID> playerUUIDs = new HashSet<>();
                while (set.next()) {
                    UUID playerUUID = UUID.fromString(set.getString(UsersTable.USER_UUID));
                    playerUUIDs.add(playerUUID);
                }
                return playerUUIDs;
            }
        };
    }

    /**
     * Query database for all player UUIDs that have joined a server.
     *
     * @param serverUUID UUID of the Plan server.
     * @return Set of UUIDs.
     */
    public static Query<Set<UUID>> fetchPlayerUUIDsOfServer(ServerUUID serverUUID) {
        String sql = SELECT +
                UsersTable.TABLE_NAME + '.' + UsersTable.USER_UUID + ',' +
                FROM + UsersTable.TABLE_NAME +
                INNER_JOIN + UserInfoTable.TABLE_NAME + " on " +
                UsersTable.TABLE_NAME + '.' + UsersTable.ID + "=" + UserInfoTable.TABLE_NAME + '.' + UserInfoTable.USER_ID +
                WHERE + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID;
        return new QueryStatement<>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Set<UUID> processResults(ResultSet set) throws SQLException {
                Set<UUID> playerUUIDs = new HashSet<>();
                while (set.next()) {
                    UUID playerUUID = UUID.fromString(set.getString(UsersTable.USER_UUID));
                    playerUUIDs.add(playerUUID);
                }
                return playerUUIDs;
            }
        };
    }

    /**
     * Query database for a Map for all UUIDs and Player names.
     *
     * @return Map: Player UUID - Player name
     */
    public static Query<Map<UUID, String>> fetchAllPlayerNames() {
        String sql = Select.from(UsersTable.TABLE_NAME, UsersTable.USER_UUID, UsersTable.USER_NAME).toString();

        return new QueryAllStatement<>(sql, 20000) {
            @Override
            public Map<UUID, String> processResults(ResultSet set) throws SQLException {
                Map<UUID, String> names = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString(UsersTable.USER_UUID));
                    String name = set.getString(UsersTable.USER_NAME);

                    names.put(uuid, name);
                }
                return names;
            }
        };
    }

    /**
     * Query database for a Player UUID matching a specific player's name.
     *
     * @param playerName Name of the player, case does not matter.
     * @return Optional: UUID if found, empty if not.
     */
    public static Query<Optional<UUID>> fetchPlayerUUIDOf(@Untrusted String playerName) {
        String sql = Select.from(UsersTable.TABLE_NAME, UsersTable.USER_UUID)
                .where("UPPER(" + UsersTable.USER_NAME + ")=UPPER(?)")
                .toString();

        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerName);
            }

            @Override
            public Optional<UUID> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    String uuidS = set.getString(UsersTable.USER_UUID);
                    return Optional.of(UUID.fromString(uuidS));
                }
                return Optional.empty();
            }
        };
    }

    /**
     * Query database for a Player name matching a specific player's UUID.
     *
     * @param playerUUID UUID of the Player
     * @return Optional: name if found, empty if not.
     */
    public static Query<Optional<String>> fetchPlayerNameOf(UUID playerUUID) {
        String sql = Select.from(UsersTable.TABLE_NAME, UsersTable.USER_NAME).where(UsersTable.USER_UUID + "=?").toString();

        return new QueryStatement<>(sql) {
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

    public static Query<List<String>> fetchMatchingPlayerNames(@Untrusted String searchFor) {
        String sql = SELECT + DISTINCT + UsersTable.USER_NAME +
                FROM + UsersTable.TABLE_NAME +
                WHERE + "LOWER(" + UsersTable.USER_NAME + ") LIKE LOWER(?)" +
                " UNION " +
                SELECT + DISTINCT + UsersTable.USER_NAME +
                FROM + UsersTable.TABLE_NAME +
                INNER_JOIN + NicknamesTable.TABLE_NAME + " on " +
                UsersTable.TABLE_NAME + '.' + UsersTable.USER_UUID + "=" + NicknamesTable.TABLE_NAME + '.' + NicknamesTable.USER_UUID +
                WHERE + "LOWER(" + NicknamesTable.NICKNAME + ") LIKE LOWER(?)";

        return new QueryStatement<>(sql, 5000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, '%' + searchFor + '%');
                statement.setString(2, '%' + searchFor + '%');
            }

            @Override
            public List<String> processResults(ResultSet set) throws SQLException {
                List<String> matchingNames = new ArrayList<>();
                while (set.next()) {
                    String match = set.getString(UsersTable.USER_NAME);
                    if (!matchingNames.contains(match)) {
                        matchingNames.add(match);
                    }
                }
                return matchingNames;
            }
        };
    }

    public static Query<Set<Integer>> fetchAllUserIds() {
        String sql = Select.from(UsersTable.TABLE_NAME, UsersTable.ID).toString();

        return new QueryAllStatement<>(sql, 2000) {
            @Override
            public Set<Integer> processResults(ResultSet set) throws SQLException {
                Set<Integer> playerUUIDs = new HashSet<>();
                while (set.next()) {
                    playerUUIDs.add(set.getInt(UsersTable.ID));
                }
                return playerUUIDs;
            }
        };
    }

    public static Query<Optional<Integer>> fetchUserId(UUID playerUUID) {
        String sql = Select.from(UsersTable.TABLE_NAME, UsersTable.ID).where(UsersTable.USER_UUID + "=?").toString();

        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Optional<Integer> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getInt(UsersTable.ID));
                }
                return Optional.empty();
            }
        };
    }

    public static Query<Integer> fetchMaxUserId(ServerUUID serverUUID) {
        String sql = SELECT + "MAX(" + UserInfoTable.USER_ID + ")" + FROM + UserInfoTable.TABLE_NAME + WHERE + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID;
        return db -> db.queryOptional(sql, row -> row.getInt(1), serverUUID).orElse(0);
    }

    public static Query<List<UUID>> fetchUUIDsStartingFromId(Integer userId, ServerUUID serverUUID, int limit) {
        String sql = SELECT + UsersTable.USER_UUID +
                FROM + UserInfoTable.TABLE_NAME + " ui" +
                LEFT_JOIN + UsersTable.TABLE_NAME + " u ON u." + UsersTable.ID + "=ui." + UserInfoTable.USER_ID +
                WHERE + UserInfoTable.USER_ID + ">=" + userId +
                AND + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                LIMIT + limit;
        return db -> db.queryList(sql, row -> UUID.fromString(row.getString(UsersTable.USER_UUID)), serverUUID);
    }
}