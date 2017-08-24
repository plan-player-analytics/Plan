/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.processing.player;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.systems.cache.DataCache;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Processor for updating name in the database if the player has changed it.
 *
 * @author Rsl1122
 */
public class NameProcessor extends PlayerProcessor {

    private final String playerName;
    private final String displayName;

    public NameProcessor(UUID uuid, String playerName, String displayName) {
        super(uuid);
        this.playerName = playerName;
        this.displayName = displayName;
    }

    @Override
    public void process() {
        UUID uuid = getUUID();
        Plan plugin = Plan.getInstance();
        DataCache dataCache = plugin.getDataCache();
        String cachedName = dataCache.getName(uuid);
        String cachedDisplayName = dataCache.getDisplayName(uuid);

        if (playerName.equals(cachedName) && displayName.equals(cachedDisplayName)) {
            return;
        }

        Database db = plugin.getDB();
        try {
            db.getUsersTable().updateName(uuid, playerName);
            db.getNicknamesTable().saveUserName(uuid, displayName);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }

        dataCache.updateNames(uuid, playerName, displayName);
    }
}