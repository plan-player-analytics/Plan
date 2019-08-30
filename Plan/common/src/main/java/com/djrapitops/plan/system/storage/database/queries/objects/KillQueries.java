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
package com.djrapitops.plan.system.storage.database.queries.objects;

import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.system.storage.database.access.Query;
import com.djrapitops.plan.system.storage.database.access.QueryStatement;
import com.djrapitops.plan.system.storage.database.sql.tables.KillsTable;
import com.djrapitops.plan.system.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.system.storage.database.sql.tables.UsersTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.djrapitops.plan.system.storage.database.sql.parsing.Sql.*;

/**
 * Queries for {@link com.djrapitops.plan.data.container.PlayerKill} and {@link com.djrapitops.plan.data.container.PlayerDeath} objects.
 *
 * @author Rsl1122
 */
public class KillQueries {

    private KillQueries() {
        // Static method class
    }

    public static Query<List<PlayerKill>> fetchPlayerKillsOnServer(UUID serverUUID, int limit) {
        String sql = SELECT + KillsTable.VICTIM_UUID + ", " +
                "v." + UsersTable.USER_NAME + " as victim_name, " +
                "k." + UsersTable.USER_NAME + " as killer_name," +
                KillsTable.DATE + ", " +
                KillsTable.WEAPON +
                FROM + KillsTable.TABLE_NAME +
                INNER_JOIN + UsersTable.TABLE_NAME + " v on v." + UsersTable.USER_UUID + "=" + KillsTable.VICTIM_UUID +
                INNER_JOIN + UsersTable.TABLE_NAME + " k on k." + UsersTable.USER_UUID + "=" + KillsTable.KILLER_UUID +
                WHERE + KillsTable.TABLE_NAME + '.' + KillsTable.SERVER_UUID + "=?" +
                ORDER_BY + KillsTable.DATE + " DESC LIMIT ?";

        return new QueryStatement<List<PlayerKill>>(sql, limit) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setInt(2, limit);
            }

            @Override
            public List<PlayerKill> processResults(ResultSet set) throws SQLException {
                List<PlayerKill> kills = new ArrayList<>();
                while (set.next()) {
                    extractKillFromResults(set).ifPresent(kills::add);
                }
                return kills;
            }
        };
    }

    public static Query<List<PlayerKill>> fetchPlayerKillsOfPlayer(UUID playerUUID) {
        String sql = SELECT + KillsTable.VICTIM_UUID + ", " +
                "v." + UsersTable.USER_NAME + " as victim_name, " +
                "k." + UsersTable.USER_NAME + " as killer_name," +
                KillsTable.DATE + ", " +
                KillsTable.WEAPON +
                FROM + KillsTable.TABLE_NAME +
                INNER_JOIN + UsersTable.TABLE_NAME + " v on v." + UsersTable.USER_UUID + "=" + KillsTable.VICTIM_UUID +
                INNER_JOIN + UsersTable.TABLE_NAME + " k on k." + UsersTable.USER_UUID + "=" + KillsTable.KILLER_UUID +
                WHERE + KillsTable.TABLE_NAME + '.' + KillsTable.KILLER_UUID + "=?" +
                ORDER_BY + KillsTable.DATE + " DESC";

        return new QueryStatement<List<PlayerKill>>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public List<PlayerKill> processResults(ResultSet set) throws SQLException {
                List<PlayerKill> kills = new ArrayList<>();
                while (set.next()) {
                    extractKillFromResults(set).ifPresent(kills::add);
                }
                return kills;
            }
        };
    }

    public static Query<List<PlayerKill>> fetchPlayerDeathsOfPlayer(UUID playerUUID) {
        String sql = SELECT + KillsTable.VICTIM_UUID + ", " +
                "v." + UsersTable.USER_NAME + " as victim_name, " +
                "k." + UsersTable.USER_NAME + " as killer_name," +
                KillsTable.DATE + ", " +
                KillsTable.WEAPON +
                FROM + KillsTable.TABLE_NAME +
                INNER_JOIN + UsersTable.TABLE_NAME + " v on v." + UsersTable.USER_UUID + "=" + KillsTable.VICTIM_UUID +
                INNER_JOIN + UsersTable.TABLE_NAME + " k on k." + UsersTable.USER_UUID + "=" + KillsTable.KILLER_UUID +
                WHERE + KillsTable.TABLE_NAME + '.' + KillsTable.VICTIM_UUID + "=?" +
                ORDER_BY + KillsTable.DATE + " DESC";

        return new QueryStatement<List<PlayerKill>>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public List<PlayerKill> processResults(ResultSet set) throws SQLException {
                List<PlayerKill> kills = new ArrayList<>();
                while (set.next()) {
                    extractKillFromResults(set).ifPresent(kills::add);
                }
                return kills;
            }
        };
    }

    private static Optional<PlayerKill> extractKillFromResults(ResultSet set) throws SQLException {
        String victimName = set.getString("victim_name");
        String killerName = set.getString("killer_name");
        if (victimName != null && killerName != null) {
            UUID victim = UUID.fromString(set.getString(KillsTable.VICTIM_UUID));
            long date = set.getLong(KillsTable.DATE);
            String weapon = set.getString(KillsTable.WEAPON);
            return Optional.of(new PlayerKill(victim, weapon, date, victimName, killerName));
        }
        return Optional.empty();
    }

    public static Query<Long> playerKillCount(long after, long before, UUID serverUUID) {
        String sql = SELECT + "COUNT(1) as count" +
                FROM + KillsTable.TABLE_NAME +
                WHERE + KillsTable.SERVER_UUID + "=?" +
                AND + KillsTable.DATE + ">=?" +
                AND + KillsTable.DATE + "<=?";
        return new QueryStatement<Long>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, after);
                statement.setLong(3, before);
            }

            @Override
            public Long processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getLong("count") : 0L;
            }
        };
    }

    public static Query<Double> averageKDR(long after, long before, UUID serverUUID) {
        String selectKillCounts = SELECT + "COUNT(1) as kills," + KillsTable.KILLER_UUID +
                FROM + KillsTable.TABLE_NAME +
                WHERE + KillsTable.SERVER_UUID + "=?" +
                AND + KillsTable.DATE + ">=?" +
                AND + KillsTable.DATE + "<=?" +
                GROUP_BY + KillsTable.KILLER_UUID;
        String selectDeathCounts = SELECT + "COUNT(1) as deaths," + KillsTable.VICTIM_UUID +
                FROM + KillsTable.TABLE_NAME +
                WHERE + KillsTable.SERVER_UUID + "=?" +
                AND + KillsTable.DATE + ">=?" +
                AND + KillsTable.DATE + "<=?" +
                GROUP_BY + KillsTable.VICTIM_UUID;
        String sql = SELECT + "AVG(CAST(kills AS double)/CAST(deaths AS double)) as kdr" +
                FROM + '(' + selectKillCounts + ") q1" +
                INNER_JOIN + '(' + selectDeathCounts + ") q2 on q1." + KillsTable.KILLER_UUID + "=q2." + KillsTable.VICTIM_UUID +
                WHERE + "deaths!=0";

        return new QueryStatement<Double>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, after);
                statement.setLong(3, before);
                statement.setString(4, serverUUID.toString());
                statement.setLong(5, after);
                statement.setLong(6, before);
            }

            @Override
            public Double processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getDouble("kdr") : 0.0;
            }
        };
    }

    public static Query<Long> mobKillCount(long after, long before, UUID serverUUID) {
        String sql = SELECT + "SUM(" + SessionsTable.MOB_KILLS + ") as count" +
                FROM + SessionsTable.TABLE_NAME +
                WHERE + SessionsTable.SERVER_UUID + "=?" +
                AND + SessionsTable.SESSION_END + ">=?" +
                AND + SessionsTable.SESSION_START + "<=?";
        return new QueryStatement<Long>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, after);
                statement.setLong(3, before);
            }

            @Override
            public Long processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getLong("count") : 0L;
            }
        };
    }

    public static Query<Long> deathCount(long after, long before, UUID serverUUID) {
        String sql = SELECT + "SUM(" + SessionsTable.DEATHS + ") as count" +
                FROM + SessionsTable.TABLE_NAME +
                WHERE + SessionsTable.SERVER_UUID + "=?" +
                AND + SessionsTable.SESSION_END + ">=?" +
                AND + SessionsTable.SESSION_START + "<=?";
        return new QueryStatement<Long>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, after);
                statement.setLong(3, before);
            }

            @Override
            public Long processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getLong("count") : 0L;
            }
        };
    }

    public static Query<List<String>> topWeaponsOfServer(long after, long before, UUID serverUUID, int limit) {
        String innerSQL = SELECT + KillsTable.WEAPON + ", COUNT(1) as kills" +
                FROM + KillsTable.TABLE_NAME +
                WHERE + KillsTable.SERVER_UUID + "=?" +
                AND + KillsTable.DATE + ">=?" +
                AND + KillsTable.DATE + "<=?" +
                GROUP_BY + KillsTable.WEAPON;
        String sql = SELECT + KillsTable.WEAPON +
                FROM + '(' + innerSQL + ") q1" +
                ORDER_BY + "kills DESC LIMIT ?";

        return new QueryStatement<List<String>>(sql, limit) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, after);
                statement.setLong(3, before);
                statement.setInt(4, limit);
            }

            @Override
            public List<String> processResults(ResultSet set) throws SQLException {
                List<String> weapons = new ArrayList<>();
                while (set.next()) weapons.add(set.getString(KillsTable.WEAPON));
                return weapons;
            }
        };
    }

    public static Query<List<String>> topWeaponsOfPlayer(long after, long before, UUID playerUUID, int limit) {
        String innerSQL = SELECT + KillsTable.WEAPON + ", COUNT(1) as kills" +
                FROM + KillsTable.TABLE_NAME +
                WHERE + KillsTable.KILLER_UUID + "=?" +
                AND + KillsTable.DATE + ">=?" +
                AND + KillsTable.DATE + "<=?" +
                GROUP_BY + KillsTable.WEAPON;
        String sql = SELECT + KillsTable.WEAPON +
                FROM + '(' + innerSQL + ") q1" +
                ORDER_BY + "kills DESC LIMIT ?";

        return new QueryStatement<List<String>>(sql, limit) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
                statement.setLong(2, after);
                statement.setLong(3, before);
                statement.setInt(4, limit);
            }

            @Override
            public List<String> processResults(ResultSet set) throws SQLException {
                List<String> weapons = new ArrayList<>();
                while (set.next()) weapons.add(set.getString(KillsTable.WEAPON));
                return weapons;
            }
        };
    }
}