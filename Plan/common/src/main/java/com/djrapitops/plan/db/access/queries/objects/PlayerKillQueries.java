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
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.tables.KillsTable;
import com.djrapitops.plan.db.sql.tables.UsersTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.djrapitops.plan.db.sql.parsing.Sql.*;

/**
 * Queries for {@link com.djrapitops.plan.data.container.PlayerKill} and {@link com.djrapitops.plan.data.container.PlayerDeath} objects.
 *
 * @author Rsl1122
 */
public class PlayerKillQueries {

    private PlayerKillQueries() {
        // Static method class
    }

    public static Query<List<PlayerKill>> fetchMostRecentPlayerKills(UUID serverUUID, int limit) {
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
}