/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database.databases.sql.tables.move;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.*;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.api.Benchmark;

import java.util.Optional;

/**
 * Class used for executing transfer queries when the database has version 8.
 * <p>
 * Changes the DB Schema to 10.
 *
 * @author Rsl1122
 */
public class Version8TransferTable extends TransferTable {

    private final int serverID;

    public Version8TransferTable(SQLDB db) {
        super(db);
        Optional<Integer> serverID = db.getServerTable().getServerID(ServerInfo.getServerUUID());
        if (!serverID.isPresent()) {
            throw new IllegalStateException("Server UUID was not registered, try rebooting the plugin.");
        }
        this.serverID = serverID.get();
    }

    public void alterTablesToV10() throws DBInitException {
        Benchmark.start("Schema copy from 8 to 10");
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

        db.setVersion(10);
        Benchmark.stop("Schema copy from 8 to 10");
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
                "(" +
                "id, uuid, registered, name" +
                ") SELECT " +
                "id, uuid, registered, name" +
                " FROM " + tempTableName;
        execute(statement);
        statement = "INSERT INTO plan_user_info " +
                "(" +
                "user_id, registered, opped, banned, server_id" +
                ") SELECT " +
                "id, registered, opped, banned, '" + serverID + "'" +
                " FROM " + tempTableName;
        execute(statement);
        statement = "INSERT INTO plan_nicknames " +
                "(" +
                "user_id, nickname, server_id" +
                ") SELECT " +
                "user_id, nickname, '" + serverID + "'" +
                " FROM " + tempNickTableName;
        execute(statement);
        try {
            if (usingMySQL) {
                execute("SET foreign_key_checks = 0");
            }
            statement = "INSERT INTO plan_kills " +
                    "(" +
                    "killer_id, victim_id, weapon, date, session_id" +
                    ") SELECT " +
                    "killer_id, victim_id, weapon, date, '0'" +
                    " FROM " + tempKillsTableName;
            execute(statement);
        } finally {
            if (usingMySQL) {
                execute("SET foreign_key_checks = 1");
            }
        }
    }

    private void copyCommandUsage() throws DBInitException {
        String tempTableName = "temp_cmdusg";
        CommandUseTable commandUseTable = db.getCommandUseTable();

        renameTable("plan_commandusages", tempTableName);

        commandUseTable.createTable();

        String statement = "INSERT INTO plan_commandusages " +
                "(" +
                "command, times_used, server_id" +
                ") SELECT " +
                "command, times_used, '" + serverID + "'" +
                " FROM " + tempTableName;
        execute(statement);

        dropTable(tempTableName);
    }

    private void copyTPS() throws DBInitException {
        String tempTableName = "temp_tps";
        TPSTable tpsTable = db.getTpsTable();

        renameTable(tpsTable.toString(), tempTableName);

        tpsTable.createTable();

        String statement = "INSERT INTO plan_tps " +
                "(" +
                "date, tps, players_online, cpu_usage, ram_usage, entities, chunks_loaded, server_id" +
                ") SELECT " +
                "date, tps, players_online, cpu_usage, ram_usage, entities, chunks_loaded, '" + serverID + "'" +
                " FROM " + tempTableName;
        execute(statement);

        dropTable(tempTableName);
    }
}
