package main.java.com.djrapitops.plan.utilities;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import static org.bukkit.Bukkit.getOfflinePlayer;

/**
 *
 * @author Rsl1122
 */
public class ManageUtils {

    /**
     * Creates a new backup sqlite file with the data of copyFromDB.
     *
     * @param dbName Name of database (mysql/sqlite)
     * @param copyFromDB Database you want to backup.
     * @return success?
     */
    public static boolean backup(String dbName, Database copyFromDB) {
        Plan plugin = Plan.getInstance();
        Date now = new Date();
        SQLiteDB backupDB = new SQLiteDB(plugin,
                dbName + "-backup-" + now.toString().substring(4, 10).replaceAll(" ", "-").replaceAll(":", "-"));
        final Collection<UUID> uuids = ManageUtils.getUUIDS(copyFromDB);
        if (uuids.isEmpty()) {
            return false;
        }
        backupDB.init();
        return clearAndCopy(backupDB, copyFromDB, uuids);
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
     * to.
     * @param copyFromDB Database where data will be copied from
     * @param fromDBsavedUUIDs UUID collection of saved uuids in the copyFromDB
     * @return success?
     */
    public static boolean clearAndCopy(Database clearAndCopyToDB, Database copyFromDB, Collection<UUID> fromDBsavedUUIDs) {
        try {
            clearAndCopyToDB.removeAllData();
            List<UserData> allUserData = copyFromDB.getUserDataForUUIDS(copyFromDB.getSavedUUIDs());
            clearAndCopyToDB.saveMultipleUserData(allUserData);
            clearAndCopyToDB.getCommandUseTable().saveCommandUse(copyFromDB.getCommandUseTable().getCommandUse());            
        } catch (SQLException | NullPointerException e) {
            Log.toLog("ManageUtils.move", e);
            return false;
        }
        return true;
    }
}
