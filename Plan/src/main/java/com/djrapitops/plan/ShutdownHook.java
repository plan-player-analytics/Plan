/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan;

import main.java.com.djrapitops.plan.api.exceptions.DatabaseInitException;
import main.java.com.djrapitops.plan.data.Action;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.tables.Actions;
import main.java.com.djrapitops.plan.database.tables.SessionsTable;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class ShutdownHook extends Thread {

    private static boolean active = false;
    private static DataCache dataCache;
    private static SQLDB db;

    public ShutdownHook(Plan plugin) {
        if (!active) {
            Runtime.getRuntime().addShutdownHook(this);
        }
        active = true;

        db = (SQLDB) plugin.getDB();
        dataCache = plugin.getDataCache();
    }

    @Override
    public void run() {
        try {
            Map<UUID, Session> activeSessions = dataCache.getActiveSessions();
            long now = MiscUtils.getTime();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db.init();
            }

            saveFirstSessionInformation(now);
            saveActiveSessions(activeSessions, now);
        } catch (DatabaseInitException e) {
            Log.toLog(this.getClass().getName(), e);
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (SQLException e) {
                    Log.toLog(this.getClass().getName(), e);
                }
            }
            db = null;
            dataCache = null;
        }
    }

    private void saveFirstSessionInformation(long now) {
        try {
            for (Map.Entry<UUID, Integer> entry : dataCache.getFirstSessionMsgCounts().entrySet()) {
                UUID uuid = entry.getKey();
                int messagesSent = entry.getValue();
                db.getActionsTable().insertAction(uuid, new Action(now, Actions.FIRST_LOGOUT, "Messages sent: " + messagesSent));
            }
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    private void saveActiveSessions(Map<UUID, Session> activeSessions, long now) {
        SessionsTable sessionsTable = db.getSessionsTable();
        for (Map.Entry<UUID, Session> entry : activeSessions.entrySet()) {
            UUID uuid = entry.getKey();
            Session session = entry.getValue();
            session.endSession(now);
            try {
                sessionsTable.saveSession(uuid, session);
            } catch (SQLException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        }
    }
}