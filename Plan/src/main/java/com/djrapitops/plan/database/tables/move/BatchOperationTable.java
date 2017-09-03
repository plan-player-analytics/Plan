/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.database.tables.move;

import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
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
     * @param db
     * @param usingMySQL
     * @throws IllegalStateException if db.init has not been called.
     */
    public BatchOperationTable(SQLDB db, boolean usingMySQL) {
        super("", db, usingMySQL);
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

    public void copyEverything(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.removeAllData();
        copyServers(toDB);
        copyUsers(toDB);
        copyWorlds(toDB);
        copyUserInfo(toDB);
        copyActions(toDB);
        copyCommandUse(toDB);
        copyIPsAndGeolocs(toDB);
        copyNicknames(toDB);
        copyTPS(toDB);
        copyWebUsers(toDB);
        copySessions(toDB);
    }

    public void copyActions(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getActionsTable().insertActions(db.getActionsTable().getAllActions());
    }

    public void copyCommandUse(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getCommandUseTable().insertCommandUsage(db.getCommandUseTable().getAllCommandUsages());
    }

    public void copyIPsAndGeolocs(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getIpsTable().insertIPsAndGeolocations(db.getIpsTable().getAllIPsAndGeolocations());
    }

    public void copyNicknames(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getNicknamesTable().insertNicknames(db.getNicknamesTable().getAllNicknames());
    }

    public void copyWebUsers(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getSecurityTable().addUsers(db.getSecurityTable().getUsers());
    }

    public void copyServers(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        ServerTable serverTable = db.getServerTable();
        List<ServerInfo> servers = serverTable.getBukkitServers();
        serverTable.getBungeeInfo().ifPresent(servers::add);
        toDB.db.getServerTable().insertAllServers(servers);
    }

    public void copyTPS(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getTpsTable().insertAllTPS(db.getTpsTable().getAllTPS());
    }

    public void copyUserInfo(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getUserInfoTable().insertUserInfo(db.getUserInfoTable().getAllUserInfo());
    }

    public void copyWorlds(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getWorldTable().saveWorlds(db.getWorldTable().getWorlds());
    }

    public void copyUsers(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        UsersTable fromTable = db.getUsersTable();
        UsersTable toTable = toDB.db.getUsersTable();
        toTable.insertUsers(fromTable.getUsers());
        toTable.updateKicked(fromTable.getAllTimesKicked());
    }

    public void copySessions(BatchOperationTable toDB) throws SQLException {
        if (toDB.equals(this)) {
            return;
        }
        toDB.db.getSessionsTable().insertSessions(db.getSessionsTable().getAllSessions(true), true);
    }
}