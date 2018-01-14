package com.djrapitops.plan.utilities;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.system.database.databases.sql.tables.move.BatchOperationTable;
import com.djrapitops.plugin.api.utility.log.Log;

import java.sql.SQLException;
import java.util.*;

/**
 * @author Rsl1122
 */
public class ManageUtils {

    /**
     * Constructor used to hide the public constructor
     */
    private ManageUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a new backup sqlite file with the data of copyFromDB.
     *
     * @param dbName     Name of database (mysql/sqlite)
     * @param copyFromDB Database you want to backup.
     */
    public static void backup(String dbName, Database copyFromDB) throws DBInitException, SQLException {
        Plan plugin = Plan.getInstance();
        String timeStamp = new Date().toString().substring(4, 10).replace(" ", "-");
        String fileName = dbName + "-backup-" + timeStamp;
        SQLiteDB backupDB = new SQLiteDB(fileName);
        Collection<UUID> uuids = ManageUtils.getUUIDS(copyFromDB);
        if (uuids.isEmpty()) {
            return;
        }
        backupDB.init();
        clearAndCopy(backupDB, copyFromDB);
        backupDB.close();
    }

    /**
     * Get the saved UUIDs in a hashset
     *
     * @param db Database to get UUIDs from
     * @return uuids hashset as a Collection.
     */
    public static Collection<UUID> getUUIDS(Database db) {
        final Set<UUID> uuids = new HashSet<>();
        try {
            uuids.addAll(db.fetch().getSavedUUIDs());
        } catch (DBException e) {
            Log.toLog(ManageUtils.class, e);
        }
        return uuids;
    }

    /**
     * Clears a database and copies data from other database to that database.
     *
     * @param clearAndCopyToDB Database that will be cleared data will be copied
     *                         to.
     * @param copyFromDB       Database where data will be copied from
     */
    public static void clearAndCopy(Database clearAndCopyToDB, Database copyFromDB) throws SQLException {
        BatchOperationTable toDB = new BatchOperationTable((SQLDB) clearAndCopyToDB);
        BatchOperationTable fromDB = new BatchOperationTable((SQLDB) copyFromDB);

        toDB.removeAllData();
        fromDB.copyEverything(toDB);
    }

    @Deprecated
    public static Database getDB(String dbName) throws DBInitException {
        return DBSystem.getInstance().getActiveDatabase(dbName);
    }
}
