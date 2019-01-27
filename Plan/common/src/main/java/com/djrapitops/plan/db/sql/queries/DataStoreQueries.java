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
import com.djrapitops.plan.db.access.ExecBatchStatement;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.sql.tables.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
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
        String sql = "UPDATE " + CommandUseTable.TABLE_NAME + " SET "
                + CommandUseTable.TIMES_USED + "=" + CommandUseTable.TIMES_USED + "+ 1" +
                " WHERE " + CommandUseTable.SERVER_ID + "=" + ServerTable.STATEMENT_SELECT_SERVER_ID +
                " AND " + CommandUseTable.COMMAND + "=?";

        return new ExecStatement(sql) {
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

    private static Executable storeSessionWorldTimes(Session session) {
        return new ExecBatchStatement(WorldTimesTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                WorldTimesTable.addSessionWorldTimesToBatch(statement, session, GMTimes.getGMKeyArray());
            }
        };
    }
}