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

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.NicknamesTable;
import com.djrapitops.plan.utilities.java.Lists;
import com.djrapitops.plan.utilities.java.Maps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for {@link Nickname} objects.
 *
 * @author AuroraLS3
 */
public class NicknameQueries {

    private NicknameQueries() {
        /* Static method class */
    }

    /**
     * Query database for all nickname data.
     *
     * @return Multimap: Server UUID - (Player UUID - List of nicknames)
     */
    public static Query<Map<ServerUUID, Map<UUID, List<Nickname>>>> fetchAllNicknameData() {
        String sql = SELECT +
                NicknamesTable.NICKNAME + ',' +
                NicknamesTable.LAST_USED + ',' +
                NicknamesTable.USER_UUID + ',' +
                NicknamesTable.SERVER_UUID +
                FROM + NicknamesTable.TABLE_NAME;

        return new QueryAllStatement<Map<ServerUUID, Map<UUID, List<Nickname>>>>(sql, 5000) {
            @Override
            public Map<ServerUUID, Map<UUID, List<Nickname>>> processResults(ResultSet set) throws SQLException {
                Map<ServerUUID, Map<UUID, List<Nickname>>> map = new HashMap<>();
                while (set.next()) {
                    ServerUUID serverUUID = ServerUUID.fromString(set.getString(NicknamesTable.SERVER_UUID));
                    UUID uuid = UUID.fromString(set.getString(NicknamesTable.USER_UUID));

                    Map<UUID, List<Nickname>> serverMap = map.computeIfAbsent(serverUUID, Maps::create);
                    List<Nickname> nicknames = serverMap.computeIfAbsent(uuid, Lists::create);

                    nicknames.add(new Nickname(
                            set.getString(NicknamesTable.NICKNAME),
                            set.getLong(NicknamesTable.LAST_USED),
                            serverUUID
                    ));
                }
                return map;
            }
        };
    }

    public static Query<Optional<Nickname>> fetchLastSeenNicknameOfPlayer(UUID playerUUID, ServerUUID serverUUID) {
        String subQuery = SELECT + "MAX(" + NicknamesTable.LAST_USED + ") FROM " + NicknamesTable.TABLE_NAME +
                WHERE + NicknamesTable.USER_UUID + "=?" +
                AND + NicknamesTable.SERVER_UUID + "=?" +
                GROUP_BY + NicknamesTable.USER_UUID;
        String sql = SELECT +
                NicknamesTable.LAST_USED + ',' + NicknamesTable.NICKNAME +
                FROM + NicknamesTable.TABLE_NAME +
                WHERE + NicknamesTable.USER_UUID + "=?" +
                AND + NicknamesTable.SERVER_UUID + "=?" +
                AND + NicknamesTable.LAST_USED + "=(" + subQuery + ')';
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

    public static Query<List<Nickname>> fetchNicknameDataOfPlayer(UUID playerUUID) {
        String sql = SELECT +
                NicknamesTable.NICKNAME + ',' +
                NicknamesTable.LAST_USED + ',' +
                NicknamesTable.SERVER_UUID +
                FROM + NicknamesTable.TABLE_NAME +
                WHERE + NicknamesTable.USER_UUID + "=?";

        return new QueryStatement<List<Nickname>>(sql, 5000) {

            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public List<Nickname> processResults(ResultSet set) throws SQLException {
                List<Nickname> nicknames = new ArrayList<>();
                while (set.next()) {
                    ServerUUID serverUUID = ServerUUID.fromString(set.getString(NicknamesTable.SERVER_UUID));
                    String nickname = set.getString(NicknamesTable.NICKNAME);
                    nicknames.add(new Nickname(nickname, set.getLong(NicknamesTable.LAST_USED), serverUUID));
                }
                return nicknames;
            }
        };
    }

}