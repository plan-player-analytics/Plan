package main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;

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
     * @return success?
     */
    public static boolean backup(String dbName, Database copyFromDB) {
        Plan plugin = Plan.getInstance();
        String timeStamp = new Date().toString().substring(4, 10).replace(" ", "-");
        String fileName = dbName + "-backup-" + timeStamp;
        SQLiteDB backupDB = new SQLiteDB(plugin, fileName);
        Collection<UUID> uuids = ManageUtils.getUUIDS(copyFromDB);
        if (uuids.isEmpty()) {
            return false;
        }
        backupDB.init();
        return clearAndCopy(backupDB, copyFromDB);
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
            uuids.addAll(db.getSavedUUIDs());
        } catch (SQLException e) {
            Log.toLog("ManageUtils.getUUIDS", e);
        }
        return uuids;
    }

    /**
     * Clears a database and copies data from other database to that database.
     *
     * @param clearAndCopyToDB Database that will be cleared data will be copied
     *                         to.
     * @param copyFromDB       Database where data will be copied from
     * @return success?
     */
    public static boolean clearAndCopy(Database clearAndCopyToDB, Database copyFromDB) {
//        try {
        clearAndCopyToDB.removeAllData();
        //TODO  List<UserInfo> allUserData = copyFromDB.getUserDataForUUIDS(copyFromDB.getSavedUUIDs());
        //  clearAndCopyToDB.saveMultipleUserData(allUserData);
//        TODO    clearAndCopyToDB.getCommandUseTable().saveCommandUse(copyFromDB.getCommandUseTable().getCommandUse());
        //TODO   clearAndCopyToDB.getTpsTable().saveTPSData(copyFromDB.getTpsTable().getTPSData());
//        } catch (SQLException | NullPointerException e) {
//            Log.toLog("ManageUtils.move", e);
//            return false;
//        }
        return true;
    }

    public static Database getDB(Plan plugin, String dbName) {
        Database database = null;
        for (Database sqldb : plugin.getDatabases()) {
            String dbConfigName = sqldb.getConfigName();
            if (Verify.equalsIgnoreCase(dbName, dbConfigName)) {
                database = sqldb;
                if (!database.init()) {
                    return null;
                }
                break;
            }
        }
        return database;
    }
}
