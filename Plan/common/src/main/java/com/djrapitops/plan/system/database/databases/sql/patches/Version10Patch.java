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
package com.djrapitops.plan.system.database.databases.sql.patches;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.*;

import java.util.Optional;

public class Version10Patch extends Patch {

    private Integer serverID;

    public Version10Patch(SQLDB db) {
        super(db);
    }

    @Override
    public boolean hasBeenApplied() {
        return !hasTable("plan_gamemodetimes");
    }

    @Override
    protected void applyPatch() {
        try {
            Optional<Integer> fetchedServerID = db.getServerTable().getServerID(getServerUUID());
            if (!fetchedServerID.isPresent()) {
                throw new IllegalStateException("Server UUID was not registered, try rebooting the plugin.");
            }
            serverID = fetchedServerID.get();
            alterTablesToV10();
        } catch (DBInitException e) {
            throw new DBOpException(e.getMessage(), e);
        }
    }

    public void alterTablesToV10() throws DBInitException {
        copyCommandUsage();

        copyTPS();

        dropTable("plan_user_info");
        copyUsers();

        dropTable("plan_ips");
        db.getGeoInfoTable().createTable();
        dropTable("plan_world_times");
        dropTable("plan_worlds");
        db.getWorldTable().createTable();
        db.getWorldTimesTable().createTable();

        dropTable("plan_gamemodetimes");
        dropTable("temp_nicks");
        dropTable("temp_kills");
        dropTable("temp_users");
    }

    private void copyUsers() throws DBInitException {
        String tempTableName = "temp_users";
        UsersTable usersTable = db.getUsersTable();
        renameTable("plan_users", tempTableName);

        String tempNickTableName = "temp_nicks";
        NicknamesTable nicknamesTable = db.getNicknamesTable();
        renameTable(nicknamesTable.toString(), tempNickTableName);

        String tempKillsTableName = "temp_kills";
        KillsTable killsTable = db.getKillsTable();
        renameTable(killsTable.toString(), tempKillsTableName);

        usersTable.createTable();
        nicknamesTable.createTable();
        dropTable("plan_sessions");
        db.getSessionsTable().createTable();
        killsTable.createTable();

        UserInfoTable userInfoTable = db.getUserInfoTable();
        userInfoTable.createTable();

        String statement = "INSERT INTO plan_users " +
                "(id, uuid, registered, name)" +
                " SELECT id, uuid, registered, name" +
                " FROM " + tempTableName;
        db.execute(statement);
        statement = "INSERT INTO plan_user_info " +
                "(user_id, registered, opped, banned, server_id)" +
                " SELECT id, registered, opped, banned, '" + serverID + "'" +
                " FROM " + tempTableName;
        db.execute(statement);
        statement = "INSERT INTO plan_nicknames " +
                "(user_id, nickname, server_id)" +
                " SELECT user_id, nickname, '" + serverID + "'" +
                " FROM " + tempNickTableName;
        db.execute(statement);
        try {
            if (dbType.supportsMySQLQueries()) {
                db.execute("SET foreign_key_checks = 0");
            }
            statement = "INSERT INTO plan_kills " +
                    "(killer_id, victim_id, weapon, date, session_id)" +
                    " SELECT killer_id, victim_id, weapon, date, '0'" +
                    " FROM " + tempKillsTableName;
            db.execute(statement);
        } finally {
            if (dbType.supportsMySQLQueries()) {
                db.execute("SET foreign_key_checks = 1");
            }
        }
    }

    private void copyCommandUsage() throws DBInitException {
        String tempTableName = "temp_cmdusg";
        CommandUseTable commandUseTable = db.getCommandUseTable();

        renameTable("plan_commandusages", tempTableName);

        commandUseTable.createTable();

        String statement = "INSERT INTO plan_commandusages " +
                "(command, times_used, server_id)" +
                " SELECT command, times_used, '" + serverID + "'" +
                " FROM " + tempTableName;
        db.execute(statement);

        dropTable(tempTableName);
    }

    private void copyTPS() throws DBInitException {
        String tempTableName = "temp_tps";
        TPSTable tpsTable = db.getTpsTable();

        renameTable(tpsTable.toString(), tempTableName);

        tpsTable.createTable();

        String statement = "INSERT INTO plan_tps " +
                "(date, tps, players_online, cpu_usage, ram_usage, entities, chunks_loaded, server_id)" +
                " SELECT date, tps, players_online, cpu_usage, ram_usage, entities, chunks_loaded, '" + serverID + "'" +
                " FROM " + tempTableName;
        db.execute(statement);

        dropTable(tempTableName);
    }
}
