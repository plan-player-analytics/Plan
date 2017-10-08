/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.processing.player;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.tables.NicknamesTable;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.processing.NewNickActionProcessor;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Processor for updating name in the database if the player has changed it.
 *
 * @author Rsl1122
 * @since 4.0.0
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

        boolean sameAsCached = displayName.equals(cachedDisplayName);
        if (playerName.equals(cachedName) && sameAsCached) {
            return;
        }

        Database db = plugin.getDB();
        NicknamesTable nicknamesTable = db.getNicknamesTable();
        cueNameChangeActionProcessor(uuid, plugin, nicknamesTable);

        try {
            db.getUsersTable().updateName(uuid, playerName);

            nicknamesTable.saveUserName(uuid, displayName);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }

        dataCache.updateNames(uuid, playerName, displayName);
    }

    private void cueNameChangeActionProcessor(UUID uuid, Plan plugin, NicknamesTable nicknamesTable) {
        try {
            List<String> nicknames = nicknamesTable.getNicknames(uuid, Plan.getServerUUID());
            if (nicknames.contains(displayName)) {
                return;
            }
            plugin.addToProcessQueue(new NewNickActionProcessor(uuid, displayName));
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}