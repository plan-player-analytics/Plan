/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.database.tables.move;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.tables.*;
import main.java.com.djrapitops.plan.utilities.Benchmark;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Class used for executing transfer queries when the database has version 8.
 * <p>
 * Changes the DB Schema to 10.
 *
 * @author Rsl1122
 */
public class Version8TransferTable extends Table {

    private final int serverID;

    public Version8TransferTable(SQLDB db, boolean usingMySQL) throws SQLException {
        super("", db, usingMySQL);
        Optional<Integer> serverID = db.getServerTable().getServerID(Plan.getServerUUID());
        if (!serverID.isPresent()) {
            throw new IllegalStateException("Server UUID was not registered, try rebooting the plugin.");
        }
        this.serverID = serverID.get();
    }

    @Override
    public void createTable() throws DBCreateTableException {
        throw new IllegalStateException("Method not supposed to be used on this table.");
    }

    private String tableRenameSql(String from, String to) {
        return usingMySQL ?
                "RENAME TABLE " + from + " TO " + to :
                "ALTER TABLE " + from + " RENAME TO " + to;
    }

    private String dropTableSql(String name) {
        return "DROP TABLE " + name;
    }

    public void alterTablesToV10() throws SQLException, DBCreateTableException {
        Benchmark.start("Schema copy from 8 to 10");
        copyCommandUsage();

        copyTPS();

        execute(dropTableSql("plan_user_info"));
        copyUsers();

        execute(dropTableSql("plan_ips"));
        db.getIpsTable().createTable();
        execute(dropTableSql("plan_world_times"));
        execute(dropTableSql("plan_worlds"));
        db.getWorldTable().createTable();
        db.getWorldTimesTable().createTable();

        execute(dropTableSql("plan_actions"));
        db.getActionsTable().createTable();

        execute(dropTableSql("plan_gamemodetimes"));
        execute(dropTableSql("temp_nicks"));
        execute(dropTableSql("temp_kills"));
        execute(dropTableSql("temp_users"));

        db.setVersion(10);
        Benchmark.stop("Schema copy from 8 to 10");
    }

    private void copyUsers() throws SQLException, DBCreateTableException {
        String tempTableName = "temp_users";
        UsersTable usersTable = db.getUsersTable();
        execute(tableRenameSql("plan_users", tempTableName));

        String tempNickTableName = "temp_nicks";
        NicknamesTable nicknamesTable = db.getNicknamesTable();
        execute(tableRenameSql(nicknamesTable.toString(), tempNickTableName));

        String tempKillsTableName = "temp_kills";
        KillsTable killsTable = db.getKillsTable();
        execute(tableRenameSql(killsTable.toString(), tempKillsTableName));

        usersTable.createTable();
        nicknamesTable.createTable();
        execute(dropTableSql("plan_sessions"));
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

    private void copyCommandUsage() throws SQLException, DBCreateTableException {
        String tempTableName = "temp_cmdusg";
        CommandUseTable commandUseTable = db.getCommandUseTable();

        execute(tableRenameSql("plan_commandusages", tempTableName));

        commandUseTable.createTable();

        String statement = "INSERT INTO plan_commandusages " +
                "(" +
                "command, times_used, server_id" +
                ") SELECT " +
                "command, times_used, '" + serverID + "'" +
                " FROM " + tempTableName;
        execute(statement);

        execute(dropTableSql(tempTableName));
    }

    private void copyTPS() throws SQLException, DBCreateTableException {
        String tempTableName = "temp_tps";
        TPSTable tpsTable = db.getTpsTable();

        execute(tableRenameSql(tpsTable.toString(), tempTableName));

        tpsTable.createTable();

        String statement = "INSERT INTO plan_tps " +
                "(" +
                "date, tps, players_online, cpu_usage, ram_usage, entities, chunks_loaded, server_id" +
                ") SELECT " +
                "date, tps, players_online, cpu_usage, ram_usage, entities, chunks_loaded, '" + serverID + "'" +
                " FROM " + tempTableName;
        execute(statement);

        execute(dropTableSql(tempTableName));
    }
}