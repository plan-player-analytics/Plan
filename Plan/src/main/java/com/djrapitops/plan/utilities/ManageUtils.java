package main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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
     * @throws java.sql.SQLException
     */
    public static boolean backup(String dbName, Database copyFromDB) throws SQLException {
        Plan plugin = Plan.getInstance();
        String timeStamp = new Date().toString().substring(4, 10).replaceAll(" ", "-").replaceAll(":", "-");
        String fileName = dbName + "-backup-" + timeStamp;
        SQLiteDB backupDB = new SQLiteDB(plugin, fileName);
        Collection<UUID> uuids = ManageUtils.getUUIDS(copyFromDB);
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
     * @throws java.sql.SQLException
     */
    public static boolean clearAndCopy(Database clearAndCopyToDB, Database copyFromDB, Collection<UUID> fromDBsavedUUIDs) throws SQLException {
        try {
            clearAndCopyToDB.removeAllData();
            List<UserData> allUserData = copyFromDB.getUserDataForUUIDS(copyFromDB.getSavedUUIDs());
            clearAndCopyToDB.saveMultipleUserData(allUserData);
            clearAndCopyToDB.getCommandUseTable().saveCommandUse(copyFromDB.getCommandUseTable().getCommandUse());
            clearAndCopyToDB.getTpsTable().saveTPSData(copyFromDB.getTpsTable().getTPSData());
        } catch (SQLException | NullPointerException e) {
            Log.toLog("ManageUtils.move", e);
            return false;
        }
        return true;
    }

    /**
     *
     * @param sessions
     * @return
     */
    public static boolean containsCombinable(List<SessionData> sessions) {
        return containsCombinable(sessions, 5000);
    }

    private static boolean containsCombinable(List<SessionData> sessions, int threshold) {
        // Checks if there are starts & ends that are the same, or less than threshold ms away from each other.
        return sessions.stream()
                .anyMatch(s -> sessions.stream()
                        .filter(ses -> !ses.equals(s))
                        .map(ses -> ses.getSessionStart())
                        .anyMatch((Long start) -> (Math.abs(s.getSessionEnd() - start) < threshold)));
    }

    /**
     *
     * @param sessions
     * @param loginTimes
     * @return
     */
    public static List<SessionData> combineSessions(List<SessionData> sessions, Integer loginTimes) {
        return combineSessions(sessions, loginTimes, 5000);
    }

    private static List<SessionData> combineSessions(List<SessionData> sessions, Integer loginTimes, int threshold) {
        if (threshold >= 35000) {
            return sessions;
        }
        List<SessionData> newSessions = new ArrayList<>();
        List<SessionData> removed = new ArrayList<>();
        for (SessionData session : sessions) {
            if (removed.contains(session)) {
                continue;
            }
            List<SessionData> close = sessions.stream().filter(ses -> Math.abs(session.getSessionEnd() - ses.getSessionStart()) < threshold).collect(Collectors.toList());
            if (!close.isEmpty()) {
                long big = MathUtils.getBiggestLong(close.stream().map((SessionData ses) -> ses.getSessionEnd()).collect(Collectors.toList()));
                session.endSession(big);
                removed.addAll(close);
            }
            newSessions.add(session);
        }
        if (loginTimes == newSessions.size()) {
            return newSessions;
        }
        boolean containsCombinable = containsCombinable(newSessions, threshold);
        if (containsCombinable) {
            return combineSessions(newSessions, threshold + 1000);
        } else {
            return newSessions;
        }
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
