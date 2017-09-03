/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.database.tables.move;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.tables.ServerTable;
import main.java.com.djrapitops.plan.database.tables.Table;
import main.java.com.djrapitops.plan.database.tables.UsersTable;
import main.java.com.djrapitops.plan.systems.info.server.ServerInfo;

import java.sql.SQLException;
import java.util.List;

/**
 * A Fake table used to store a lot of big table operations.
 * <p>
 * To use this table create a new BatchOperationTable with both SQLDB objects.
 * {@code SQLDB from; SQLDB to;}
 * {@code fromT = new BatchOperationTable(from);}
 * {@code toT = new BatchOperationTable(to);}
 * {@code fromT.copy(toT);}
 * <p>
 * The copy methods assume that the table has been cleared, or that no duplicate data will be entered for a user.
 * <p>
 * clearTable methods can be used to clear the table beforehand.
 * <p>
 * Server & User tables should be copied first.
 *
 * @author Rsl1122
 * @since 4.0.0
 */
public class BatchOperationTable extends Table {

    /**
     * Constructor, call to access copy functionality.
     *
     * @param database
     * @throws IllegalStateException if database.init has not been called.
     * @throws ClassCastException    if database is not SQLDB.
     */
    public BatchOperationTable(Database database) {
        super("", (SQLDB) database, false);
        if (!db.isOpen()) {
            throw new IllegalStateException("Given Database had not been initialized.");
        }
    }

    @Override
    public void createTable() throws DBCreateTableException {
        throw new IllegalStateException("Method not supposed to be used on this table.");
    }

    public void clearTable(Table table) throws SQLException {
        table.removeAllData();
    }

    @Override
    public void removeAllData() throws SQLException {
        db.removeAllData();
    }

    public void copyEverything(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        Log.debug("Start Batch Copy Everything");
        toDB.removeAllData();

        copyServers(toDB);
        System.out.println(toDB.getDb().getServerTable().getBukkitServers().toString());
        copyUsers(toDB);
        System.out.println(toDB.getDb().getSavedUUIDs().toString());
        copyWorlds(toDB);
        copyTPS(toDB);
        copyWebUsers(toDB);
        copyCommandUse(toDB);
        copyActions(toDB);
        copyIPsAndGeolocs(toDB);
        copyNicknames(toDB);
        copySessions(toDB);
        copyUserInfo(toDB);
    }

    public void copyActions(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        Log.debug("Batch Copy Actions");
        toDB.getDb().getActionsTable().insertActions(db.getActionsTable().getAllActions());
    }

    public void copyCommandUse(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        Log.debug("Batch Copy Commands");
        toDB.getDb().getCommandUseTable().insertCommandUsage(db.getCommandUseTable().getAllCommandUsages());
    }

    public void copyIPsAndGeolocs(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        Log.debug("Batch Copy IPs & Geolocations");
        toDB.getDb().getIpsTable().insertIPsAndGeolocations(db.getIpsTable().getAllIPsAndGeolocations());
    }

    public void copyNicknames(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        Log.debug("Batch Copy Nicknames");
        toDB.getDb().getNicknamesTable().insertNicknames(db.getNicknamesTable().getAllNicknames());
    }

    public void copyWebUsers(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        Log.debug("Batch Copy WebUsers");
        toDB.getDb().getSecurityTable().addUsers(db.getSecurityTable().getUsers());
    }

    public void copyServers(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        Log.debug("Batch Copy Servers");
        ServerTable serverTable = db.getServerTable();
        List<ServerInfo> servers = serverTable.getBukkitServers();
        System.out.println(servers.toString());
        serverTable.getBungeeInfo().ifPresent(servers::add);
        toDB.getDb().getServerTable().insertAllServers(servers);
    }

    public void copyTPS(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        Log.debug("Batch Copy TPS");
        toDB.getDb().getTpsTable().insertAllTPS(db.getTpsTable().getAllTPS());
    }

    public void copyUserInfo(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        Log.debug("Batch Copy UserInfo");
        toDB.getDb().getUserInfoTable().insertUserInfo(db.getUserInfoTable().getAllUserInfo());
    }

    public void copyWorlds(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        Log.debug("Batch Copy Worlds");
        toDB.getDb().getWorldTable().saveWorlds(db.getWorldTable().getWorlds());
    }

    public void copyUsers(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        Log.debug("Batch Copy Users");
        UsersTable fromTable = db.getUsersTable();
        UsersTable toTable = toDB.getDb().getUsersTable();
        toTable.insertUsers(fromTable.getUsers());
        toTable.updateKicked(fromTable.getAllTimesKicked());
    }

    public void copySessions(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        Log.debug("Batch Copy Sessions");
        toDB.getDb().getSessionsTable().insertSessions(db.getSessionsTable().getAllSessions(true), true);
    }
}