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

import com.djrapitops.plan.data.container.PlayerDeath;
import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.patches.KillsOptimizationPatch;
import com.djrapitops.plan.db.patches.KillsServerIDPatch;
import com.djrapitops.plan.db.patches.Version10Patch;
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Sql;
import com.djrapitops.plan.db.sql.queries.LargeFetchQueries;
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
public class KillsTable extends Table {

    public static final String TABLE_NAME = "plan_kills";

    public static final String ID = "id";
    public static final String KILLER_UUID = "killer_uuid";
    public static final String VICTIM_UUID = "victim_uuid";
    public static final String SERVER_UUID = "server_uuid";
    public static final String SESSION_ID = "session_id";
    public static final String WEAPON = "weapon";
    public static final String DATE = "date";

    private final UsersTable usersTable;

    public KillsTable(SQLDB db) {
        super(TABLE_NAME, db);
        usersTable = db.getUsersTable();
        sessionsTable = db.getSessionsTable();
        insertStatement = "INSERT INTO " + tableName + " ("
                + KILLER_UUID + ", "
                + VICTIM_UUID + ", "
                + SERVER_UUID + ", "
                + SESSION_ID + ", "
                + DATE + ", "
                + WEAPON
                + ") VALUES (?, ?, ?, ?, ?, ?)";
    }

    private final SessionsTable sessionsTable;
    private String insertStatement;

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(ID, Sql.INT).primaryKey()
                .column(KILLER_UUID, Sql.varchar(36)).notNull()
                .column(VICTIM_UUID, Sql.varchar(36)).notNull()
                .column(SERVER_UUID, Sql.varchar(36)).notNull()
                .column(WEAPON, Sql.varchar(30)).notNull()
                .column(DATE, Sql.LONG).notNull()
                .column(SESSION_ID, Sql.INT).notNull()
                .foreignKey(SESSION_ID, SessionsTable.TABLE_NAME, SessionsTable.ID)
                .toString();
    }

    public void addKillsToSessions(UUID uuid, Map<Integer, Session> sessions) {
        String usersUUIDColumn = usersTable + "." + UsersTable.USER_UUID;
        String usersNameColumn = usersTable + "." + UsersTable.USER_NAME + " as victim_name";
        String sql = "SELECT " +
                SESSION_ID + ", " +
                DATE + ", " +
                WEAPON + ", " +
                VICTIM_UUID + ", " +
                usersNameColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersUUIDColumn + "=" + VICTIM_UUID +
                " WHERE " + KILLER_UUID + "=?";

        query(new QueryStatement<Object>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Object processResults(ResultSet set) throws SQLException {
                while (set.next()) {
                    int sessionID = set.getInt(SESSION_ID);
                    Session session = sessions.get(sessionID);
                    if (session == null) {
                        continue;
                    }
                    UUID victim = UUID.fromString(set.getString(VICTIM_UUID));
                    String victimName = set.getString("victim_name");
                    long date = set.getLong(DATE);
                    String weapon = set.getString(WEAPON);
                    session.getPlayerKills().add(new PlayerKill(victim, weapon, date, victimName));
                }
                return null;
            }
        });
    }

    public void addDeathsToSessions(UUID uuid, Map<Integer, Session> sessions) {
        String usersUUIDColumn = usersTable + "." + UsersTable.USER_UUID;
        String usersNameColumn = usersTable + "." + UsersTable.USER_NAME + " as killer_name";
        String sql = "SELECT " +
                SESSION_ID + ", " +
                DATE + ", " +
                WEAPON + ", " +
                KILLER_UUID + ", " +
                usersNameColumn +
                " FROM " + tableName +
                " INNER JOIN " + usersTable + " on " + usersUUIDColumn + "=" + KILLER_UUID +
                " WHERE " + VICTIM_UUID + "=?";

        query(new QueryStatement<Object>(sql, 50000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public Object processResults(ResultSet set) throws SQLException {
                while (set.next()) {
                    int sessionID = set.getInt(SESSION_ID);
                    Session session = sessions.get(sessionID);
                    if (session == null) {
                        continue;
                    }
                    UUID killer = UUID.fromString(set.getString(KILLER_UUID));
                    String name = set.getString("killer_name");
                    long date = set.getLong(DATE);
                    String weapon = set.getString(WEAPON);
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
        Map<Integer, List<PlayerKill>> playerKillsBySessionID = db.query(LargeFetchQueries.fetchAllPlayerKillDataBySessionID());
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
}
