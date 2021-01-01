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

import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import org.apache.commons.lang3.math.NumberUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for {@link Server} objects.
 *
 * @author Rsl1122
 */
public class ServerQueries {

    private ServerQueries() {
        /* Static method class */
    }

    /**
     * Query database for all Plan server information.
     *
     * @return Map: Server UUID - Plan Server Information
     */
    public static Query<Map<UUID, Server>> fetchPlanServerInformation() {
        String sql = SELECT + '*' + FROM + ServerTable.TABLE_NAME + WHERE + ServerTable.INSTALLED + "=?";

        return new QueryStatement<Map<UUID, Server>>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, true);
            }

            @Override
            public Map<UUID, Server> processResults(ResultSet set) throws SQLException {
                Map<UUID, Server> servers = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(ServerTable.SERVER_UUID));
                    servers.put(serverUUID, new Server(
                            set.getInt(ServerTable.SERVER_ID),
                            serverUUID,
                            set.getString(ServerTable.NAME),
                            set.getString(ServerTable.WEB_ADDRESS),
                            set.getBoolean(ServerTable.PROXY)
                    ));
                }
                return servers;
            }
        };
    }

    public static Query<Collection<Server>> fetchPlanServerInformationCollection() {
        return db -> db.query(fetchPlanServerInformation()).values();
    }

    public static Query<Optional<Server>> fetchServerMatchingIdentifier(UUID serverUUID) {
        return fetchServerMatchingIdentifier(serverUUID.toString());
    }

    public static Query<Optional<Server>> fetchServerMatchingIdentifier(String identifier) {
        String sql = SELECT + '*' + FROM + ServerTable.TABLE_NAME +
                " WHERE (LOWER(" + ServerTable.SERVER_UUID + ") LIKE LOWER(?)" +
                OR + "LOWER(" + ServerTable.NAME + ") LIKE LOWER(?)" +
                OR + ServerTable.SERVER_ID + "=?" +
                OR + ServerTable.SERVER_ID + "=?)" +
                AND + ServerTable.INSTALLED + "=?" +
                " LIMIT 1";
        return new QueryStatement<Optional<Server>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, identifier);
                statement.setString(2, identifier);
                statement.setInt(3, NumberUtils.isParsable(identifier) ? Integer.parseInt(identifier) : -1);
                String id = identifier.startsWith("Server ") ? identifier.substring(7) : identifier;
                statement.setInt(4, NumberUtils.isParsable(id) ? Integer.parseInt(id) : -1);
                statement.setBoolean(5, true);
            }

            @Override
            public Optional<Server> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(new Server(
                            set.getInt(ServerTable.SERVER_ID),
                            UUID.fromString(set.getString(ServerTable.SERVER_UUID)),
                            set.getString(ServerTable.NAME),
                            set.getString(ServerTable.WEB_ADDRESS),
                            set.getBoolean(ServerTable.PROXY)
                    ));
                }
                return Optional.empty();
            }
        };
    }

    public static Query<Optional<Server>> fetchProxyServerInformation() {
        String sql = SELECT + '*' + FROM + ServerTable.TABLE_NAME +
                WHERE + ServerTable.INSTALLED + "=?" +
                AND + ServerTable.PROXY + "=?" +
                " LIMIT 1";
        return new QueryStatement<Optional<Server>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, true);
                statement.setBoolean(2, true);
            }

            @Override
            public Optional<Server> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(new Server(
                            set.getInt(ServerTable.SERVER_ID),
                            UUID.fromString(set.getString(ServerTable.SERVER_UUID)),
                            set.getString(ServerTable.NAME),
                            set.getString(ServerTable.WEB_ADDRESS),
                            set.getBoolean(ServerTable.PROXY)
                    ));
                }
                return Optional.empty();
            }
        };
    }

    public static Query<Map<UUID, String>> fetchServerNames() {
        String sql = Select.from(ServerTable.TABLE_NAME,
                ServerTable.SERVER_UUID, ServerTable.NAME)
                .toString();

        return new QueryAllStatement<Map<UUID, String>>(sql) {
            @Override
            public Map<UUID, String> processResults(ResultSet set) throws SQLException {
                Map<UUID, String> names = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(ServerTable.SERVER_UUID));
                    names.put(serverUUID, set.getString(ServerTable.NAME));
                }
                return names;
            }
        };
    }

    public static Query<List<Server>> findMatchingServers(String identifier) {
        if (identifier.isEmpty()) return db -> Collections.emptyList();

        String sql = SELECT + '*' + FROM + ServerTable.TABLE_NAME +
                " WHERE (LOWER(" + ServerTable.SERVER_UUID + ") LIKE LOWER(?)" +
                OR + "LOWER(" + ServerTable.NAME + ") LIKE LOWER(?)" +
                OR + ServerTable.SERVER_ID + "=?" +
                OR + ServerTable.SERVER_ID + "=?)" +
                AND + ServerTable.INSTALLED + "=?" +
                " LIMIT 1";
        return new QueryStatement<List<Server>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, '%' + identifier + '%');
                statement.setString(2, '%' + identifier + '%');
                statement.setInt(3, NumberUtils.isParsable(identifier) ? Integer.parseInt(identifier) : -1);
                String id = identifier.startsWith("Server ") ? identifier.substring(7) : identifier;
                statement.setInt(4, NumberUtils.isParsable(id) ? Integer.parseInt(id) : -1);
                statement.setBoolean(5, true);
            }

            @Override
            public List<Server> processResults(ResultSet set) throws SQLException {
                List<Server> matches = new ArrayList<>();
                while (set.next()) {
                    matches.add(new Server(
                            set.getInt(ServerTable.SERVER_ID),
                            UUID.fromString(set.getString(ServerTable.SERVER_UUID)),
                            set.getString(ServerTable.NAME),
                            set.getString(ServerTable.WEB_ADDRESS),
                            set.getBoolean(ServerTable.PROXY)
                    ));
                }
                return matches;
            }
        };
    }
}