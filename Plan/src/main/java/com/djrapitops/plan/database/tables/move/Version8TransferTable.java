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
        serverID = db.getServerTable().getServerID(Plan.getServerUUID()).get();
    }

    @Override
    public void createTable() throws DBCreateTableException {
        throw new IllegalStateException("Method not supposed to be used on this table.");
    }

    private String tableRenameSql(String from, String to) {
        return usingMySQL ?
                "RENAME " + from + " TO " + to :
                "ALTER TABLE " + from + " RENAME TO " + to;
    }

    private String dropTableSql(String name) {
        return "DROP TABLE " + name;
    }

    public void alterTablesToV10() throws SQLException, DBCreateTableException {
        Benchmark.start("Schema copy from 8 to 10");
        copyCommandUsage();

        copyTPS();

        copyUsers();

        execute(dropTableSql("plan_ips"));
        db.getIpsTable().createTable();

        execute(dropTableSql("plan_sessions"));
        db.getSessionsTable().createTable();

        execute(dropTableSql("plan_worldtimes"));
        db.getWorldTimesTable().createTable();
        execute(dropTableSql("plan_gamemodetimes"));

        execute(dropTableSql("plan_kills"));
        db.getKillsTable().createTable();

        db.setVersion(10);
        Benchmark.stop("Schema copy from 8 to 10");
    }

    private void copyUsers() throws SQLException, DBCreateTableException {
        String tempTableName = "temp_users";
        UsersTable usersTable = db.getUsersTable();
        UserInfoTable userInfoTable = db.getUserInfoTable();

        execute(tableRenameSql("plan_users", tempTableName));

        usersTable.createTable();
        userInfoTable.createTable();

        String statement = "INSERT INTO plan_users " +
                "(" +
                "uuid, registered, name" +
                ") SELECT " +
                "uuid, registered, name" +
                " FROM " + tempTableName;
        execute(statement);
        statement = "(SELECT plan_users.id as a FROM plan_users JOIN temp_users on temp_users.uuid=plan_users.uuid" +
                " WHERE plan_users.uuid=temp_users.uuid); INSERT INTO plan_user_info " +
                "(" +
                "user_id, registered, opped, banned, server_id" +
                ") SELECT " +
                "a, registered, opped, banned, '" + serverID + "'" +
                " FROM " + tempTableName;
        execute(statement);

        copyNicknames();

        execute(dropTableSql(tempTableName));
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

    private void copyNicknames() throws SQLException, DBCreateTableException {
        String tempTableName = "temp_nicks";
        NicknamesTable nicknamesTable = db.getNicknamesTable();

        execute(tableRenameSql(nicknamesTable.toString(), tempTableName));

        nicknamesTable.createTable();

        // TODO ID Of user
        String statement = "INSERT INTO plan_nicknames " +
                "(" +
                "user_id, nickname" +
                ") SELECT " +
                "(SELECT plan_users.id FROM plan_users JOIN temp_users on temp_users.uuid=plan_users.uuid" +
                " WHERE plan_users.uuid=temp_users.uuid), " +
                "nickname" +
                " FROM " + tempTableName;
        execute(statement);
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