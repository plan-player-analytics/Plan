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

import com.djrapitops.plan.delivery.domain.ServerIdentifier;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.queries.RowExtractors;
import com.djrapitops.plan.storage.database.sql.tables.KillsTable;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for {@link PlayerKill} objects.
 *
 * @author AuroraLS3
 */
public class KillQueries {

    private KillQueries() {
        // Static method class
    }

    public static Query<List<PlayerKill>> fetchPlayerKillsOnServer(ServerUUID serverUUID, int limit) {
        String sql = SELECT +
                KillsTable.KILLER_UUID + ", " +
                KillsTable.VICTIM_UUID + ", " +
                KillsTable.SERVER_UUID + ", " +
                "v." + UsersTable.USER_NAME + " as victim_name, " +
                "v." + UsersTable.REGISTERED + " as victim_" + UsersTable.REGISTERED + ", " +
                "k." + UsersTable.USER_NAME + " as killer_name," +
                KillsTable.DATE + ", " +
                KillsTable.WEAPON + ", " +
                "server." + ServerTable.NAME + " as server_name," +
                "server." + ServerTable.ID + " as server_id" +
                FROM + KillsTable.TABLE_NAME + " ki" +
                INNER_JOIN + UsersTable.TABLE_NAME + " v on v." + UsersTable.USER_UUID + "=ki." + KillsTable.VICTIM_UUID +
                INNER_JOIN + UsersTable.TABLE_NAME + " k on k." + UsersTable.USER_UUID + "=ki." + KillsTable.KILLER_UUID +
                INNER_JOIN + ServerTable.TABLE_NAME + " server on server." + ServerTable.SERVER_UUID + "=ki." + KillsTable.SERVER_UUID +
                WHERE + "ki." + KillsTable.SERVER_UUID + "=?" +
                ORDER_BY + KillsTable.DATE + " DESC LIMIT ?";

        return new QueryStatement<>(sql, limit) {
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
        String sql = SELECT +
                KillsTable.KILLER_UUID + ", " +
                KillsTable.VICTIM_UUID + ", " +
                KillsTable.SERVER_UUID + ", " +
                "v." + UsersTable.USER_NAME + " as victim_name, " +
                "v." + UsersTable.REGISTERED + " as victim_" + UsersTable.REGISTERED + ", " +
                "k." + UsersTable.USER_NAME + " as killer_name," +
                KillsTable.DATE + ", " +
                KillsTable.WEAPON + ", " +
                "server." + ServerTable.NAME + " as server_name," +
                "server." + ServerTable.ID + " as server_id" +
                FROM + KillsTable.TABLE_NAME + " ki" +
                INNER_JOIN + UsersTable.TABLE_NAME + " v on v." + UsersTable.USER_UUID + "=ki." + KillsTable.VICTIM_UUID +
                INNER_JOIN + UsersTable.TABLE_NAME + " k on k." + UsersTable.USER_UUID + "=ki." + KillsTable.KILLER_UUID +
                INNER_JOIN + ServerTable.TABLE_NAME + " server on server." + ServerTable.SERVER_UUID + "=ki." + KillsTable.SERVER_UUID +
                WHERE + "ki." + KillsTable.KILLER_UUID + "=?" +
                ORDER_BY + KillsTable.DATE + " DESC";

        return new QueryStatement<>(sql, 100) {
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
        String sql = SELECT +
                KillsTable.KILLER_UUID + ", " +
                KillsTable.VICTIM_UUID + ", " +
                KillsTable.SERVER_UUID + ", " +
                "v." + UsersTable.USER_NAME + " as victim_name, " +
                "v." + UsersTable.REGISTERED + " as victim_" + UsersTable.REGISTERED + ", " +
                "k." + UsersTable.USER_NAME + " as killer_name," +
                KillsTable.DATE + ", " +
                KillsTable.WEAPON + ", " +
                "server." + ServerTable.NAME + " as server_name," +
                "server." + ServerTable.ID + " as server_id" +
                FROM + KillsTable.TABLE_NAME + " ki" +
                INNER_JOIN + UsersTable.TABLE_NAME + " v on v." + UsersTable.USER_UUID + "=ki." + KillsTable.VICTIM_UUID +
                INNER_JOIN + UsersTable.TABLE_NAME + " k on k." + UsersTable.USER_UUID + "=ki." + KillsTable.KILLER_UUID +
                INNER_JOIN + ServerTable.TABLE_NAME + " server on server." + ServerTable.SERVER_UUID + "=ki." + KillsTable.SERVER_UUID +
                WHERE + "ki." + KillsTable.VICTIM_UUID + "=?" +
                ORDER_BY + KillsTable.DATE + " DESC";

        return new QueryStatement<>(sql, 100) {
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
            UUID killer = UUID.fromString(set.getString(KillsTable.KILLER_UUID));
            UUID victim = UUID.fromString(set.getString(KillsTable.VICTIM_UUID));
            long date = set.getLong(KillsTable.DATE);
            String weapon = set.getString(KillsTable.WEAPON);
            return Optional.of(new PlayerKill(
                    new PlayerKill.Killer(killer, killerName),
                    new PlayerKill.Victim(victim, victimName, set.getLong("victim_" + UsersTable.REGISTERED)),
                    new ServerIdentifier(ServerUUID.fromString(set.getString(KillsTable.SERVER_UUID)),
                            Server.getIdentifiableName(set.getString("server_name"), set.getInt("server_id"), false)
                    ), weapon, date
            ));
        }
        return Optional.empty();
    }

    public static Query<Long> playerKillCount(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT + "COUNT(1) as count" +
                FROM + KillsTable.TABLE_NAME +
                WHERE + KillsTable.SERVER_UUID + "=?" +
                AND + KillsTable.DATE + ">=?" +
                AND + KillsTable.DATE + "<=?";

        return db -> db.queryOptional(sql, RowExtractors.getLong("count"), serverUUID, after, before)
                .orElse(0L);
    }

    public static Query<Double> averageKDR(long after, long before, ServerUUID serverUUID) {
        String selectKillCounts = SELECT + "COUNT(1) as kills," + KillsTable.KILLER_UUID +
                FROM + KillsTable.TABLE_NAME +
                WHERE + KillsTable.SERVER_UUID + "=?" +
                AND + KillsTable.DATE + ">=?" +
                AND + KillsTable.DATE + "<=?" +
                GROUP_BY + KillsTable.KILLER_UUID;
        String selectPlayerDeathCounts = SELECT + "COUNT(1) as deaths," + KillsTable.VICTIM_UUID +
                FROM + KillsTable.TABLE_NAME +
                WHERE + KillsTable.SERVER_UUID + "=?" +
                AND + KillsTable.DATE + ">=?" +
                AND + KillsTable.DATE + "<=?" +
                GROUP_BY + KillsTable.VICTIM_UUID;
        String sql = SELECT + "kills, deaths" +
                FROM + UsersTable.TABLE_NAME + " u" +
                LEFT_JOIN + '(' + selectKillCounts + ") q1 on q1." + KillsTable.KILLER_UUID + "=u." + UsersTable.USER_UUID +
                LEFT_JOIN + '(' + selectPlayerDeathCounts + ") q2 on q2." + KillsTable.VICTIM_UUID + "=u." + UsersTable.USER_UUID +
                WHERE + "kills" + IS_NOT_NULL +
                AND + "deaths" + IS_NOT_NULL;

        return new QueryStatement<>(sql) {
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
                double totalKDR = 0.0;
                int playerCount = 0;
                while (set.next()) {
                    int kills = set.getInt("kills");
                    int deaths = set.getInt("deaths");
                    totalKDR += (double) kills / (deaths > 0 ? deaths : 1);
                    playerCount++;
                }
                return totalKDR / (playerCount > 0 ? playerCount : 1);
            }
        };
    }

    public static Query<Long> mobKillCount(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT + "SUM(" + SessionsTable.MOB_KILLS + ") as count" +
                FROM + SessionsTable.TABLE_NAME +
                WHERE + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                AND + SessionsTable.SESSION_END + ">=?" +
                AND + SessionsTable.SESSION_START + "<=?";


        return db -> db.queryOptional(sql, RowExtractors.getLong("count"), serverUUID, after, before)
                .orElse(0L);
    }

    public static Query<Long> deathCount(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT + "SUM(" + SessionsTable.DEATHS + ") as count" +
                FROM + SessionsTable.TABLE_NAME +
                WHERE + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                AND + SessionsTable.SESSION_END + ">=?" +
                AND + SessionsTable.SESSION_START + "<=?";
        return db -> db.queryOptional(sql, RowExtractors.getLong("count"), serverUUID, after, before)
                .orElse(0L);
    }

    public static Query<List<String>> topWeaponsOfServer(long after, long before, ServerUUID serverUUID, int limit) {
        String innerSQL = SELECT + KillsTable.WEAPON + ", COUNT(1) as kills" +
                FROM + KillsTable.TABLE_NAME +
                WHERE + KillsTable.SERVER_UUID + "=?" +
                AND + KillsTable.DATE + ">=?" +
                AND + KillsTable.DATE + "<=?" +
                GROUP_BY + KillsTable.WEAPON;
        String sql = SELECT + KillsTable.WEAPON +
                FROM + '(' + innerSQL + ") q1" +
                ORDER_BY + "kills DESC LIMIT ?";

        return db -> db.queryList(sql, RowExtractors.getString(KillsTable.WEAPON), serverUUID, after, before, limit);
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

        return db -> db.queryList(sql, RowExtractors.getString(KillsTable.WEAPON), playerUUID, after, before, limit);
    }
}