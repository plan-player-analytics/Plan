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
package com.djrapitops.plan.db.sql.tables;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.container.PlayerDeath;
import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.patches.KillsOptimizationPatch;
import com.djrapitops.plan.db.patches.KillsServerIDPatch;
import com.djrapitops.plan.db.patches.Version10Patch;
import com.djrapitops.plan.db.sql.parsing.Column;
import com.djrapitops.plan.db.sql.parsing.Sql;
import com.djrapitops.plan.db.sql.parsing.TableSqlParser;
import com.djrapitops.plan.db.sql.queries.batch.LargeFetchQueries;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Table that is in charge of storing kill data for each session.
 * <p>
 * Table Name: plan_kills
 * <p>
 * Patches related to this table:
 * {@link Version10Patch}
 * {@link KillsServerIDPatch}
 * {@link KillsOptimizationPatch}
 *
 * @author Rsl1122
 */
public class KillsTable extends UserUUIDTable {

    public static final String TABLE_NAME = "plan_kills";

    private final UsersTable usersTable;

    public KillsTable(SQLDB db) {
        super(TABLE_NAME, db);
        usersTable = db.getUsersTable();
        sessionsTable = db.getSessionsTable();
        insertStatement = "INSERT INTO " + tableName + " ("
                + Col.KILLER_UUID + ", "
                + Col.VICTIM_UUID + ", "
                + Col.SERVER_UUID + ", "
                + Col.SESSION_ID + ", "
                + Col.DATE + ", "
                + Col.WEAPON
                + ") VALUES (?, ?, ?, ?, ?, ?)";
    }

    private final SessionsTable sessionsTable;
    private String insertStatement;

    @Override
    public void createTable() throws DBInitException {
        createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(supportsMySQLQueries, Col.ID)
                .column(Col.KILLER_UUID, Sql.varchar(36)).notNull()
                .column(Col.VICTIM_UUID, Sql.varchar(36)).notNull()
                .column(Col.SERVER_UUID, Sql.varchar(36)).notNull()
                .column(Col.WEAPON, Sql.varchar(30)).notNull()
                .column(Col.DATE, Sql.LONG).notNull()
                .column(Col.SESSION_ID, Sql.INT).notNull()
                .primaryKey(supportsMySQLQueries, Col.ID)
                .foreignKey(Col.SESSION_ID, sessionsTable.getTableName(), SessionsTable.Col.ID)
                .toString()
        );
    }

    @Override
    public void removeUser(UUID uuid) {
        String sql = "DELETE FROM " + tableName +
                " WHERE " + Col.KILLER_UUID + "=?" +
                " OR " + Col.VICTIM_UUID + "=?";

        execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setString(2, uuid.toString());
            }
        });
    }

    public void addKillsToSessions(UUID uuid, Map<Integer, Session> sessions) {
        String usersUUIDColumn = usersTable + "." + UsersTable.Col.UUID;
        String usersNameColumn = usersTable + "." + UsersTable.Col.USER_NAME + " as victim_name";
        String sql = "SELECT " +
                Col.SESSION_ID + ", " +
                Col.DATE + ", " +
                Col.WEAPON + ", " +
                Col.VICTIM_UUID + ", " +
                usersNameColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersUUIDColumn + "=" + Col.VICTIM_UUID +
                " WHERE " + Col.KILLER_UUID + "=?";

        query(new QueryStatement<Object>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Object processResults(ResultSet set) throws SQLException {
                while (set.next()) {
                    int sessionID = set.getInt(Col.SESSION_ID.get());
                    Session session = sessions.get(sessionID);
                    if (session == null) {
                        continue;
                    }
                    UUID victim = UUID.fromString(set.getString(Col.VICTIM_UUID.get()));
                    String victimName = set.getString("victim_name");
                    long date = set.getLong(Col.DATE.get());
                    String weapon = set.getString(Col.WEAPON.get());
                    session.getPlayerKills().add(new PlayerKill(victim, weapon, date, victimName));
                }
                return null;
            }
        });
    }

    public void addDeathsToSessions(UUID uuid, Map<Integer, Session> sessions) {
        String usersUUIDColumn = usersTable + "." + UsersTable.Col.UUID;
        String usersNameColumn = usersTable + "." + UsersTable.Col.USER_NAME + " as killer_name";
        String sql = "SELECT " +
                Col.SESSION_ID + ", " +
                Col.DATE + ", " +
                Col.WEAPON + ", " +
                Col.KILLER_UUID + ", " +
                usersNameColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersUUIDColumn + "=" + Col.KILLER_UUID +
                " WHERE " + Col.VICTIM_UUID + "=?";

        query(new QueryStatement<Object>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Object processResults(ResultSet set) throws SQLException {
                while (set.next()) {
                    int sessionID = set.getInt(Col.SESSION_ID.get());
                    Session session = sessions.get(sessionID);
                    if (session == null) {
                        continue;
                    }
                    UUID killer = UUID.fromString(set.getString(Col.KILLER_UUID.get()));
                    String name = set.getString("killer_name");
                    long date = set.getLong(Col.DATE.get());
                    String weapon = set.getString(Col.WEAPON.get());
                    session.getUnsafe(SessionKeys.PLAYER_DEATHS).add(new PlayerDeath(killer, name, weapon, date));
                }
                return null;
            }
        });
    }

    public void savePlayerKills(UUID uuid, int sessionID, List<PlayerKill> playerKills) {
        if (Verify.isEmpty(playerKills)) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (PlayerKill kill : playerKills) {
                    UUID victim = kill.getVictim();
                    long date = kill.getDate();
                    String weapon = kill.getWeapon();
                    if (Verify.containsNull(victim, uuid)) {
                        continue;
                    }

                    statement.setString(1, uuid.toString());
                    statement.setString(2, victim.toString());
                    statement.setString(3, getServerUUID().toString());
                    statement.setInt(4, sessionID);
                    statement.setLong(5, date);
                    statement.setString(6, weapon);
                    statement.addBatch();
                }
            }
        });
    }

    public void addKillsToSessions(Map<UUID, Map<UUID, List<Session>>> map) {
        Map<Integer, List<PlayerKill>> playerKillsBySessionID = db.query(LargeFetchQueries.fetchAllPlayerKillsBySessionID());
        for (UUID serverUUID : map.keySet()) {
            for (List<Session> sessions : map.get(serverUUID).values()) {
                for (Session session : sessions) {
                    List<PlayerKill> playerKills = playerKillsBySessionID.get(session.getUnsafe(SessionKeys.DB_ID));
                    if (playerKills != null) {
                        session.setPlayerKills(playerKills);
                    }
                }
            }
        }
    }

    public void savePlayerKills(Map<UUID, Map<UUID, List<Session>>> allSessions) {
        if (Verify.isEmpty(allSessions)) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                // Every server
                for (UUID serverUUID : allSessions.keySet()) {
                    // Every player
                    for (Map.Entry<UUID, List<Session>> entry : allSessions.get(serverUUID).entrySet()) {
                        UUID killer = entry.getKey();
                        List<Session> sessions = entry.getValue();
                        // Every session
                        for (Session session : sessions) {
                            int sessionID = session.getUnsafe(SessionKeys.DB_ID);
                            // Every kill
                            for (PlayerKill kill : session.getPlayerKills()) {
                                UUID victim = kill.getVictim();
                                long date = kill.getDate();
                                String weapon = kill.getWeapon();
                                statement.setString(1, killer.toString());
                                statement.setString(2, victim.toString());
                                statement.setString(3, serverUUID.toString());
                                statement.setInt(4, sessionID);
                                statement.setLong(5, date);
                                statement.setString(6, weapon);
                                statement.addBatch();
                            }
                        }
                    }
                }
            }
        });
    }

    public enum Col implements Column {
        ID("id"),
        KILLER_UUID("killer_uuid"),
        VICTIM_UUID("victim_uuid"),
        SERVER_UUID("server_uuid"),
        SESSION_ID("session_id"),
        WEAPON("weapon"),
        DATE("date");

        private final String column;

        Col(String column) {
            this.column = column;
        }

        @Override
        public String get() {
            return toString();
        }

        @Override
        public String toString() {
            return column;
        }
    }
}
