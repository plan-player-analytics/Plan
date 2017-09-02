/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.database.tables.move;

import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.api.exceptions.DatabaseException;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.tables.Table;

import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

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
    public BatchOperationTable(SQLDB db, boolean usingMySQL) {
        super("", db, usingMySQL);
    }

    @Override
    public void createTable() throws DBCreateTableException {
        throw new IllegalStateException("Method not supposed to be used on this table.");
    }

    public void clearTable(Table table) throws DatabaseException {
        table.removeAllData();
    }

    public void clearTable(Collection<UUID> uuids, Table table) {
        // TODO
    }

    public void copyActions(BatchOperationTable toDB) throws SQLException {
        toDB.db.getActionsTable().insertActions(db.getActionsTable().getAllActions());
    }

    public void copyCommandUse(BatchOperationTable toDB) throws SQLException {
        toDB.db.getCommandUseTable().insertCommandUsage(db.getCommandUseTable().getAllCommandUsages());
    }

    public void copyIPsAndGeolocs(BatchOperationTable toDB) throws SQLException {
        toDB.db.getIpsTable().insertIPsAndGeolocations(db.getIpsTable().getAllIPsAndGeolocations());
    }

    public void copyNicknames(BatchOperationTable toDB) throws SQLException {
        toDB.db.getNicknamesTable().insertNicknames(db.getNicknamesTable().getAllNicknames());
    }

    public void copyWebUsers(BatchOperationTable toDB) throws SQLException {
        toDB.db.getSecurityTable().addUsers(db.getSecurityTable().getUsers());
    }
}