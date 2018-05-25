package com.djrapitops.plan.utilities;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plugin.api.utility.log.Log;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

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
    public static void backup(String dbName, Database copyFromDB) throws SQLException {
        SQLiteDB backupDB = null;
        try {
            String timeStamp = new Date().toString().substring(4, 10).replace(" ", "-");
            String fileName = dbName + "-backup-" + timeStamp;
            backupDB = new SQLiteDB(fileName);
            Collection<UUID> uuids = copyFromDB.fetch().getSavedUUIDs();
            if (uuids.isEmpty()) {
                return;
            }
            backupDB.init();
            clearAndCopy(backupDB, copyFromDB);
        } catch (DBException e) {
            Log.toLog(ManageUtils.class, e);
        } finally {
            if (backupDB != null) {
                backupDB.close();
            }
        }
    }

    /**
     * Clears a database and copies data from other database to that database.
     *
     * @param clearAndCopyToDB Database that will be cleared data will be copied
     *                         to.
     * @param copyFromDB       Database where data will be copied from
     */
    public static void clearAndCopy(Database clearAndCopyToDB, Database copyFromDB) throws SQLException, DBException {
        copyFromDB.backup().backup(clearAndCopyToDB);
    }
}
