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
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.java.Maps;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for {@link Server} objects.
 *
 * @author AuroraLS3
 */
public class ServerQueries {

    private ServerQueries() {
        /* Static method class */
    }

    public static Query<Collection<Server>> fetchUninstalledServerInformation() {
        String sql = SELECT + '*' + FROM + ServerTable.TABLE_NAME + WHERE + ServerTable.INSTALLED + "=?";
        return new QueryStatement<>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, false);
            }

            @Override
            public Collection<Server> processResults(ResultSet set) throws SQLException {
                Collection<Server> servers = new HashSet<>();
                while (set.next()) {
                    servers.add(extractServer(set));
                }
                return servers;
            }
        };
    }

    /**
     * Query database for all Plan server information.
     *
     * @return Map: Server UUID - Plan Server Information
     */
    public static Query<Map<ServerUUID, Server>> fetchPlanServerInformation() {
        String sql = SELECT + '*' + FROM + ServerTable.TABLE_NAME + WHERE + ServerTable.INSTALLED + "=?";

        return new QueryStatement<>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, true);
            }

            @Override
            public Map<ServerUUID, Server> processResults(ResultSet set) throws SQLException {
                Map<ServerUUID, Server> servers = new HashMap<>();
                while (set.next()) {
                    ServerUUID serverUUID = ServerUUID.fromString(set.getString(ServerTable.SERVER_UUID));
                    servers.put(serverUUID, new Server(
                            set.getInt(ServerTable.ID),
                            serverUUID,
                            set.getString(ServerTable.NAME),
                            set.getString(ServerTable.WEB_ADDRESS),
                            set.getBoolean(ServerTable.PROXY),
                            set.getString(ServerTable.PLAN_VERSION)));
                }
                return servers;
            }
        };
    }

    public static Query<Collection<Server>> fetchPlanServerInformationCollection() {
        return db -> db.query(fetchPlanServerInformation()).values();
    }

    public static Query<Optional<Server>> fetchServerMatchingIdentifier(ServerUUID serverUUID) {
        return fetchServerMatchingIdentifier(serverUUID.toString());
    }

    public static Query<Optional<Server>> fetchServerMatchingIdentifier(@Untrusted String identifier) {
        String sql = SELECT + '*' + FROM + ServerTable.TABLE_NAME +
                WHERE + "(LOWER(" + ServerTable.SERVER_UUID + ") LIKE LOWER(?)" +
                OR + "LOWER(" + ServerTable.NAME + ") LIKE LOWER(?)" +
                OR + ServerTable.ID + "=?" +
                OR + ServerTable.ID + "=?)" +
                AND + ServerTable.INSTALLED + "=?" +
                LIMIT + '1';
        return new QueryStatement<>(sql) {
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
                    return Optional.of(extractServer(set));
                }
                return Optional.empty();
            }
        };
    }

    public static Query<List<Server>> fetchProxyServers() {
        String sql = SELECT + '*' + FROM + ServerTable.TABLE_NAME +
                WHERE + ServerTable.INSTALLED + "=?" +
                AND + ServerTable.PROXY + "=?";
        return db -> db.queryList(sql, ServerQueries::extractServer, true, true);
    }

    public static Query<List<Server>> fetchAllServers() {
        String sql = SELECT + '*' + FROM + ServerTable.TABLE_NAME +
                WHERE + ServerTable.INSTALLED + "=?";
        return db -> db.queryList(sql, ServerQueries::extractServer, true, true);
    }

    private static @NotNull Server extractServer(ResultSet set) throws SQLException {
        return new Server(
                set.getInt(ServerTable.ID),
                ServerUUID.fromString(set.getString(ServerTable.SERVER_UUID)),
                set.getString(ServerTable.NAME),
                set.getString(ServerTable.WEB_ADDRESS),
                set.getBoolean(ServerTable.PROXY),
                set.getString(ServerTable.PLAN_VERSION)
        );
    }

    public static Query<List<ServerUUID>> fetchProxyServerUUIDs() {
        String sql = SELECT + ServerTable.SERVER_UUID + FROM + ServerTable.TABLE_NAME +
                WHERE + ServerTable.INSTALLED + "=?" +
                AND + ServerTable.PROXY + "=?";
        return db -> db.queryList(sql, set -> ServerUUID.fromString(set.getString(ServerTable.SERVER_UUID)),
                true, true
        );
    }

    public static Query<List<String>> fetchGameServerNames() {
        String sql = Select.from(ServerTable.TABLE_NAME,
                        ServerTable.ID, ServerTable.SERVER_UUID, ServerTable.NAME)
                .where(ServerTable.PROXY + "=0")
                .toString();

        return new QueryAllStatement<>(sql) {
            @Override
            public List<String> processResults(ResultSet set) throws SQLException {
                List<String> names = new ArrayList<>();
                while (set.next()) {
                    names.add(Server.getIdentifiableName(set.getString(ServerTable.NAME), set.getInt(ServerTable.ID), false));
                }
                return names;
            }
        };
    }

    public static Query<Map<ServerUUID, String>> fetchServerNames() {
        String sql = Select.from(ServerTable.TABLE_NAME,
                        ServerTable.ID, ServerTable.SERVER_UUID, ServerTable.NAME, ServerTable.PROXY)
                .toString();

        return new QueryAllStatement<>(sql) {
            @Override
            public Map<ServerUUID, String> processResults(ResultSet set) throws SQLException {
                Map<ServerUUID, String> names = new HashMap<>();
                while (set.next()) {
                    ServerUUID serverUUID = ServerUUID.fromString(set.getString(ServerTable.SERVER_UUID));
                    names.put(serverUUID, Server.getIdentifiableName(set.getString(ServerTable.NAME),
                            set.getInt(ServerTable.ID),
                            set.getBoolean(ServerTable.PROXY)));
                }
                return names;
            }
        };
    }

    public static Query<List<Server>> findMatchingServers(String identifier) {
        if (identifier.isEmpty()) return db -> Collections.emptyList();

        String sql = SELECT + '*' + FROM + ServerTable.TABLE_NAME +
                WHERE + "(LOWER(" + ServerTable.SERVER_UUID + ") LIKE LOWER(?)" +
                OR + "LOWER(" + ServerTable.NAME + ") LIKE LOWER(?)" +
                OR + ServerTable.ID + "=?" +
                OR + ServerTable.ID + "=?)" +
                AND + ServerTable.INSTALLED + "=?" +
                LIMIT + '1';
        return new QueryStatement<>(sql) {
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
                    matches.add(extractServer(set));
                }
                return matches;
            }
        };
    }

    public static Query<Integer> fetchServerCount() {
        String sql = SELECT + "COUNT(1) as c" + FROM + ServerTable.TABLE_NAME +
                WHERE + ServerTable.INSTALLED + "=?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, true);
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt("c") : 1;
            }
        };
    }

    public static Query<Integer> fetchBiggestServerID() {
        String sql = SELECT + "MAX(" + ServerTable.ID + ") as max_id" + FROM + ServerTable.TABLE_NAME +
                WHERE + ServerTable.INSTALLED + "=?";
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setBoolean(1, true);
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt("max_id") : 1;
            }
        };
    }

    public static Query<Map<String, ServerUUID>> fetchServerNamesToUUIDs() {
        return db -> Maps.reverse(db.query(fetchServerNames()));
    }

    public static Query<List<ServerUUID>> fetchServersMatchingIdentifiers(@Untrusted List<String> serverNames) {
        return db -> {
            Map<String, ServerUUID> nameToUUIDMap = db.query(ServerQueries.fetchServerNamesToUUIDs());
            return serverNames.stream()
                    .map(nameToUUIDMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        };
    }
}