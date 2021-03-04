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
package com.djrapitops.plan.storage.database.transactions.patches;

import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.sql.tables.*;

import java.util.Optional;

import static com.djrapitops.plan.storage.database.sql.building.Sql.FROM;

/**
 * Table schema change patch for version 4.0.0 to support BungeeCord servers.
 * <p>
 * This patch makes the database compatible with further changes to the schema,
 * bugs in this patch are possible, as the patch is untested against new schema.
 * <p>
 * Version 10 comes from "schema version" that was in use in the database to version changes
 * before Patch system was implemented.
 *
 * @author AuroraLS3
 * @see VersionTableRemovalPatch for Patch that removes the schema versions
 */
public class Version10Patch extends Patch {

    private Integer serverID;

    @Override
    public boolean hasBeenApplied() {
        return !hasTable("plan_gamemodetimes");
    }

    @Override
    protected void applyPatch() {
        Optional<Server> server = query(ServerQueries.fetchServerMatchingIdentifier(getServerUUID()));
        serverID = server.flatMap(Server::getId)
                .orElseThrow(() -> new IllegalStateException("Server UUID was not registered, try rebooting the plugin."));

        alterTablesToV10();
    }

    public void alterTablesToV10() {
        copyTPS();

        dropTable(UserInfoTable.TABLE_NAME);
        copyUsers();

        dropTable(GeoInfoTable.TABLE_NAME);
        execute(GeoInfoTable.createTableSQL(dbType));
        dropTable(WorldTimesTable.TABLE_NAME);
        dropTable(WorldTable.TABLE_NAME);
        execute(WorldTable.createTableSQL(dbType));
        execute(WorldTimesTable.createTableSQL(dbType));

        dropTable("plan_gamemodetimes");
        dropTable("temp_nicks");
        dropTable("temp_kills");
        dropTable("temp_users");
    }

    private void copyUsers() {
        String tempTableName = "temp_users";
        renameTable(UsersTable.TABLE_NAME, tempTableName);

        String tempNickTableName = "temp_nicks";
        renameTable(NicknamesTable.TABLE_NAME, tempNickTableName);

        String tempKillsTableName = "temp_kills";
        renameTable(KillsTable.TABLE_NAME, tempKillsTableName);

        execute(UsersTable.createTableSQL(dbType));
        execute(NicknamesTable.createTableSQL(dbType));
        dropTable(SessionsTable.TABLE_NAME);
        execute(SessionsTable.createTableSQL(dbType));
        execute(KillsTable.createTableSQL(dbType));

        execute(UserInfoTable.createTableSQL(dbType));

        String statement = "INSERT INTO plan_users " +
                "(id, uuid, registered, name)" +
                " SELECT id, uuid, registered, name" +
                FROM + tempTableName;
        execute(statement);
        statement = "INSERT INTO plan_user_info " +
                "(user_id, registered, opped, banned, server_id)" +
                " SELECT id, registered, opped, banned, '" + serverID + "'" +
                FROM + tempTableName;
        execute(statement);
        statement = "INSERT INTO plan_nicknames " +
                "(user_id, nickname, server_id)" +
                " SELECT user_id, nickname, '" + serverID + "'" +
                FROM + tempNickTableName;
        execute(statement);
        statement = "INSERT INTO plan_kills " +
                "(killer_id, victim_id, weapon, date, session_id)" +
                " SELECT killer_id, victim_id, weapon, date, '0'" +
                FROM + tempKillsTableName;
        execute(statement);
    }

    private void copyTPS() {
        String tempTableName = "temp_tps";

        renameTable(TPSTable.TABLE_NAME, tempTableName);

        execute(TPSTable.createTableSQL(dbType));

        String statement = "INSERT INTO plan_tps " +
                "(date, tps, players_online, cpu_usage, ram_usage, entities, chunks_loaded, server_id)" +
                " SELECT date, tps, players_online, cpu_usage, ram_usage, entities, chunks_loaded, '" + serverID + "'" +
                FROM + tempTableName;
        execute(statement);

        dropTable(tempTableName);
    }
}
