package main.java.com.djrapitops.plan.utilities;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

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

    public static boolean containsCombinable(List<SessionData> sessions) {
        // Checks if there are starts & ends that are the same, or less than 5000 ms away from each other.
        return sessions.stream()
                .anyMatch(s -> sessions.stream()
                        .filter(ses -> !ses.equals(s))
                        .map(ses -> ses.getSessionStart())
                        .anyMatch((Long start) -> (Math.abs(s.getSessionEnd() - start) < 5000)));
    }

    public static List<SessionData> combineSessions(List<SessionData> sessions) {
        List<SessionData> newSessions = new ArrayList<>();
        List<SessionData> removed = new ArrayList<>();
        Iterator<SessionData> iterator = sessions.iterator();
        while (iterator.hasNext()) {
            SessionData session = iterator.next();
            if (removed.contains(session)) {
                continue;
            }
            List<SessionData> close = sessions.stream().filter(ses -> Math.abs(session.getSessionEnd() - ses.getSessionStart()) < 5000).collect(Collectors.toList());
            if (!close.isEmpty()) {
                long big = MathUtils.getBiggestLong(close.stream().map((SessionData ses) -> ses.getSessionEnd()).collect(Collectors.toList()));
                session.endSession(big);
                removed.addAll(close);
            }
            newSessions.add(session);
        }
        boolean containsCombinable = containsCombinable(newSessions);
        if (containsCombinable) {
            return combineSessions(newSessions);
        } else {
            return newSessions;
        }
    }
}
