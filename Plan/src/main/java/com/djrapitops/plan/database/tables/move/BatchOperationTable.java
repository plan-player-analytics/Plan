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

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
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

    public void copyActions(BatchOperationTable toDB) throws SQLException {
        toDB.db.getActionsTable().insertActions(db.getActionsTable().getAllActions());
    }

    public void copyCommandUse(BatchOperationTable toDB) throws SQLException {
        toDB.db.getCommandUseTable().insertCommandUsage(db.getCommandUseTable().getAllCommandUsages());
    }
}