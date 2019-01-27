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
package com.djrapitops.plan.db.sql.queries;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.db.access.ExecBatchStatement;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.sql.tables.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Static method class for single item store queries.
 *
 * @author Rsl1122
 */
public class DataStoreQueries {

    private DataStoreQueries() {
        /* static method class */
    }

    /**
     * Store the used command in the database.
     *
     * @param serverUUID  UUID of the Plan server.
     * @param commandName Name of the command that was used.
     * @return Executable, use inside a {@link com.djrapitops.plan.db.access.transactions.Transaction}
     */
    public static Executable storeUsedCommandInformation(UUID serverUUID, String commandName) {
        return connection -> {
            if (!updateCommandUsage(serverUUID, commandName).execute(connection)) {
                insertNewCommandUsage(serverUUID, commandName).execute(connection);
            }
            return false;
        };
    }

    private static Executable updateCommandUsage(UUID serverUUID, String commandName) {
        return new ExecStatement(CommandUseTable.UPDATE_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setString(2, commandName);
            }
        };
    }

    private static Executable insertNewCommandUsage(UUID serverUUID, String commandName) {
        return new ExecStatement(CommandUseTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, commandName);
                statement.setInt(2, 1);
                statement.setString(3, serverUUID.toString());
            }
        };
    }

    /**
     * Store a finished session in the database.
     *
     * @param session Session, of which {@link Session#endSession(long)} has been called.
     * @return Executable, use inside a {@link com.djrapitops.plan.db.access.transactions.Transaction}
     * @throws IllegalArgumentException If {@link Session#endSession(long)} has not yet been called.
     */
    public static Executable storeSession(Session session) {
        session.getValue(SessionKeys.END).orElseThrow(() -> new IllegalArgumentException("Attempted to save a session that has not ended."));
        return connection -> {
            storeSessionInformation(session).execute(connection);
            storeSessionKills(session).execute(connection);
            storeSessionWorldTimesWorlds(session).execute(connection);
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
                statement.setInt(4, session.getUnsafe(SessionKeys.DEATH_COUNT));
                statement.setInt(5, session.getUnsafe(SessionKeys.MOB_KILL_COUNT));
                statement.setLong(6, session.getUnsafe(SessionKeys.AFK_TIME));
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

    // TODO Remove usage after WorldChange event stores world names in its own transaction
    private static Executable storeSessionWorldTimesWorlds(Session session) {
        return connection -> {
            UUID serverUUID = session.getUnsafe(SessionKeys.SERVER_UUID);

            Collection<String> worlds = session.getValue(SessionKeys.WORLD_TIMES)
                    .map(WorldTimes::getWorldTimes).map(Map::keySet)
                    .orElse(Collections.emptySet());

            for (String world : worlds) {
                storeWorldName(serverUUID, world).execute(connection);
            }
            return false;
        };
    }

    // TODO Remove usage after WorldChange event stores world names in its own transaction
    private static Executable storeWorldName(UUID serverUUID, String worldName) {
        return connection -> {
            if (doesWorldNameExist(connection, serverUUID, worldName)) {
                return insertWorldName(serverUUID, worldName).execute(connection);
            }
            return false;
        };
    }

    // TODO Remove usage after WorldChange event stores world names in its own transaction
    private static boolean doesWorldNameExist(Connection connection, UUID serverUUID, String worldName) {
        String selectSQL = "SELECT COUNT(1) as c FROM " + WorldTable.TABLE_NAME +
                " WHERE " + WorldTable.NAME + "=?" +
                " AND " + WorldTable.SERVER_UUID + "=?";
        try (PreparedStatement statement = connection.prepareStatement(selectSQL)) {
            statement.setString(1, worldName);
            statement.setString(2, serverUUID.toString());
            try (ResultSet set = statement.executeQuery()) {
                return set.next() && set.getInt("c") > 0;
            }
        } catch (SQLException ignored) {
            // Assume it has been saved.
            return true;
        }
    }

    private static Executable insertWorldName(UUID serverUUID, String worldName) {
        return new ExecStatement(WorldTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, worldName);
                statement.setString(2, serverUUID.toString());
            }
        };
    }

    private static Executable storeSessionWorldTimes(Session session) {
        return new ExecBatchStatement(WorldTimesTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                WorldTimesTable.addSessionWorldTimesToBatch(statement, session, GMTimes.getGMKeyArray());
            }
        };
    }
}