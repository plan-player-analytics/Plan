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

import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.store.objects.DateObj;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.tables.SecurityTable;
import com.djrapitops.plan.db.sql.tables.ServerTable;
import com.djrapitops.plan.db.sql.tables.TPSTable;
import com.djrapitops.plan.system.info.server.Server;
import org.apache.commons.lang3.math.NumberUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * Static method class for queries that return single item if found.
 *
 * @author Rsl1122
 */
public class OptionalFetchQueries {

    private OptionalFetchQueries() {
        /* Static method class */
    }

    public static Query<Optional<Server>> fetchMatchingServerIdentifier(UUID serverUUID) {
        return fetchMatchingServerIdentifier(serverUUID.toString());
    }

    public static Query<Optional<Server>> fetchMatchingServerIdentifier(String identifier) {
        String sql = "SELECT * FROM " + ServerTable.TABLE_NAME +
                " WHERE (LOWER(" + ServerTable.SERVER_UUID + ") LIKE LOWER(?)" +
                " OR LOWER(" + ServerTable.NAME + ") LIKE LOWER(?)" +
                " OR " + ServerTable.SERVER_ID + "=?)" +
                " AND " + ServerTable.INSTALLED + "=?" +
                " LIMIT 1";
        return new QueryStatement<Optional<Server>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, identifier);
                statement.setString(2, identifier);
                statement.setInt(3, NumberUtils.isParsable(identifier) ? Integer.parseInt(identifier) : -1);
                statement.setBoolean(4, true);
            }

            @Override
            public Optional<Server> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(new Server(
                            set.getInt(ServerTable.SERVER_ID),
                            UUID.fromString(set.getString(ServerTable.SERVER_UUID)),
                            set.getString(ServerTable.NAME),
                            set.getString(ServerTable.WEB_ADDRESS),
                            set.getInt(ServerTable.MAX_PLAYERS)
                    ));
                }
                return Optional.empty();
            }
        };
    }

    public static Query<Optional<Server>> fetchProxyServerInformation() {
        return db -> db.query(fetchMatchingServerIdentifier("BungeeCord"));
    }

    public static Query<Optional<WebUser>> fetchWebUser(String called) {
        String sql = "SELECT * FROM " + SecurityTable.TABLE_NAME +
                " WHERE " + SecurityTable.USERNAME + "=? LIMIT 1";
        return new QueryStatement<Optional<WebUser>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, called);
            }

            @Override
            public Optional<WebUser> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    String saltedPassHash = set.getString(SecurityTable.SALT_PASSWORD_HASH);
                    int permissionLevel = set.getInt(SecurityTable.PERMISSION_LEVEL);
                    return Optional.of(new WebUser(called, saltedPassHash, permissionLevel));
                }
                return Optional.empty();
            }
        };
    }

    public static Query<Optional<DateObj<Integer>>> fetchPeakPlayerCount(UUID serverUUID, long afterDate) {
        String sql = "SELECT " + TPSTable.DATE + ", MAX(" + TPSTable.PLAYERS_ONLINE + ") as max FROM " + TPSTable.TABLE_NAME +
                " WHERE " + TPSTable.SERVER_ID + "=" + ServerTable.STATEMENT_SELECT_SERVER_ID +
                " AND " + TPSTable.DATE + ">= ?";

        return new QueryStatement<Optional<DateObj<Integer>>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, afterDate);
            }

            @Override
            public Optional<DateObj<Integer>> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(new DateObj<>(
                            set.getLong(TPSTable.DATE),
                            set.getInt(TPSTable.PLAYERS_ONLINE)
                    ));
                }
                return Optional.empty();
            }
        };
    }

    public static Query<Optional<DateObj<Integer>>> fetchAllTimePeakPlayerCount(UUID serverUUID) {
        return db -> db.query(fetchPeakPlayerCount(serverUUID, 0));
    }
}